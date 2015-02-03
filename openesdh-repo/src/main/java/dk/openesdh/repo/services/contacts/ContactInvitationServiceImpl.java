package dk.openesdh.repo.services.contacts;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.invitation.InvitationServiceImpl;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.UrlUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Lanre Abiwon
 */
public class ContactInvitationServiceImpl extends InvitationServiceImpl implements ContactInvitationService {
    private static final Log logger = LogFactory.getLog(ContactInvitationServiceImpl.class);

    //Dependencies
    private SysAdminParams sysAdminParams;
    //private final String uiServerPath = UrlUtil.getShareUrl(sysAdminParams);
    //services
    AuthenticationService authenticationService;
    ContactService contactService;

    @Override
    public NominatedInvitation inviteContact(String inviteeFirstName, String inviteeLastName, String inviteeEmail, Invitation.ResourceType resourceType, String acceptUrl, String rejectUrl) {
        return null;
    }

    //<editor-fold desc="setters">
    @Override
    public void setSysAdminParams(SysAdminParams sysAdminParams) {
        this.sysAdminParams = sysAdminParams;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setContactService(ContactService contactService) {
        this.contactService = contactService;
    }
    //</editor-fold>

}
