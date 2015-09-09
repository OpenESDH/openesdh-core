package dk.openesdh.repo.services.lock;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OELockServiceImpl implements OELockService {
    private static Logger LOGGER = Logger.getLogger(OELockServiceImpl.class);

    protected NodeService nodeService;
    protected OwnableService ownableService;
    protected PermissionService permissionService;
    protected LockService lockService;
    protected TransactionService transactionService;

    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setOwnableService(OwnableService ownableService) {
        this.ownableService = ownableService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void lock(final NodeRef nodeRef) {
        if (nodeService.hasAspect(nodeRef, OpenESDHModel.ASPECT_OE_LOCKED)) {
            // Don't touch, already locked
            LOGGER.warn("Node already has locked aspect when locking: " + nodeRef);
            return;
        }
        AuthenticationUtil.runAsSystem(() -> {
            // Set the owner to be the System user to prevent the
            // original owner from modifying the node
            String originalOwner = null;
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE)) {
                originalOwner = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_OWNER);
            }
            ownableService.setOwner(nodeRef, AuthenticationUtil.getSystemUserName());

            // Add locked aspect
            Map<QName, Serializable> props = new HashMap<>();
            props.put(OpenESDHModel.PROP_OE_LOCKED_BY, AuthenticationUtil.getFullyAuthenticatedUser());
            props.put(OpenESDHModel.PROP_OE_LOCKED_DATE, new Date());

            // Save the original owner, or null if there wasn't any
            props.put(OpenESDHModel.PROP_OE_ORIGINAL_OWNER, originalOwner);
            nodeService.addAspect(nodeRef, OpenESDHModel.ASPECT_OE_LOCKED, props);

            // Add the LockPermissionsToDeny permission set to deny everyone
            permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, "LockPermissionsToDeny", false);

            // Add a never-expiring close as the system user
            lockService.lock(nodeRef, LockType.READ_ONLY_LOCK, 0);
            return null;
        });
    }

    @Override
    public void unlock(final NodeRef nodeRef) {
        AuthenticationUtil.runAsSystem(() -> {
            lockService.unlock(nodeRef);

            // Only delete the permission if permissions are not
            // inherited (shared)
            if (!permissionService.getInheritParentPermissions(nodeRef)) {
                permissionService.deletePermission(nodeRef, PermissionService.ALL_AUTHORITIES, "LockPermissionsToDeny");
            }

            String originalOwner = (String) nodeService.getProperty(nodeRef, OpenESDHModel.PROP_OE_ORIGINAL_OWNER);
            if (originalOwner != null) {
                // Restore the node's original owner
                ownableService.setOwner(nodeRef, originalOwner);
            } else {
                // Remove the ownable aspect, since it wasn't there to
                // begin with
                nodeService.removeAspect(nodeRef, ContentModel.ASPECT_OWNABLE);
            }
            nodeService.removeAspect(nodeRef, OpenESDHModel.ASPECT_OE_LOCKED);
            return null;
        });
    }
}