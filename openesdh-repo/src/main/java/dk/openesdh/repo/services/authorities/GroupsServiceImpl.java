package dk.openesdh.repo.services.authorities;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.authorities.GroupsCsvParser.Group;
import dk.openesdh.repo.services.cases.CaseService;

@Service("GroupsService")
public class GroupsServiceImpl implements GroupsService {

    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;

    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    private TransactionRunner transactionRunner;

    @Override
    public boolean typeEqualsOpenEType(String type, String authorityName) throws InvalidNodeRefException {
        return type.equals(nodeService.getProperty(authorityService.getAuthorityNodeRef(authorityName),
                OpenESDHModel.PROP_OE_OPENE_TYPE));
    }

    @Override
    public boolean hasAspectTypeOPENE(String authorityName) {
        return nodeService.hasAspect(authorityService.getAuthorityNodeRef(authorityName),
                OpenESDHModel.ASPECT_OE_OPENE_TYPE);
    }

    @Override
    public void addAspectTypeOPENE(String fullName) throws InvalidNodeRefException, InvalidAspectException {
        NodeRef nodeRef = authorityService.getAuthorityNodeRef(fullName);
        Map<QName, Serializable> aspectProps = new HashMap<>();
        aspectProps.put(OpenESDHModel.PROP_OE_OPENE_TYPE, CREATED_ON_OPEN_E);
        nodeService.addAspect(nodeRef, OpenESDHModel.ASPECT_OE_OPENE_TYPE, aspectProps);
    }

    @Override
    public void uploadGroupsCSV(InputStream groupsCsv) throws IOException {
        Collection<QName> caseTypes = caseService.getRegisteredCaseTypes();
        List<String> validCaseTypes = caseTypes
            .stream()
            .map(QName::getPrefixString)
            .map(caseType -> caseType.replace(":case", ""))
            .collect(Collectors.toList());
        GroupsCsvParser groupsParser = new GroupsCsvParser(validCaseTypes);
        List<Group> groups = groupsParser.parse(groupsCsv);
        if (groups.isEmpty()) {
            return;
        }

        transactionRunner.runInTransaction(() -> {
            groups.stream().forEach(this::createGroupIfAbsent);
            groups.stream().forEach(this::manageMemberShips);
            return null;
        });
    }

    private void createGroupIfAbsent(Group group) {
        if (authorityService.authorityExists(PermissionService.GROUP_PREFIX + group.getShortName())) {
            return;
        }
        String fullName = authorityService.createAuthority(AuthorityType.GROUP, group.getShortName(),
                group.getDisplayName(),
                authorityService.getDefaultZones());
        addAspectTypeOPENE(fullName);
    }

    private void manageMemberShips(Group group) {
        String groupName = PermissionService.GROUP_PREFIX + group.getShortName();
        Set<String> currentParentGroups = authorityService.getContainingAuthorities(AuthorityType.GROUP, groupName, true);
        group.getMemberOfGroups()
            .stream()
            .map(parent -> PermissionService.GROUP_PREFIX + parent)
            .filter(parent -> !currentParentGroups.contains(parent))
            .forEach(parent -> {
                authorityService.addAuthority(parent, groupName);
            });
    }

    public Set<String> getCurrentUserGroups() {
        String name = AuthenticationUtil.getFullyAuthenticatedUser();
        Set<String> groups = authorityService.getContainingAuthorities(AuthorityType.GROUP, name, false)
                .stream()
                .filter(this::hasAspectTypeOPENE)
                .collect(Collectors.toSet());
        return groups;
    }
}
