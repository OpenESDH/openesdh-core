package dk.openesdh.repo.services.members;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.services.RunInTransactionAsAdmin;
import dk.openesdh.repo.services.cases.CaseService;

@Service("CaseMembersService")
public class CaseMembersServiceImpl implements CaseMembersService, RunInTransactionAsAdmin {
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("DictionaryService")
    private DictionaryService dictionaryService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;
    @Autowired
    @Qualifier("TransactionService")
    private TransactionService transactionService;
    @Autowired
    private CaseService caseService;

    @Override
    public Map<String, Set<String>> getMembersByRole(NodeRef caseNodeRef, boolean noExpandGroups,
            boolean includeOwner) {
        String caseId = caseService.getCaseId(caseNodeRef);
        Set<String> roles = includeOwner ? caseService.getAllRoles(caseNodeRef) : caseService.getRoles(caseNodeRef);
        return runInTransaction(() -> {
            return roles.stream()
                    .collect(Collectors.toMap(role -> role, (String role) -> {
                        String groupName = caseService.getCaseRoleGroupName(caseId, role);
                        return authorityService.getContainedAuthorities(null, groupName, noExpandGroups);
            }));
        });
    }

    @Override
    public void removeAuthorityFromRole(final String authorityName, final String role, final NodeRef caseNodeRef) {
        caseService.checkCanUpdateCaseRoles(caseNodeRef);
        runInTransactionAsAdmin(() -> {
            String caseId = caseService.getCaseId(caseNodeRef);
            String groupName = caseService.getCaseRoleGroupName(caseId, role);
            if (authorityService.authorityExists(groupName) && authorityService.authorityExists(authorityName)) {
                authorityService.removeAuthority(groupName, authorityName);
            }
            return null;
        });
    }

    @Override
    public void removeAuthorityFromRole(final NodeRef authorityNodeRef, final String role, final NodeRef caseNodeRef) {
        removeAuthorityFromRole(getAuthorityName(authorityNodeRef), role, caseNodeRef);
    }

    @Override
    public void addAuthorityToRole(final String authorityName, final String role, final NodeRef caseNodeRef) {
        caseService.checkCanUpdateCaseRoles(caseNodeRef);
        String caseId = caseService.getCaseId(caseNodeRef);
        String groupName = caseService.getCaseRoleGroupName(caseId, role);
        try {
            System.out.println("%%%%%% adding to group: " + groupName + " authority: " + authorityName);
            authorityService.addAuthority(groupName, authorityName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void addAuthorityToRole(final NodeRef authorityNodeRef, final String role, final NodeRef caseNodeRef) {
        addAuthorityToRole(getAuthorityName(authorityNodeRef), role, caseNodeRef);
    }

    @Override
    public void addAuthoritiesToRole(final List<NodeRef> authorities, final String role, final NodeRef caseNodeRef) {
        caseService.checkCanUpdateCaseRoles(caseNodeRef);

        runInTransactionAsAdmin(() -> {
            String caseId = caseService.getCaseId(caseNodeRef);
            final String groupName = caseService.getCaseRoleGroupName(caseId, role);
            if (!authorityService.authorityExists(groupName)) {
                return null;
            }

            for (NodeRef authorityNodeRef : authorities) {
                String authority = getAuthorityName(authorityNodeRef);
                if (authority != null) {
                    authorityService.addAuthority(groupName, authority);
                }
            }
            return null;
        });
    }

    @Override
    public void changeAuthorityRole(final String authorityName, final String fromRole, final String toRole,
            final NodeRef caseNodeRef) {
        caseService.checkCanUpdateCaseRoles(caseNodeRef);
        runInTransactionAsAdmin(() -> {
            removeAuthorityFromRole(authorityName, fromRole, caseNodeRef);
            addAuthorityToRole(authorityName, toRole, caseNodeRef);
            return null;
        });
    }

    @Override
    public List<Long> getCaseDbIdsWhereAuthorityHasRole(NodeRef authorityNodeRef, String role) {
        Pattern pattern = Pattern.compile("GROUP_case_\\d+-(\\d+)_" + role);

        Set<String> containingAuthorities = authorityService.getContainingAuthorities(null,
                getAuthorityName(authorityNodeRef), false);

        return containingAuthorities.stream()
                    .map(authority -> pattern.matcher(authority))
                    .filter(Matcher::matches)
                    .map(matcher -> Long.parseLong(matcher.group(1)))
                    .collect(Collectors.toList());
    }

    // Copied (almost directly) from AuthorityDAOImpl because it is not exposed
    // in the AuthorityService public API
    @Override
    public String getAuthorityName(NodeRef authorityRef) {
        if (!nodeService.exists(authorityRef)) {
            return null;
        }

        if (isAuthorityGroup(authorityRef)) {
            return (String) nodeService.getProperty(authorityRef, ContentModel.PROP_AUTHORITY_NAME);
        }

        if (isAuthorityPerson(authorityRef)) {
            return (String) nodeService.getProperty(authorityRef, ContentModel.PROP_USERNAME);
        }

        return null;
    }

    @Override
    public boolean isAuthorityGroup(NodeRef authorityRef) {
        return isAuthorityOfType(authorityRef, ContentModel.TYPE_AUTHORITY_CONTAINER);
    }

    @Override
    public boolean isAuthorityPerson(NodeRef authorityRef) {
        return isAuthorityOfType(authorityRef, ContentModel.TYPE_PERSON);
    }

    private boolean isAuthorityOfType(NodeRef authorityRef, QName type) {
        QName authorityType = nodeService.getType(authorityRef);
        return dictionaryService.isSubClass(authorityType, type);
    }

    @Override
    public TransactionService getTransactionService() {
        return transactionService;
    }
}
