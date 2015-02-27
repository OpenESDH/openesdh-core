<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/case.js">

/**
 * Gets the nodeRef of the documents library for the case
 * (Allows us to use the doclib widget)
 * @param nodeRef
 * @returns {*}
 */
function getCaseDocumentNodeRef(nodeRef) {

    var caseDocNode = nodeRef.replace("://", "/");
    var connector = remote.connect("alfresco");
    var docNode = connector.get("/api/openesdh/caselib/docNode/" +caseDocNode);

    var caseDocNodeRef = eval('(' + docNode + ')');
    return caseDocNodeRef.node;
}

var caseId = url.templateArgs.caseId;
var caseNodeRef = getCaseNodeRefFromId(caseId);

var documentNode = getCaseDocumentNodeRef(caseNodeRef);
var services = [
    "alfresco/services/DocumentService",
    "alfresco/services/LightboxService",
    "alfresco/services/ContentService",
    "alfresco/dialogs/AlfDialogService",
    "openesdh/common/services/CaseActionService",
    "alfresco/services/RatingsService",
    "alfresco/services/QuickShareService",
    {
        name: "openesdh/common/services/CaseDocumentService",
        config: {
            documentsNodeRef: documentNode
        }
    }
];

model.jsonModel = {
    services: services,
    widgets: [
        {
            id: "SHARE_VERTICAL_LAYOUT",
            name: "alfresco/layout/VerticalWidgets",
            config:
            {
                //widgets: widgets
                widgets: [
                    {
                        name: "openesdh/common/widgets/dashlets/CaseDocumentsDashlet",
                        config: {
                            nodeRef: caseNodeRef
                        }
                    }
                ]
            }
        }
    ]
};
