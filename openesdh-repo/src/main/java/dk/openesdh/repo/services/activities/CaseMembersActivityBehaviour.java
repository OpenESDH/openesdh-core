package dk.openesdh.repo.services.activities;

import java.util.Optional;
import java.util.regex.Matcher;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteChildAssociationPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.members.CaseMembersService;

@Service("caseMembersActivityBehaviour")
public class CaseMembersActivityBehaviour implements OnCreateChildAssociationPolicy, OnDeleteChildAssociationPolicy {

    @Autowired
    @Qualifier(CaseService.BEAN_ID)
    private CaseService caseService;
    @Autowired
    @Qualifier("CaseMembersService")
    private CaseMembersService caseMembersService;
    @Autowired
    @Qualifier("policyComponent")
    private PolicyComponent policyComponent;
    @Autowired
    @Qualifier("CaseActivityService")
    private CaseActivityService activityService;

    @PostConstruct
    public void init() {
        this.policyComponent.bindAssociationBehaviour(OnCreateChildAssociationPolicy.QNAME,
                ContentModel.TYPE_AUTHORITY_CONTAINER, ContentModel.ASSOC_MEMBER, 
                new JavaBehaviour(this, "onCreateChildAssociation"));

        this.policyComponent.bindAssociationBehaviour(OnDeleteChildAssociationPolicy.QNAME,
                ContentModel.TYPE_AUTHORITY_CONTAINER, ContentModel.ASSOC_MEMBER, 
                new JavaBehaviour(this, "onDeleteChildAssociation"));
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {
        postActivity(childAssocRef, activityService::postOnCaseMemberAdd);
    }

    @Override
    public void onDeleteChildAssociation(ChildAssociationRef childAssocRef) {
        postActivity(childAssocRef, activityService::postOnCaseMemberRemove);
    }

    private void postActivity(ChildAssociationRef childAssocRef, CaseMemberActivity activity) {
        String groupName = caseMembersService.getAuthorityName(childAssocRef.getParentRef());
        Matcher matcher = CaseService.CASE_ROLE_GROUP_NAME_PATTERN.matcher(groupName);
        if (!matcher.matches()) {
            return;
        }
        String caseId = matcher.group(1);
        String role = matcher.group(2);
        
        Optional.ofNullable(caseService.getCaseById(caseId))
            .ifPresent(caseNodeRef -> activity.post(caseId, childAssocRef.getChildRef(), role));
    }

    private interface CaseMemberActivity {
        void post(String caseId, NodeRef authority, String role);
    }
}
