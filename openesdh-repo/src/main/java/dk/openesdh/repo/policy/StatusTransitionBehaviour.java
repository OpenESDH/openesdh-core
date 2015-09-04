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
 * Makes sure that users cannot directly set case (TODO: or document) statuses.
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
                OpenESDHModel.ASPECT_OE_STATUS,
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
        if (caseService.isCaseNode(nodeRef)) {
            throw new AlfrescoRuntimeException("Case status cannot be " +
                    "changed directly. Must call the CaseService" +
                    ".switchStatus method.");
        }
        // TODO: Handle for documents as well
//        else if (caseService.isCaseDocNode(nodeRef)) {
//            throw new AlfrescoRuntimeException("Document status cannot be " +
//                    "changed directly. Must call the DocumentService" +
//                    ".switchStatus method.");
//        }
    }
}

