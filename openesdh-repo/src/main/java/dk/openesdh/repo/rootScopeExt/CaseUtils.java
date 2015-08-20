package dk.openesdh.repo.rootScopeExt;

import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

public class CaseUtils extends BaseScopableProcessorExtension{
    private CaseService caseService;
    private ServiceRegistry services;

    public ScriptNode getCaseInfo(String caseId ){
        NodeRef caseNodeRef = this.caseService.getCaseInfo(caseId).getNodeRef();

        ScriptNode oeCase = new ScriptNode(caseNodeRef,services, getScope());

        return oeCase;
    }

    //<editor-fold desc="injected bean setters">
    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setServices(ServiceRegistry services) {
        this.services = services;
    }
    //</editor-fold>

}
