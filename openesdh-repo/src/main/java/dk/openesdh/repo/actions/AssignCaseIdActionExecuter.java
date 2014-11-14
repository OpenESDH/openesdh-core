package dk.openesdh.repo.actions;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by syastrov on 11/10/14.
 */
public class AssignCaseIdActionExecuter extends ActionExecuterAbstractBase {
    private static Logger LOGGER = Logger.getLogger(AssignCaseIdActionExecuter.class.toString());

    public static final String NAME = "assign-case-id";

    private NodeService nodeService;
    private CaseService caseService;

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        NodeRef caseNodeRef = caseService.getParentCase(actionedUponNodeRef);
        if (caseNodeRef != null) {
            String caseId = caseService.getCaseId(caseNodeRef);
            Map<QName, Serializable> properties = new HashMap<>();
            properties.put(OpenESDHModel.PROP_OE_CASE_ID, caseId);
            nodeService.addAspect(actionedUponNodeRef,
                    OpenESDHModel.ASPECT_OE_CASE_ID, properties);
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }
}
