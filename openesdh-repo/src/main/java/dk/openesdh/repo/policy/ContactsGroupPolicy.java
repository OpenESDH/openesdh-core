package dk.openesdh.repo.policy;

import dk.openesdh.repo.services.contacts.ContactService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Lanre Abiwon
 */
public class ContactsGroupPolicy implements BeforeDeleteChildAssociationPolicy, OnCreateChildAssociationPolicy {

    /* logger */
    private static Log logger = LogFactory.getLog(ContactsGroupPolicy.class);

    /* Services */
    private NodeService nodeService;
    private ContactService contactService;
    private PolicyComponent policyComponent;
    private AuthorityService authorityService;
    private String contactsGroup;

    /**
     * Spring bean init method
     */
    public void init() {
        PropertyCheck.mandatory(this, "authorityService", authorityService);
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "contactService", contactService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);

        this.policyComponent.bindAssociationBehaviour(BeforeDeleteChildAssociationPolicy.QNAME, ContentModel.TYPE_AUTHORITY_CONTAINER, ContentModel.ASSOC_MEMBER, new JavaBehaviour(this, "beforeDeleteChildAssociation"));
        this.policyComponent.bindAssociationBehaviour(OnCreateChildAssociationPolicy.QNAME, ContentModel.TYPE_AUTHORITY_CONTAINER, ContentModel.ASSOC_MEMBER, new JavaBehaviour(this, "onCreateChildAssociation"));
    }


    @Override
    public void beforeDeleteChildAssociation(ChildAssociationRef childAssociationRef) {
        NodeRef personNodeRef = childAssociationRef.getChildRef();
        String groupName = (String) nodeService.getProperty(childAssociationRef.getParentRef(), ContentModel.PROP_AUTHORITY_NAME);
        if( groupName.equals(contactsGroup) ){
            //If we delete we keep the contact but for now we leave this here in case we will need to do some processing
        }
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean b) {
        final NodeRef personNodeRef = childAssociationRef.getChildRef();
        String groupName = (String) nodeService.getProperty(childAssociationRef.getParentRef(), ContentModel.PROP_AUTHORITY_NAME);

        if( groupName.equals(contactsGroup) ){
            //create a contact for the person
            this.contactService.createContact(personNodeRef);
        }

    }

    public void setContactsGroup(String contactsGroup) {
        this.contactsGroup = contactsGroup;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param policyComponent the policyComponent to set
     */
    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setContactService(ContactService contactService) {
        this.contactService = contactService;
    }

    public void setAuthorityService (AuthorityService authorityService) {
        this.authorityService = authorityService;
    }





}