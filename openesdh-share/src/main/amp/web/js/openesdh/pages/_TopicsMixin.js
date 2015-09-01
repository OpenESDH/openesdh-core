define(["dojo/_base/declare"],
    function(declare) {

        return declare(null, {
            CreateCaseTopic:"OE_CREATE_CASE_TOPIC",
            CreateCaseSuccess:"OE_CREATE_CASE_SUCCESS",
            CaseReloadDocumentsTopic:"OE_RELOAD_DOCUMENTS",
            CaseInfoTopic: "CASE_INFO",
            CaseHistoryTopic : "CASE_HISTORY",
            DocumentsTopic: "DOCUMENT_LIST",
            MainDocument: "MAIN_DOCUMENT_SUCCESS",
            CaseMembersList: "CASE_MEMBERS_LIST",
            CaseRefreshDocInfoTopic: "REFRESH_DOC_RECORD_INFO",
            CaseMembersListSuccess: "CASE_MEMBERS_LIST_SUCCESS",
            ShowCreateCaseDialog: "OE_SHOW_CREATE_CASE_DIALOG",
            //These next 3 topics are also present in openesdh/common/widgets/dashlets/_DocumentTopicsMixin.js
            CaseDocumentRowSelect: "DOCUMENT_ROW_SELECT",
            CaseDocumentRowDeselect: "DOCUMENT_ROW_DESELECT",
            CaseDocumentReloadAttachmentsTopic: "OE_RELOAD_ATTACHMENTS",
            GetDocumentVersionsTopic: "GET_DOCUMENT_VERSIONS",
            DocumentVersionRevertTopic: "DOCUMENT_REVERT",
            DocumentVersionRevertFormSubmitTopic: "DOCUMENT_REVERT_FORM_SUBMIT",
            DocumentVersionUploaderTopic: "OE_SHOW_VERSION_UPLOADER",
            VersionReversionSuccess: "VERSION_REVERT_SUCCESS",
            GetDocumentVersionsTopicClick: "GET_DOCUMENT_VERSIONS_CLICK",
            CaseDocumentMoved : "OE_CASE_DOCUMENT_MOVED",
            MoveDocumentTopic: "OE_MOVE_DOC",
            CopyDocumentTopic: "OE_COPY_DOC",
            FindCaseDialogTopic : "OE_CREATE_FIND_CASE_DIALOG",
            NotesTopicsScope: "OPENESDH_NOTES_DASHLET",
            NoteCreatedTopic: "ALF_CRUD_CREATE_SUCCESS"
        });
    });