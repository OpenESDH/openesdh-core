package dk.openesdh.repo.services.activities;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;

@Service("caseActivityBehaviour")
public class CaseActivityBehaviour implements OnUpdatePropertiesPolicy{

    @Autowired
    @Qualifier("policyComponent")
    private PolicyComponent policyComponent;
    @Autowired
    @Qualifier("CaseActivityService")
    private CaseActivityService activityService;

    @PostConstruct
    public void init() {
        this.policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, OpenESDHModel.TYPE_CASE_BASE,
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        Optional.ofNullable(before.get(OpenESDHModel.PROP_OE_ID))
            .map(Object::toString)
            .ifPresent(caseId -> activityService.postOnCaseUpdate(nodeRef));
    }
}
