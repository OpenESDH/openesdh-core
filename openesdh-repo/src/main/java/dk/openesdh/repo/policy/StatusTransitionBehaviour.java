package dk.openesdh.repo.policy;

import dk.openesdh.repo.model.CaseStatus;
import dk.openesdh.repo.model.DocumentStatus;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Map;


/**
 * Makes sure that transitions between case or document statuses are valid.
 */
public class StatusTransitionBehaviour implements
        NodeServicePolicies.OnUpdatePropertiesPolicy {

    private static Log LOGGER = LogFactory.getLog(StatusTransitionBehaviour.class);

    // Dependencies
    private CaseService caseService;
    private PolicyComponent policyComponent;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                OpenESDHModel.TYPE_OE_BASE,
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        String beforeStatus = (String) before.get(OpenESDHModel.PROP_OE_STATUS);
        if (beforeStatus == null) {
            return;
        }
        String afterStatus = (String) after.get(OpenESDHModel.PROP_OE_STATUS);
        if (beforeStatus.equals(afterStatus)) {
            return;
        }

        if (!isValidStateTransition(nodeRef, beforeStatus, afterStatus)) {
            throw new AlfrescoRuntimeException(
                    "Invalid transition for status: {0} to {1}",
                    new String[]{beforeStatus, afterStatus});
        }
    }

    private boolean isValidStateTransition(NodeRef nodeRef, String before, String after) {
        if (caseService.isCaseNode(nodeRef)) {
            return CaseStatus.isValidTransition(before, after);
        } else if (caseService.isCaseDocNode(nodeRef)) {
            return DocumentStatus.isValidTransition(before, after);
        } else {
            return true;
        }
    }
}

