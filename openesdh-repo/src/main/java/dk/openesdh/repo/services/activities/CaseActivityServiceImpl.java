package dk.openesdh.repo.services.activities;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.members.CaseMembersService;

@Service("CaseActivityService")
public class CaseActivityServiceImpl implements CaseActivityService {
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;
    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;
    @Autowired
    @Qualifier("CaseMembersService")
    private CaseMembersService caseMembersService;
    @Autowired
    @Qualifier("DocumentService")
    private DocumentService documentService;
    @Autowired
    @Qualifier("activityService")
    private ActivityService activityService;

    @Override
    public void postOnCaseUpdate(NodeRef caseNodeRef) {
        postActivity(caseNodeRef, CaseActivityService.ACTIVITY_TYPE_CASE_UPDATE,
                () -> createNewActivity(caseNodeRef));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postOnCaseMemberRemove(String caseId, NodeRef authority, String role) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        postActivity(caseNodeRef, CaseActivityService.ACTIVITY_TYPE_CASE_MEMBER_REMOVE, () -> {
            JSONObject json = createNewActivity(caseNodeRef);
            json.put("role", role);
            json.put("member", getAuthorityDisplayName(authority));
            return json;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postOnCaseMemberAdd(String caseId, NodeRef authority, String role) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        postActivity(caseNodeRef, CaseActivityService.ACTIVITY_TYPE_CASE_MEMBER_ADD, () -> {
            JSONObject json = createNewActivity(caseNodeRef);
            json.put("role", role);
            json.put("member", getAuthorityDisplayName(authority));
            return json;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postOnCaseWorkflowStart(String caseId, String description) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        postActivity(caseNodeRef, CaseActivityService.ACTIVITY_TYPE_CASE_WORKFLOW_START, () -> {
            JSONObject json = createNewActivity(caseId, caseNodeRef);
            json.put("workflowDescription", description);
            return json;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postOnCaseWorkflowCancel(String caseId, String description) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        postActivity(caseNodeRef, CaseActivityService.ACTIVITY_TYPE_CASE_WORKFLOW_CANCEL, () -> {
            JSONObject json = createNewActivity(caseId, caseNodeRef);
            json.put("workflowDescription", description);
            return json;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postOnEndCaseWorkflowTask(String caseId, String description, Optional<String> taskOutcome) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        String activityType = CaseActivityService.ACTIVITY_TYPE_CASE_WORKFLOW_TASK_
                + taskOutcome.map(outcome -> outcome.toLowerCase()).orElse("end");
        postActivity(caseNodeRef, activityType, () -> {
            JSONObject json = createNewActivity(caseId, caseNodeRef);
            json.put("workflowDescription", description);
            return json;
        });
    }

    @Override
    public void postOnCaseDocumentUpload(NodeRef documentNodeRef) {
        postCaseDocumentActivity(documentNodeRef, CaseActivityService.ACTIVITY_TYPE_CASE_DOCUMENT_UPLOAD);
    }

    @Override
    public void postOnCaseDocumentNewVersionUpload(NodeRef documentNodeRef) {
        postCaseDocumentActivity(documentNodeRef,
                CaseActivityService.ACTIVITY_TYPE_CASE_DOCUMENT_NEW_VERSION_UPLOAD);
    }

    @Override
    public void postOnCaseDocumentAttachmentUpload(NodeRef attachmentNodeRef) {
        postCaseDocumentAttachmentActivity(attachmentNodeRef,
                CaseActivityService.ACTIVITY_TYPE_CASE_DOCUMENT_ATTACHMENT_UPLOAD);
    }

    @Override
    public void postOnCaseDocumentAttachmentNewVersionUpload(NodeRef attachmentNodeRef) {
        postCaseDocumentAttachmentActivity(attachmentNodeRef,
                CaseActivityService.ACTIVITY_TYPE_CASE_DOCUMENT_ATTACHMENT_NEW_VERSION_UPLOAD);
    }

    @SuppressWarnings("unchecked")
    private void postCaseDocumentActivity(NodeRef documentNodeRef, String activityType) {
        NodeRef caseNodeRef = documentService.getCaseNodeRef(documentNodeRef);
        postActivity(caseNodeRef, activityType, () -> {
            JSONObject json = createNewActivity(caseNodeRef);
            json.put("docRecordNodeRef", documentService.getDocRecordNodeRef(documentNodeRef).toString());
            json.put("docTitle", nodeService.getProperty(documentNodeRef, ContentModel.PROP_TITLE));
            return json;
        });
    }

    @SuppressWarnings("unchecked")
    private void postCaseDocumentAttachmentActivity(NodeRef attachmentNodeRef, String activityType) {
        NodeRef caseNodeRef = documentService.getCaseNodeRef(attachmentNodeRef);
        postActivity(caseNodeRef, activityType, () -> {
            NodeRef docRecord = documentService.getDocRecordNodeRef(attachmentNodeRef);
            JSONObject json = createNewActivity(caseNodeRef);
            json.put("docRecordNodeRef", documentService.getDocRecordNodeRef(attachmentNodeRef).toString());
            json.put("attachmentTitle", nodeService.getProperty(attachmentNodeRef, ContentModel.PROP_NAME));
            json.put("docTitle", nodeService.getProperty(docRecord, ContentModel.PROP_TITLE));
            return json;
        });
    }

    private JSONObject createNewActivity(NodeRef caseNodeRef) {
        return createNewActivity(caseService.getCaseId(caseNodeRef), caseNodeRef);
    }

    @SuppressWarnings("unchecked")
    private JSONObject createNewActivity(String caseId, NodeRef caseNodeRef) {
        PersonInfo currentUserInfo = personService.getPerson(personService.getPerson(AuthenticationUtil
                .getFullyAuthenticatedUser()));
        String currentUserDisplayName = currentUserInfo.getFirstName() + " " + currentUserInfo.getLastName();
        JSONObject json = new JSONObject();
        json.put("caseId", caseId);
        json.put("caseTitle", nodeService.getProperty(caseNodeRef, ContentModel.PROP_TITLE));
        json.put("modifier", AuthenticationUtil.getFullyAuthenticatedUser());
        json.put("modifierDisplayName", currentUserDisplayName);
        return json;
    }

    private String getAuthorityDisplayName(NodeRef authority) {
        if (caseMembersService.isAuthorityPerson(authority)) {
            PersonInfo person = personService.getPerson(authority);
            return person.getFirstName() + " " + person.getLastName();
        }
        return caseMembersService.getAuthorityName(authority);
    }

    private void postActivity(NodeRef caseNodeRef, String activityType, Supplier<JSONObject> activityJsonSupplier) {
        Set<String> usersToNotify = getUsersToNotify(caseNodeRef);
        if (usersToNotify.isEmpty()) {
            return;
        }
        String activity = activityJsonSupplier.get().toJSONString();
        usersToNotify.forEach(userId -> notifyUser(activityType, userId, activity));
    }
    
    private Set<String> getUsersToNotify(NodeRef caseNodeRef) {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        return caseService.getCaseOwnersUserIds(caseNodeRef)
                .stream()
                .filter(userId -> !userId.equals(currentUser))
                .collect(Collectors.toSet());
    }

    private void notifyUser(String activityType, String userId, String jsonData) {
        activityService.postActivity(activityType, null, null, jsonData, userId);
    }
}
