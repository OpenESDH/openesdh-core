package dk.openesdh.repo.policy;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Guard against the deletion of the admin user.
 *
 * @author Lanre
 */
@Service("beforeDeletePersonPolicy")
public class PreventAdminDeleteBehaviour implements NodeServicePolicies.BeforeDeleteNodePolicy {
    private static Log logger = LogFactory.getLog(PreventAdminDeleteBehaviour.class);

    //<editor-fold desc="Dependencies">
    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;

    @Autowired
    @Qualifier("policyComponent")
    private PolicyComponent policyComponent;
    //</editor-fold>

    @PostConstruct
    public void init() {

        //Change the notification frequency because "on transaction commit" (the default) means the node actually
        //does not exist and will throw an error.
        Behaviour beforeDeleteNode = new JavaBehaviour(this, "beforeDeleteNode", Behaviour.NotificationFrequency.EVERY_EVENT);

        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ContentModel.TYPE_PERSON,
                beforeDeleteNode);
    }

    /**
     * Called before a node is deleted and checks if the user name of the person being deleted is "admin". If so a
     * Runtime Exception is thrown.
     *
     * @param nodeRef the node reference
     */
    @Override
    public void beforeDeleteNode(NodeRef nodeRef) {
        PersonService.PersonInfo person = personService.getPerson(nodeRef);
        if(person.getUserName().equalsIgnoreCase("admin"))
            throw new AlfrescoRuntimeException("Deletion of primary admin user is not allowed.");
    }
}