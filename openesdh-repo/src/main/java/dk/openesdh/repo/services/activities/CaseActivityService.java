package dk.openesdh.repo.services.activities;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONObject;

public interface CaseActivityService {

    String BEAN_ID = "CaseActivityService";

    String ACTIVITY_TYPE_CASE_UPDATE = "dk.openesdh.case-update";

    String ACTIVITY_TYPE_CASE_MEMBER_ADD = "dk.openesdh.case.member-add";

    String ACTIVITY_TYPE_CASE_MEMBER_REMOVE = "dk.openesdh.case.member-remove";

    String ACTIVITY_TYPE_CASE_WORKFLOW_START = "dk.openesdh.case.workflow-start";

    String ACTIVITY_TYPE_CASE_WORKFLOW_CANCEL = "dk.openesdh.case.workflow-cancel";

    String ACTIVITY_TYPE_CASE_WORKFLOW_TASK_ = "dk.openesdh.case.workflow.task-";

    String ACTIVITY_TYPE_CASE_WORKFLOW_TASK_END = "dk.openesdh.case.workflow.task-end";

    String ACTIVITY_TYPE_CASE_WORKFLOW_TASK_APPROVE = "dk.openesdh.case.workflow.task-approve";

    String ACTIVITY_TYPE_CASE_WORKFLOW_TASK_REJECT = "dk.openesdh.case.workflow.task-reject";

    String ACTIVITY_TYPE_CASE_DOCUMENT_UPLOAD = "dk.openesdh.case.document-upload";

    String ACTIVITY_TYPE_CASE_DOCUMENT_NEW_VERSION_UPLOAD = "dk.openesdh.case.document.new.version-upload";

    String ACTIVITY_TYPE_CASE_DOCUMENT_ATTACHMENT_UPLOAD = "dk.openesdh.case.document.attachment-upload";

    String ACTIVITY_TYPE_CASE_DOCUMENT_ATTACHMENT_NEW_VERSION_UPLOAD = "dk.openesdh.case.document.attachment.new.version-upload";

    String ACTIVITY_TYPE = "activityType";

    String MODIFIER = "modifier";

    String MODIFIER_DISPLAY_NAME = "modifierDisplayName";

    String CASE_TITLE = "caseTitle";

    String CASE_ID = "caseId";

    String MEMBER = "member";

    String ROLE = "role";

    String DOC_RECORD_NODE_REF = "docRecordNodeRef";

    String DOC_TITLE = "docTitle";

    String ATTACHMENT_TITLE = "attachmentTitle";

    String WORKFLOW_DESCRIPTION = "workflowDescription";

    /**
     * Creates activity feed on case update
     *
     * @param caseNodeRef
     * nodeRef of the updated case
     */
    void postOnCaseUpdate(NodeRef caseNodeRef);

    void postOnCaseMemberRemove(String caseId, NodeRef authority, String role);

    void postOnCaseMemberAdd(String caseId, NodeRef authority, String role);

    void postOnCaseWorkflowStart(String caseId, String description);

    void postOnCaseWorkflowCancel(String caseId, String description);

    void postOnEndCaseWorkflowTask(String caseId, String description, Optional<String> taskOutcome);

    void postOnCaseDocumentUpload(NodeRef documentNodeRef);

    void postOnCaseDocumentAttachmentUpload(NodeRef attachmentNodeRef);

    void postOnCaseDocumentNewVersionUpload(NodeRef documentNodeRef);

    void postOnCaseDocumentAttachmentNewVersionUpload(NodeRef attachmentNodeRef);

    PagingResults<ActivityFeedEntity> getCurrentUserActivities(PagingRequest paging);

    int countCurrentUserNewActivities();

    void setCurrentUserLastReadActivityFeedId(String feedId);

    void postActivity(String caseId, String activityType, Function<NodeRef, JSONObject> activityJsonFunction);

    JSONObject createNewActivity(String caseId, NodeRef caseNodeRef);

    void notifyUser(String activityType, String userId, String jsonData);

    Set<String> getCaseMembersToNotify(String caseId, NodeRef caseNodeRef);

}
