package dk.openesdh.repo.webscripts.cases;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

public class CaseReadWriteRoles extends CaseRoles {

    @Override
    protected Set<String> getRoles(NodeRef caseNodeRef) {
        return caseService.getReadWriteRoles(caseNodeRef);
    }

}
