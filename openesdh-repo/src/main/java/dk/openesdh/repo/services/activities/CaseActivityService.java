package dk.openesdh.repo.services.activities;

import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;

public interface CaseActivityService {

    static final String ACTIVITY_TYPE_CASE_UPDATE = "dk.openesdh.case-update";

    static final String ACTIVITY_TYPE_CASE_MEMBER_ADD = "dk.openesdh.case.member-add";

    static final String ACTIVITY_TYPE_CASE_MEMBER_REMOVE = "dk.openesdh.case.member-remove";

    static final String ACTIVITY_TYPE_CASE_WORKFLOW_START = "dk.openesdh.case.workflow-start";

    static final String ACTIVITY_TYPE_CASE_WORKFLOW_CANCEL = "dk.openesdh.case.workflow-cancel";

    static final String ACTIVITY_TYPE_CASE_WORKFLOW_TASK_ = "dk.openesdh.case.workflow.task-";

    static final String ACTIVITY_TYPE_CASE_WORKFLOW_TASK_END = "dk.openesdh.case.workflow.task-end";

    static final String ACTIVITY_TYPE_CASE_WORKFLOW_TASK_APPROVE = "dk.openesdh.case.workflow.task-approve";

    static final String ACTIVITY_TYPE_CASE_WORKFLOW_TASK_REJECT = "dk.openesdh.case.workflow.task-reject";

    static final String ACTIVITY_TYPE_CASE_DOCUMENT_UPLOAD = "dk.openesdh.case.document-upload";

    static final String ACTIVITY_TYPE_CASE_DOCUMENT_NEW_VERSION_UPLOAD = "dk.openesdh.case.document.new.version-upload";

    static final String ACTIVITY_TYPE_CASE_DOCUMENT_ATTACHMENT_UPLOAD = "dk.openesdh.case.document.attachment-upload";

    static final String ACTIVITY_TYPE_CASE_DOCUMENT_ATTACHMENT_NEW_VERSION_UPLOAD = "dk.openesdh.case.document.attachment.new.version-upload";
    /**
     * Creates activity feed on case update
     * 
     * @param caseNodeRef
     *            nodeRef of the updated case
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
}
