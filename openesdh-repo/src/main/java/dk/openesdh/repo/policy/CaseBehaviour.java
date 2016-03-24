package dk.openesdh.repo.policy;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CasePermissionService;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.tenant.TenantOpeneModulesService;

/**
 * Created by torben on 19/08/14.
 */
@Service(CaseBehaviour.BEAN_ID)
public class CaseBehaviour implements OnCreateNodePolicy, BeforeCreateNodePolicy, OnUpdatePropertiesPolicy,
        OnUpdateNodePolicy {

    public static final String BEAN_ID = "caseBehaviour";

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    private CaseService caseService;
    @Autowired
    @Qualifier("CasePermissionService")
    private CasePermissionService casePermissionService;
    @Autowired
    @Qualifier("policyComponent")
    private PolicyComponent policyComponent;

    @Autowired
    @Qualifier("TenantOpeneModulesService")
    private TenantOpeneModulesService tenantOpeneModulesService;

    // Behaviours
    private Behaviour onCreateNode;
    private Behaviour beforeCreateNode;

    @PostConstruct
    public void init() {

        // Create behaviours
        this.onCreateNode = new JavaBehaviour(this, "onCreateNode",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.beforeCreateNode = new JavaBehaviour(this, "beforeCreateNode",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(BeforeCreateNodePolicy.QNAME, OpenESDHModel.TYPE_CASE_BASE,
                this.beforeCreateNode);

        this.policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, OpenESDHModel.TYPE_CASE_BASE,
                this.onCreateNode);

        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                OpenESDHModel.TYPE_CASE_BASE, new JavaBehaviour(this, "onUpdateProperties"));

        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdateNodePolicy.QNAME,
                OpenESDHModel.ASPECT_OE_JOURNALIZABLE, new JavaBehaviour(this, "onUpdateNode"));
    }

    @Autowired
    @Qualifier("CaseService")
    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        caseService.createCase(childAssociationRef);
    }

    @Override
    public void beforeCreateNode(NodeRef parentRef, QName assocTypeQName, QName assocQName, QName caseTypeQName) {
        tenantOpeneModulesService.checkCaseTypeModuleEnabled(caseTypeQName);
        casePermissionService.checkCaseCreatorPermissions(caseTypeQName);
    }

    @Override
    public void onUpdateNode(NodeRef nodeRef) {
        // Handle updating journalKeyIndexed and journalFacetIndexed
        // properties based on journalKey and journalFacet properties,
        // respectively.
        NodeRef journalKey = (NodeRef) nodeService.getProperty(nodeRef, OpenESDHModel.PROP_OE_JOURNALKEY);
        if (journalKey != null) {
            Map<QName, Serializable> properties = nodeService.getProperties(journalKey);
            nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_JOURNALKEY_INDEXED,
                    properties.get(ContentModel.PROP_NAME) + " " + properties.get(ContentModel.PROP_TITLE));
        } else {
            nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_JOURNALKEY_INDEXED, null);
        }
        NodeRef journalFacet = (NodeRef) nodeService.getProperty(nodeRef, OpenESDHModel.PROP_OE_JOURNALFACET);
        if (journalFacet != null) {
            Map<QName, Serializable> properties = nodeService.getProperties(journalFacet);
            nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_JOURNALFACET_INDEXED,
                    properties.get(ContentModel.PROP_NAME) + " " + properties.get(ContentModel.PROP_TITLE));
        } else {
            nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_JOURNALFACET_INDEXED, null);
        }
    }

    @Override
    public void onUpdateProperties(NodeRef caseNodeRef, Map<QName, Serializable> before,
            Map<QName, Serializable> after) {
        checkCaseStatus(caseNodeRef, before, after);
    }

    private void checkCaseStatus(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        String beforeStatus = (String) before.get(OpenESDHModel.PROP_OE_STATUS);
        if (beforeStatus == null) {
            return;
        }
        String afterStatus = (String) after.get(OpenESDHModel.PROP_OE_STATUS);
        if (beforeStatus.equals(afterStatus)) {
            return;
        }
        if (caseService.isCaseNode(nodeRef)) {
            throw new AlfrescoRuntimeException("Case status cannot be "
                    + "changed directly. Must call the CaseService" + ".changeCaseStatus method.");
        }
    }
}

