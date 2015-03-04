define(["dojo/_base/declare"],
    function(declare) {

        return declare(null, {
            CaseInfoTopic: "CASE_INFO",
            CaseHistoryTopic : "CASE_HISTORY",
            DocumentsTopic: "DOCUMENT_LIST",
            MainDocument: "MAIN_DOCUMENT_SUCCESS",
            CaseMembersList: "CASE_MEMBERS_LIST",
            CaseMembersListSuccess: "CASE_MEMBERS_LIST_SUCCESS",
            //These next 3 topics are also present in openesdh/common/widgets/dashlets/_DocumentTopicsMixin.js
            CaseDocumentRowSelect: "DOCUMENT_ROW_SELECT",
            CaseDocumentRowDeselect: "DOCUMENT_ROW_DESELECT",
            CaseDocumentReloadAttachmentsTopic: "OE_RELOAD_ATTACHMENTS",
            GetDocumentVersionsTopic: "GET_DOCUMENT_VERSIONS",
            GetDocumentVersionsTopicClick: "GET_DOCUMENT_VERSIONS_CLICK"
        });
    });