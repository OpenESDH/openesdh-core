package dk.openesdh.repo.services.cases;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface CaseTypeService {

    String getCaseType(NodeRef caseNodeRef);

    String getCaseType(QName typeQName);

    String getCaseTypeTitle(NodeRef caseNodeRef);

    String getCaseTypeTitle(QName typeQName);

}
