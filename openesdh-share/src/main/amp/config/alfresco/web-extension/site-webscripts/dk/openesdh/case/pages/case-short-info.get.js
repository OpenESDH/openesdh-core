<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/case.js">

var parseQueryString = function(queryString) {
    var params = {}, queries, temp, i, l;
    queries = queryString.split("&");
    for (i = 0, l = queries.length; i < l; i++) {
        temp = queries[i].split('=');
        params[temp[0]] = temp[1];
    }
    return params;
}
var caseId = parseQueryString(url.queryString)["caseId"];
var documentNodeRef = parseQueryString(url.queryString)["documentNodeRef"];
var nodeRef = getCaseNodeRefFromId(caseId);

logger.log("caseId: " + caseId);
logger.log("nodeRef: " + nodeRef);

model.jsonModel = {
    widgets: [
        {
            name: "alfresco/layout/ClassicWindow",
            config: {
                widgets: [
                    {
                        name: "alfresco/layout/VerticalWidgets",
                        config: {
                            widgets: [
                                {
                                    name: "openesdh/pages/case/widgets/InfoWidget"
                                },
                                {
                                    name: "alfresco/navigation/Link",
                                    config: {
                                        label: msg.get("case-short-info.view-details.label"),
                                        templateString: "<a href='/share/page/oe/case/"+caseId+"/dashboard' target='_blank'>"+msg.get("case-short-info.view-details.label")+"</a>"
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        }
    ],
    services: [
        {
            name: "openesdh/common/services/CaseService",
            config:{
                caseId: caseId,
                nodeRef: nodeRef,
                documentNodeRef: documentNodeRef
            }
        }
    ]
};