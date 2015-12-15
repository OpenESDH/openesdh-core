package dk.openesdh.repo.policy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Here we guard against duplicate user emails in the system. just like we need the user name to be unique so must
 * the email. Do not remove this as user creation might be circumvented by another method hence we need a catch all.
 *
 * @author Lanre
 */
@Service("userEmailCheckBehaviour")
public class CheckUserEmailBehaviour implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnUpdateNodePolicy {
    private static Log logger = LogFactory.getLog(CheckUserEmailBehaviour.class);

    //<editor-fold desc="Dependencies">
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;
    @Autowired
    @Qualifier("policyComponent")
    private PolicyComponent policyComponent;

    // Behaviours
    private Behaviour onCreateNode;
    private Behaviour onUpdateNode;
    //</editor-fold>

    @PostConstruct
    public void init() {

        // Create behaviours
        this.onCreateNode = new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.onUpdateNode = new JavaBehaviour(this, "onUpdateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ContentModel.TYPE_PERSON,
                this.onCreateNode);
        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdateNodePolicy.QNAME, ContentModel.TYPE_PERSON,
                this.onUpdateNode);
    }

    public void onCreateNode(ChildAssociationRef childAssocRef) {
        NodeRef personRef = childAssocRef.getChildRef();
        String email = this.nodeService.getProperty(personRef, ContentModel.PROP_EMAIL).toString();
        if(checkIfEmailExists(email))
            throw new AlfrescoRuntimeException("Error creating person: Email must be unique and already exists: " + email);
    }

    public void onUpdateNode(NodeRef personRef) {
        String email = this.nodeService.getProperty(personRef, ContentModel.PROP_EMAIL).toString();
        if(checkIfEmailExists(email))
            throw new AlfrescoRuntimeException("Error updating email: already exists.");
    }

    private boolean checkIfEmailExists(String email) {
        PagingRequest paging = new PagingRequest(5);
        List<QName> filterProps = new ArrayList<>();
        filterProps.add(ContentModel.PROP_EMAIL);
        PagingResults<PersonService.PersonInfo> persons = personService.getPeople(email, filterProps, null, paging);
        return persons.getPage().size() > 1;
    }
}