package dk.openesdh.repo.policy;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.CaseService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by torben on 19/08/14.
 */
public class JournalizeBehaviour implements NodeServicePolicies.OnAddAspectPolicy {

    private static Log LOGGER = LogFactory.getLog(JournalizeBehaviour.class);
    public static final String ADMIN_USER_NAME = "admin";

    // Dependencies

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    private PolicyComponent policyComponent;
    private LockService lockService;
    private PermissionService permissionService;
    private AuthorityService authorityService;
    private NodeService nodeService;

    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    private BehaviourFilter policyBehaviourFilter;

    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper) {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    private RetryingTransactionHelper retryingTransactionHelper;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }


    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }


    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }


    // Behaviours
    private Behaviour onAddAspect;

    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    public void init() {

        // Create behaviours
        this.onAddAspect = new JavaBehaviour(this, "onAddAspect", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
                OpenESDHModel.ASPECT_CASE_JOURNALIZED,
                this.onAddAspect
        );
    }

    @Override
    public void onAddAspect(final NodeRef nodeRef, QName qName) {



        policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
        nodeService.setProperty(nodeRef,ContentModel.PROP_CREATOR,"admin");
        policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);


        System.out.println("nodeRef: " + nodeRef);

        lockService.lock(nodeRef, LockType.READ_ONLY_LOCK, 0, true);
        LockStatus lockStatus = lockService.getLockStatus(nodeRef);
        System.out.println("lockstatus for case: " + lockStatus.toString());


        Set<String> settablePermissions = permissionService.getSettablePermissions(nodeRef);
        String caseId = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);







        for (Iterator<String> iterator = settablePermissions.iterator(); iterator.hasNext(); ) {
            String permission = iterator.next();
            System.out.println("permission" + permission);
            String groupSuffix = "case_" + caseId + "_" + permission;
            String groupName = authorityService.getName(AuthorityType.GROUP, groupSuffix);
            NodeRef groupNodeRef = authorityService.getAuthorityNodeRef(groupName);

            lockService.lock(groupNodeRef, LockType.READ_ONLY_LOCK, 0, true);

            lockStatus = lockService.getLockStatus(groupNodeRef);
            System.out.println("lockstatus: " + lockStatus.toString());


        }


    }
}
