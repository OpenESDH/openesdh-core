package dk.openesdh.repo.services.authorities;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.services.RunInTransactionAsAdmin;
import dk.openesdh.repo.services.authorities.UsersCsvParser.User;

@Service("UsersService")
public class UsersServiceImpl implements UsersService, RunInTransactionAsAdmin {

    private static final String ERROR_GENERAL = "person.err.userCSV.general";
    private static final String MSG_CREATED = "person.msg.userCSV.created";
    private static final String MSG_EXISTING = "person.msg.userCSV.existing";

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
    @Qualifier("TransactionService")
    private TransactionService transactionService;

    @Autowired
    @Qualifier("tenantService")
    private TenantService tenantService;

    @Override
    public JSONObject uploadUsersCsv(InputStream usersCsv) throws Exception {

        UsersCsvParser parser = new UsersCsvParser();
        List<User> users = parser.parse(usersCsv);
        JSONObject json = new JSONObject();
        json.put("totalUsers", users.size());
        json.put("addedUsers", 0);
        json.put("users", Collections.emptyList());
        if (users.isEmpty()) {
            return json;
        }

        try {
            return addUsers(users);
        } catch (Exception e) {
            throw new Exception(I18NUtil.getMessage(ERROR_GENERAL), e);
        }
    }

    private JSONObject addUsers(List<User> users) throws JSONException {

        List<Map<String, String>> uploadResults = new ArrayList<>();

        int addedUsers = runInTransaction(() -> {
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private String addUser(Map<QName, String> userProps) {
        String userName = userProps.get(ContentModel.PROP_USERNAME);
        userName = PersonServiceImpl.updateUsernameForTenancy(userName, tenantService);

        if (personService.personExists(userName)) {
            return MSG_EXISTING;
        }

        String password = userProps.get(ContentModel.PROP_PASSWORD);
        userProps.remove(ContentModel.PROP_PASSWORD);
        personService.createPerson((Map<QName, Serializable>) (Map) userProps);

        authenticationService.createAuthentication(userName, password.toCharArray());

        return MSG_CREATED;
    }

    private Map<String, String> getUploadStatus(Map<QName, String> props, String status) {
        Map<String, String> result = new HashMap<String, String>();
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
            .forEach(group -> authorityService.addAuthority(group,  userName));
    }

    @Override
    public TransactionService getTransactionService() {
        return transactionService;
    }

}
