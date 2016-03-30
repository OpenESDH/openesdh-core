package dk.openesdh.repo.rootScopeExt;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;

public class CaseUtils extends BaseScopableProcessorExtension {

    private CaseService caseService;
    private DocumentService documentService;
    private ServiceRegistry services;

    public ScriptNode getCaseInfo(String caseId) {
        NodeRef caseNodeRef = this.caseService.getCaseInfo(caseId).getNodeRef();

        ScriptNode oeCase = new ScriptNode(caseNodeRef, services, getScope());

        return oeCase;
    }

    public String getCaseId(ScriptNode nodeObj) {
        NodeRef objNodeRef = nodeObj.getNodeRef();
        if (caseService.isCaseNode(objNodeRef) || caseService.isCaseDocNode(objNodeRef)) {
            NodeRef caseNodeRef = documentService.getCaseNodeRef(objNodeRef);
            if (caseNodeRef == null) {
                return "";
            }
            return caseService.getCaseId(caseNodeRef);
        }
        return "";
    }

    public ScriptNode resolveCasesHomeNodeRef() {
        NodeRef nodeRef = caseService.getCasesRootNodeRef();
        return (ScriptNode) new ValueConverter().convertValueForScript(this.services, getScope(), null, nodeRef);
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
