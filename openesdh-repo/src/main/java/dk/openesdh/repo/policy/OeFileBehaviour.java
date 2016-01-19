package dk.openesdh.repo.policy;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;

@Service("oeFileBehaviour")
public class OeFileBehaviour implements OnCreateNodePolicy, OnCreateChildAssociationPolicy {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("OwnableService")
    private OwnableService ownableService;
    @Autowired
    @Qualifier("policyComponent")
    private PolicyComponent policyComponent;
    @Autowired
    @Qualifier("policyBehaviourFilter")
    private BehaviourFilter behaviourFilter;
    @Autowired
    @Qualifier("PermissionService")
    private PermissionService permissionService;

    private Behaviour onCreateNode;
    private Behaviour onCreateChildAssociation;

    @PostConstruct
    public void init() {
        // Create behaviours
        this.onCreateNode = new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.EVERY_EVENT);
        this.onCreateChildAssociation = new JavaBehaviour(this, "onCreateChildAssociation", Behaviour.NotificationFrequency.EVERY_EVENT);
        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, OpenESDHModel.TYPE_OE_AUTHORITY_FILES_FOLDER, this.onCreateNode);
        this.policyComponent.bindAssociationBehaviour(OnCreateChildAssociationPolicy.QNAME, OpenESDHModel.TYPE_OE_AUTHORITY_FILES_FOLDER, this.onCreateChildAssociation);
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {
        if (isNewNode) {
            behaviourFilter.disableBehaviour(childAssocRef.getChildRef());
        }
        String authorityName = (String) nodeService.getProperty(childAssocRef.getParentRef(), ContentModel.PROP_NAME);
        ownableService.setOwner(childAssocRef.getChildRef(), authorityName);
        if (isNewNode) {
            behaviourFilter.enableBehaviour(childAssocRef.getChildRef());
        }
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        String authorityName = (String) nodeService.getProperty(childAssocRef.getChildRef(), ContentModel.PROP_NAME);
        NodeRef userFolder = childAssocRef.getChildRef();
        ownableService.setOwner(userFolder, authorityName);

        permissionService.setInheritParentPermissions(userFolder, false);
        permissionService.setPermission(userFolder, authorityName, PermissionService.ALL_PERMISSIONS, true);
        permissionService.setPermission(userFolder, PermissionService.OWNER_AUTHORITY, PermissionService.ALL_PERMISSIONS, true);
    }
}
