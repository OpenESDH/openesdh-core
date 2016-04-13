package dk.openesdh.repo.services.authorities;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dk.openesdh.repo.exceptions.DomainException;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.NodeInfoService;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.authorities.UsersCsvParser.User;

@Service("UsersService")
public class UsersServiceImpl implements UsersService {

    private static final String MSG_CREATED = "person.msg.userCSV.created";
    private static final String MSG_EXISTING = "person.msg.userCSV.existing";
    private static final QName PROP_CM_PASSWORD = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "password");
    private static final String USERNAME_BY_EMAIL_CMIS_QUERY = "SELECT cm:userName FROM cm:person WHERE cm:userName IS NOT NULL AND cm:email='%s'";

    private final Set<Consumer<JSONObject>> userJsonDecorators = new HashSet<>();
    private final Set<Consumer<UserSavingContext>> userValidators = new HashSet<>();
    private final Set<Consumer<UserSavingContext>> beforeSaveActions = new HashSet<>();
    private final Set<Consumer<UserSavingContext>> afterSaveActions = new HashSet<>();

    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;
    @Autowired
    @Qualifier("AuthenticationService")
    private MutableAuthenticationService authenticationService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;
    @Autowired
    @Qualifier("tenantService")
    private TenantService tenantService;
    @Autowired
    @Qualifier("NodeInfoService")
    private NodeInfoService nodeInfoService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    private TransactionRunner transactionRunner;
    @Autowired
    private SearchService searchService;

    @Override
    public JSONObject uploadUsersCsv(InputStream usersCsv) throws IOException {

        UsersCsvParser parser = new UsersCsvParser();
        List<User> users = parser.parse(usersCsv);
        JSONObject json = new JSONObject();
        json.put("totalUsers", users.size());
        json.put("addedUsers", 0);
        json.put("users", Collections.emptyList());
        if (users.isEmpty()) {
            return json;
        }
        return addUsers(users);
    }

    private JSONObject addUsers(List<User> users) {

        List<Map<String, String>> uploadResults = new ArrayList<>();

        int addedUsers = transactionRunner.runInTransaction(() -> {
            int iAddedUsers = 0;
            for (User user : users) {
                String status = addUser(user.getProperties());
                uploadResults.add(getUploadStatus(user.getProperties(), status));
                if (MSG_CREATED.equals(status)) {
                    iAddedUsers++;
                }
                manageUserMemberships(user);
            }
            return iAddedUsers;
        });
        JSONObject json = new JSONObject();
        json.put("totalUsers", users.size());
        json.put("addedUsers", addedUsers);
        json.put("users", uploadResults);
        return json;
    }

    @PostConstruct
    public void init() {
        userValidators.add(mandatoryPropValidator(ContentModel.PROP_USERNAME, "userName"));
        userValidators.add(mandatoryPropValidator(ContentModel.PROP_FIRSTNAME, "firstName"));
        userValidators.add(mandatoryPropValidator(ContentModel.PROP_EMAIL, "email"));
        userValidators.add(uniqueEmailValidator());

        userJsonDecorators.add(getManagerAssocDecorator());
    }

    public JSONObject getUserJson(NodeRef nodeRef) {
        JSONObject userJson = nodeInfoService.getNodeParametersJSON(nodeRef);
        userJson.put("enabled", authenticationService.getAuthenticationEnabled((String) ((JSONObject) userJson.get("cm")).get("userName")));
        userJsonDecorators.forEach(t -> t.accept(userJson));
        return userJson;
    }

    private UserSavingContext createUserSavingContext(Map<QName, Serializable> userProps, boolean accountEnabled) {
        UserSavingContext context = new UserSavingContext();
        if (userProps.containsKey(ContentModel.PROP_NODE_UUID)) {
            context.setNodeRef(new NodeRef(
                    (String) userProps.get(ContentModel.PROP_STORE_PROTOCOL),
                    (String) userProps.get(ContentModel.PROP_STORE_IDENTIFIER),
                    (String) userProps.get(ContentModel.PROP_NODE_UUID)));
        }
        context.setUserName((String) userProps.get(ContentModel.PROP_USERNAME));
        context.setAccountEnabled(accountEnabled);
        context.setProps(userProps);
        context.setCopiedProps(Maps.newHashMap(userProps));
        return context;
    }

    public NodeRef createUser(Map<QName, Serializable> userProps, boolean accountEnabled) {
        UserSavingContext context = createUserSavingContext(userProps, accountEnabled);

        executeValidators(context);

        String userName = (String) context.getProps().get(ContentModel.PROP_USERNAME);
        userName = PersonServiceImpl.updateUsernameForTenancy(userName, tenantService);

        if (personService.personExists(userName)) {
            throw new UsernameExistsDomainException().forField("userName");
        }

        String password = (String) context.getProps().get(PROP_CM_PASSWORD);
        context.getProps().remove(PROP_CM_PASSWORD);

        executeBeforeSaveActions(context);

        NodeRef person = personService.createPerson(context.getProps());
        authenticationService.createAuthentication(userName, password.toCharArray());
        authenticationService.setAuthenticationEnabled(userName, context.isAccountEnabled());

        context.setNodeRef(person);

        executeAfterSaveActions(context);

        return person;
    }

    @Override
    public NodeRef updateUser(Map<QName, Serializable> userProps, boolean accountEnabled) {
        UserSavingContext context = createUserSavingContext(userProps, accountEnabled);

        executeValidators(context);

        String userName = (String) userProps.get(ContentModel.PROP_USERNAME);
        userName = PersonServiceImpl.updateUsernameForTenancy(userName, tenantService);

        context.getProps().remove(PROP_CM_PASSWORD);

        executeBeforeSaveActions(context);

        personService.setPersonProperties(userName, context.getProps());
        if (authenticationService.getAuthenticationEnabled(userName) != context.isAccountEnabled()) {
            authenticationService.setAuthenticationEnabled(userName, context.isAccountEnabled());
        }

        NodeRef person = personService.getPerson(userName);

        executeAfterSaveActions(context);

        return person;
    }

    private String addUser(Map<QName, String> userProps) {
        try {
            createUser((Map<QName, Serializable>) (Map) userProps, true);
        } catch (UsernameExistsDomainException e) {
            return MSG_EXISTING;
        }
        return MSG_CREATED;
    }

    private Map<String, String> getUploadStatus(Map<QName, String> props, String status) {
        Map<String, String> result = new HashMap<>();
        result.put("username", props.get(ContentModel.PROP_USERNAME));
        result.put("uploadStatus", I18NUtil.getMessage(status, props.get(ContentModel.PROP_EMAIL)));
        return result;
    }

    private void manageUserMemberships(User user) {
        String userName = user.getProperties().get(ContentModel.PROP_USERNAME);
        Set<String> currentGroups = authorityService.getContainingAuthorities(AuthorityType.GROUP, userName, true);
        user.getGroups()
                .stream()
                .map(group -> PermissionService.GROUP_PREFIX + group)
                .filter(group -> !currentGroups.contains(group))
                .forEach(group -> authorityService.addAuthority(group, userName));
    }

    private Consumer<UserSavingContext> mandatoryPropValidator(QName qname, String fieldName) {
        return context -> {
            if (!context.getProps().containsKey(qname)) {
                throw new DomainException(ERROR_REQUIRED).forField(fieldName);
            }
        };
    }

    private Consumer<UserSavingContext> uniqueEmailValidator() {
        return context -> {
            Optional<String> personByEmail = getPersonByEmail((String) context.getProps().get(ContentModel.PROP_EMAIL));
            personByEmail.ifPresent(p -> {
                if (!p.equals((String) context.getUserName())) {
                    throw new DomainException(ERROR_EMAIL_EXISTS).forField("email");
                }
            });
        };
    }

    private Optional<String> getPersonByEmail(String email) {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO);
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParams.setQueryConsistency(QueryConsistency.TRANSACTIONAL);

        StringBuilder query = new StringBuilder(256);
        query.append(String.format(USERNAME_BY_EMAIL_CMIS_QUERY, email));

        searchParams.setQuery(query.toString());
        ResultSet results = null;
        try {
            results = searchService.query(searchParams);
            if (results.getNodeRefs().size() > 1) {
                throw new RuntimeException("More then one person with same email found!");
            }
            if (results.getNodeRefs().size() < 1) {
                return Optional.empty();
            }
            return Optional.ofNullable(results.getRow(0).getQName().getLocalName());
        } finally {
            if (results != null) {
                results.close();
            }
        }
    }

    public void registerUserJsonDecorator(Consumer<JSONObject> decorator) {
        userJsonDecorators.add(decorator);
    }

    public void registerUserValidator(Consumer<UserSavingContext> validator) {
        userValidators.add(validator);
    }

    public void registerBeforeSaveAction(Consumer<UserSavingContext> beforeSaveAction) {
        beforeSaveActions.add(beforeSaveAction);
    }

    public void registerAfterSaveAction(Consumer<UserSavingContext> afterSaveAction) {
        afterSaveActions.add(afterSaveAction);
    }

    private void executeValidators(UserSavingContext context) {
        userValidators.forEach(validator -> validator.accept(context));
    }

    private void executeBeforeSaveActions(UserSavingContext context) {
        beforeSaveActions.forEach(action -> action.accept(context));
    }

    private void executeAfterSaveActions(UserSavingContext context) {
        afterSaveActions.forEach(action -> action.accept(context));
    }

    /**
     * add manager association to json.assoc.manager
     *
     * @return
     */
    private Consumer<JSONObject> getManagerAssocDecorator() {
        return json -> {
            getNodeRefFromUserJson(json).ifPresent(nodeRef -> {
                nodeService.getChildAssocs(nodeRef, Sets.newHashSet(OpenESDHModel.ASSOC_OE_MANAGER)).forEach(managerAssoc -> {
                    //create manager
                    JSONObject manager = new JSONObject();

                    PersonService.PersonInfo person = personService.getPerson(managerAssoc.getChildRef());
                    manager.put("nodeRef", nodeRef.toString());
                    manager.put("name", getPersonFullName(person));
                    manager.put("userName", person.getUserName());

                    //add to json
                    if (!json.containsKey("assoc")) {
                        json.put("assoc", new JSONObject());
                    }
                    JSONObject assoc = (JSONObject) json.get("assoc");
                    assoc.put("manager", manager);
                });
            });

        };
    }

    private String getPersonFullName(PersonService.PersonInfo person) {
        return Joiner.on(" ").skipNulls().join(person.getFirstName(), person.getLastName()).trim();
    }

    private Optional<NodeRef> getNodeRefFromUserJson(JSONObject json) {
        if (json.containsKey(NamespaceService.SYSTEM_MODEL_PREFIX)
                && json.get(NamespaceService.SYSTEM_MODEL_PREFIX) instanceof JSONObject) {
            JSONObject sys = (JSONObject) json.get(NamespaceService.SYSTEM_MODEL_PREFIX);
            if (sys.containsKey(ContentModel.PROP_NODE_UUID)) {
                NodeRef nodeRef = new NodeRef(
                        (String) sys.get(ContentModel.PROP_STORE_PROTOCOL.getLocalName()),
                        (String) sys.get(ContentModel.PROP_STORE_IDENTIFIER.getLocalName()),
                        (String) sys.get(ContentModel.PROP_NODE_UUID.getLocalName()));
                return Optional.of(nodeRef);
            }
        }
        return Optional.empty();
    }
}
