package dk.openesdh.repo.services.cases;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.gdata.util.common.base.Joiner;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.members.CaseMembersService;

@Service("CaseOwnersService")
public class CaseOwnersServiceImpl implements CaseOwnersService {
    
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;
    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;
    @Autowired
    @Qualifier("CaseMembersService")
    private CaseMembersService caseMembersService;

    @Override
    public JSONArray getCaseOwners(NodeRef nodeRef) throws JSONException {
        JSONArray owners = new JSONArray();
        nodeService.getTargetAssocs(nodeRef, OpenESDHModel.ASSOC_CASE_OWNERS)
            .stream()
            .map(AssociationRef::getTargetRef)
            .map(this::getCaseOwnerJson)
            .forEach(owners::put);
        return owners;
    }

    private JSONObject getCaseOwnerJson(NodeRef nodeRef) {
        try {
            JSONObject owner = new JSONObject();
            owner.put("nodeRef", nodeRef.toString());
            QName type = nodeService.getType(nodeRef);
            if (type.isMatch(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
                owner.put("type", AuthorityType.GROUP);
                String groupName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHORITY_NAME);
                owner.put("name", groupName);
                owner.put("displayName", authorityService.getAuthorityDisplayName(groupName));
            } else if (type.isMatch(ContentModel.TYPE_PERSON)) {
                owner.put("type", AuthorityType.USER);
                PersonService.PersonInfo person = personService.getPerson(nodeRef);
                owner.put("name", person.getUserName());
                owner.put("displayName",
                        Joiner.on(" ").skipNulls().join(person.getFirstName(), person.getLastName()).trim());
            }
            return owner;
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Set<String> getCaseOwnersUserIds(NodeRef caseNodeRef) {
        return nodeService.getTargetAssocs(caseNodeRef, OpenESDHModel.ASSOC_CASE_OWNERS)
                .stream()
                .flatMap(assoc -> getUserIds(assoc.getTargetRef()).stream())
                .collect(Collectors.toSet());
    }
    
    private Set<String> getUserIds(NodeRef authorityNodeRef) {
        QName type = nodeService.getType(authorityNodeRef);
        if (type.isMatch(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
            String groupName = (String) nodeService.getProperty(authorityNodeRef, ContentModel.PROP_NAME);
            return authorityService.getContainedAuthorities(AuthorityType.USER, groupName, false);
        } else if (type.isMatch(ContentModel.TYPE_PERSON)) {
            return new HashSet<String>(Arrays.asList(personService.getPerson(authorityNodeRef).getUserName()));
        }
        return Collections.emptySet();
    }

    @Override
    public Set<String> getCaseOwnersAuthorityNames(NodeRef caseNodeRef) {
        return nodeService.getTargetAssocs(caseNodeRef, OpenESDHModel.ASSOC_CASE_OWNERS)
                .stream()
                .map(AssociationRef::getTargetRef)
                .map(caseMembersService::getAuthorityName)
                .collect(Collectors.toSet());
    }

}
