package dk.openesdh.repo.policy;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.services.contacts.ContactService;


/**
 * @author Lanre Abiwon
 */
@Service("contact.group.policies")
public class ContactsGroupPolicy implements BeforeDeleteChildAssociationPolicy, OnCreateChildAssociationPolicy {

    /* logger */
    private static Log logger = LogFactory.getLog(ContactsGroupPolicy.class);

    /* Services */
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("ContactService")
    private ContactService contactService;
    @Autowired
    @Qualifier("policyComponent")
    private PolicyComponent policyComponent;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;

    private static final String contactsGroup = "GROUP_CONTACTS";

    /**
     * Spring bean init method
     */
    @PostConstruct
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
            //this.contactService.createContact(personNodeRef);
        }

    }
}