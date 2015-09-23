package dk.openesdh.repo.rootScopeExt;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

public class CaseUtils extends BaseScopableProcessorExtension{
    private CaseService caseService;
    private DocumentService documentService;
    private ServiceRegistry services;

    public ScriptNode getCaseInfo(String caseId ){
        NodeRef caseNodeRef = this.caseService.getCaseInfo(caseId).getNodeRef();

        ScriptNode oeCase = new ScriptNode(caseNodeRef, services, getScope());

        return oeCase;
    }

    public String getCaseId(ScriptNode nodeObj){
        String caseId="";

        NodeRef objNodeRef = nodeObj.getNodeRef();
        if(caseService.isCaseNode(objNodeRef) || caseService.isCaseDocNode(objNodeRef)) {
            NodeRef caseNodeRef = documentService.getCaseNodeRef(objNodeRef);
            caseId = caseService.getCaseId(caseNodeRef).toString();
        }
        return caseId;
    }

    //<editor-fold desc="injected bean setters">
    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setServices(ServiceRegistry services) {
        this.services = services;
    }
    //</editor-fold>

}
