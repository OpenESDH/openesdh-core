package dk.openesdh.repo.services.activities;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.version.VersionServicePolicies.OnCreateVersionPolicy;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;

@Service("caseDocumentActivityBehaviour")
public class CaseDocumentActivityBehaviour implements OnCreateChildAssociationPolicy,
        OnCreateVersionPolicy {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("policyComponent")
    private PolicyComponent policyComponent;
    @Autowired
    @Qualifier("CaseActivityService")
    private CaseActivityService activityService;

    @PostConstruct
    public void init() {
        this.policyComponent.bindAssociationBehaviour(OnCreateChildAssociationPolicy.QNAME,
                OpenESDHModel.TYPE_DOC_SIMPLE, ContentModel.ASSOC_CONTAINS, new JavaBehaviour(this,
                        "onCreateChildAssociation", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        this.policyComponent.bindClassBehaviour(OnCreateVersionPolicy.QNAME, OpenESDHModel.TYPE_DOC_FILE,
                new JavaBehaviour(this, "onCreateVersion"));
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {
        if (!nodeService.exists(childAssocRef.getChildRef())) {
            return;
        }

        if (nodeService.countChildAssocs(childAssocRef.getParentRef(), true) == 1) {
            activityService.postOnCaseDocumentUpload(childAssocRef.getChildRef());
        } else {
            activityService.postOnCaseDocumentAttachmentUpload(childAssocRef.getChildRef());
        }
    }

    @Override
    public void onCreateVersion(QName classRef, NodeRef versionableNode,
            Map<String, Serializable> versionProperties, PolicyScope nodeDetails) {
        if (nodeService.hasAspect(versionableNode, OpenESDHModel.ASPECT_DOC_IS_MAIN_FILE)) {
            activityService.postOnCaseDocumentNewVersionUpload(versionableNode);
        } else {
            activityService.postOnCaseDocumentAttachmentNewVersionUpload(versionableNode);
        }
    }

}