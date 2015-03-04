define(["dojo/_base/declare"],
    function(declare) {

        return declare(null, {
            ReloadDocumentsTopic: "OE_RELOAD_DOCUMENTS",
            DocumentRowSelect: "DOCUMENT_ROW_SELECT",
            DocumentRowDeselect: "DOCUMENT_ROW_DESELECT",
            AttachmentGridRefresh:"REFRESH_ATTACHMENT_GRID",
            AttachmentRowSelect: "ATTACHMENTS_DOCUMENT_ROW_SELECT",
            AttachmentRowDeselect: "ATTACHMENTS_DOCUMENT_ROW_DESELECT",
            ReloadAttachmentsTopic: "OE_RELOAD_ATTACHMENTS",
            GetDocumentVersionsTopic: "GET_DOCUMENT_VERSIONS",
            VersionsGridRefresh:"VERSIONS_GRID_REFRESH"

        });
    });