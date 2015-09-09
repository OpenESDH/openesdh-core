package dk.openesdh.repo.services.lock;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Created by syastrov on 9/9/15.
 */
public interface OELockService {
    /**
     * Lock a node, with the system user as the lock owner, and applies the
     * oe:locked aspect.
     *
     * Also denies all permissions in the LockPermissionsToDeny permission
     * group, which includes the right to ChangePermissions, among other
     * things.
     *
     * The owner of the node is set to the system user, to prevent the
     * node owner (via the dynamic ROLE_OWNER permission) from being able to
     * bypass these permissions.
     *
     * @param nodeRef
     */
    void lock(NodeRef nodeRef);

    /**
     * Unlock the node, restoring the original owner of the node, removing
     * the LockPermissionsToDeny permission, and removing the oe:locked
     * aspect.
     * @param nodeRef
     */
    void unlock(NodeRef nodeRef);
}
