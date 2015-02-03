package dk.openesdh.repo.services.contacts;

import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.NominatedInvitation;

/**
 * Created by lanre on 06/01/2015.
 */
public interface ContactInvitationService{
    public NominatedInvitation inviteContact(String inviteeFirstName, String inviteeLastName, String inviteeEmail,
                                              Invitation.ResourceType resourceType, String acceptUrl, String rejectUrl);




}
