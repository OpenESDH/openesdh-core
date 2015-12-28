package dk.openesdh.repo.actions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;

/**
 * Created by syastrov on 11/10/14.
 */
@Service("assign-case-id")
public class AssignCaseIdActionExecuter extends ActionExecuterAbstractBase {
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        if (!nodeService.exists(actionedUponNodeRef)) {
            return;
        }
        AuthenticationUtil.runAs(() -> {
            NodeRef caseNodeRef = caseService.getParentCase(actionedUponNodeRef);
            if (caseNodeRef != null) {
                String caseId = caseService.getCaseId(caseNodeRef);
                Map<QName, Serializable> properties = new HashMap<>();
                properties.put(OpenESDHModel.PROP_OE_CASE_ID, caseId);
                nodeService.addAspect(actionedUponNodeRef, OpenESDHModel.ASPECT_OE_CASE_ID, properties);
            }
            return null;
        }, AuthenticationUtil.getAdminUserName());

    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

    }
}
