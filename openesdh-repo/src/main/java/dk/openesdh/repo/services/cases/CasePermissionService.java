package dk.openesdh.repo.services.cases;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface CasePermissionService {

    public String getCaseReaderName(String caseType);

    public String getCaseReaderName(QName caseType);

    public String getCaseReaderName(NodeRef caseNodeRef);

    public String getCaseWriterName(String caseType);

    public String getCaseWriterName(QName caseType);

    public String getCaseWriterName(NodeRef caseNodeRef);

    public String getCaseOwnerName(String caseType);

    public String getCaseOwnerName(QName caseType);

    public String getCaseOwnerName(NodeRef caseNodeRef);
}
