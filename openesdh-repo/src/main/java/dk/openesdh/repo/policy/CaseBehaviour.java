package dk.openesdh.repo.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;

/**
 * Created by torben on 19/08/14.
 */
@Service("caseBehaviour")
public class CaseBehaviour implements OnCreateNodePolicy, BeforeCreateNodePolicy, OnUpdatePropertiesPolicy {

    private static Log LOGGER = LogFactory.getLog(CaseBehaviour.class);
    private static final String ACTIVITY_TYPE_CASE_UPDATE = "dk.openesdh.case-update";

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;
    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;
    @Autowired
    @Qualifier("policyComponent")
    private PolicyComponent policyComponent;
    @Autowired
    @Qualifier("ActivityService")
    private ActivityService activityService;

    // Behaviours
    private Behaviour onCreateNode;
    private Behaviour beforeCreateNode;
    private Behaviour onUpdateProperties;

    @PostConstruct
    public void init() {

        // Create behaviours
        this.onCreateNode = new JavaBehaviour(this, "onCreateNode",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.beforeCreateNode = new JavaBehaviour(this, "beforeCreateNode",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

        this.onUpdateProperties = new JavaBehaviour(this, "onUpdateProperties",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(BeforeCreateNodePolicy.QNAME, OpenESDHModel.TYPE_CASE_BASE,
                this.beforeCreateNode);

        this.policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, OpenESDHModel.TYPE_CASE_BASE,
                this.onCreateNode);

        this.policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, OpenESDHModel.TYPE_CASE_BASE,
                this.onUpdateProperties);
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        caseService.createCase(childAssociationRef);
    }

    @Override
    public void beforeCreateNode(NodeRef parentRef, QName assocTypeQName, QName assocQName, QName caseTypeQName) {
        caseService.checkCaseCreatorPermissions(caseTypeQName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        String currentUser = AuthenticationUtil.getRunAsUser();
        Set<String> usersToNotify = caseService.getCaseOwnersUserIds(nodeRef)
                .stream()
                .filter(userId -> !userId.equals(currentUser))
                .collect(Collectors.toSet());

        if (usersToNotify.isEmpty()) {
            return;
        }

        PersonInfo currentUserInfo = personService.getPerson(personService.getPerson(currentUser));
        String currentUserDisplayName = currentUserInfo.getFirstName() + " " + currentUserInfo.getLastName();

        JSONObject json = new JSONObject();
        json.put("caseId", caseService.getCaseId(nodeRef));
        json.put("title", nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));
        json.put("modifier", currentUser);
        json.put("modifierDisplayName", currentUserDisplayName);

        String jsonData = json.toJSONString();
        usersToNotify.forEach(userId -> notifyUser(userId, jsonData));
    }
    private void notifyUser(String userId, String jsonData) {
        activityService.postActivity(ACTIVITY_TYPE_CASE_UPDATE, null, null, jsonData, userId);
    }
}

