package dk.openesdh.repo.services;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Created by syastrov on 9/9/15.
 *
 * @param <STATUS> HasStatus.StatusChoise
 */
public interface HasStatus<STATUS extends Enum<STATUS>> {

    /**
     * Get the node's status.
     *
     * @param nodeRef
     * @return String
     */
    STATUS getNodeStatus(NodeRef nodeRef);

    /**
     * Return a list of valid next statuses for the node.
     *
     * These are statuses that the user is permitted to set the node to
     * based on the user's permissions, the current status, and the valid
     * status transitions.
     *
     * @param nodeRef
     * @return
     */
    List<STATUS> getValidNextStatuses(NodeRef nodeRef);

    /**
     * Return whether the user can change the node status from a given
     * status to another status.
     *
     * @param fromStatus
     * @param toStatus
     * @param user
     * @param nodeRef
     * @return
     */
    boolean canChangeNodeStatus(STATUS fromStatus, STATUS toStatus, String user, NodeRef nodeRef);

    /**
     * Change the node to a new status.
     *
     * An exception will be thrown if this is not allowed because the user
     * does not have the permission or if it is an invalid status transition.
     *
     * @param nodeRef
     * @param newStatus
     * @throws Exception
     */
    void changeNodeStatus(NodeRef nodeRef, STATUS newStatus) throws Exception;
}
