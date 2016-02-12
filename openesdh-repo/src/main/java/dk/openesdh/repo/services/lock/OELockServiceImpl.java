package dk.openesdh.repo.services.lock;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;

@Service("OELockService")
public class OELockServiceImpl implements OELockService {

    private final Logger logger = LoggerFactory.getLogger(OELockServiceImpl.class);

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("OwnableService")
    private OwnableService ownableService;
    @Autowired
    @Qualifier("PermissionService")
    private PermissionService permissionService;
    @Autowired
    @Qualifier("LockService")
    private LockService lockService;

    @Override
    public void lock(final NodeRef nodeRef, final boolean lockChildren) {
        if (nodeService.hasAspect(nodeRef, OpenESDHModel.ASPECT_OE_LOCKED)) {
            // Don't touch, already locked
            logger.warn("Node already has locked aspect when locking: {}", nodeRef);
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

            // Add a never-expiring lock as the system user
            // We have to temporarily switch the fully authenticated user,
            // because LockService uses that to determine the lock owner.
            String authenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
            try {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                lockService.lock(nodeRef, LockType.READ_ONLY_LOCK, 0);
            } finally {
                AuthenticationUtil.setFullyAuthenticatedUser(authenticatedUser);
            }

            if (lockChildren) {
                for (ChildAssociationRef childAssociationRef : nodeService.getChildAssocs(nodeRef)) {
                    lock(childAssociationRef.getChildRef(), true);
                }
            }
            return null;
        });
    }

    @Override
    public void lock(NodeRef nodeRef) {
        lock(nodeRef, false);
    }

    @Override
    public void unlock(final NodeRef nodeRef, boolean unlockChildren) {
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

            if (unlockChildren) {
                for (ChildAssociationRef childAssociationRef : nodeService.getChildAssocs(nodeRef)) {
                    unlock(childAssociationRef.getChildRef(), true);
                }
            }
            return null;
        });
    }

    @Override
    public void unlock(NodeRef nodeRef) {
        unlock(nodeRef, false);
    }

    @Override
    public boolean isLocked(NodeRef nodeRef) {
        return nodeService.hasAspect(nodeRef, OpenESDHModel.ASPECT_OE_LOCKED)
                && (lockService.getLockStatus(nodeRef) == LockStatus.LOCKED || lockService.getLockStatus(nodeRef) == LockStatus.LOCK_OWNER);
    }
}
