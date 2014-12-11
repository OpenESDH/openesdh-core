<#import "case.lib.ftl" as itemLib />
<#assign workingCopyLabel = " " + message("coci_service.working_copy_label")>
<#escape x as jsonUtils.encodeJSONString(x)>
{
"totalRecords": ${doclist.paging.totalRecords?c},
    <#if doclist.paging.totalRecordsUpper??>
    "totalRecordsUpper": ${doclist.paging.totalRecordsUpper?c},
    </#if>
"startIndex": ${doclist.paging.startIndex?c},
"metadata":
{
"repositoryId": "${server.id}",
    <#if (doclist.container.nodeRef)??>"container": "${doclist.container.nodeRef}",</#if>
    <#if (doclist.parent.nodeJSON)??>"parent": <#noescape>${doclist.parent.nodeJSON},</#noescape></#if>
    <#if doclist.customJSON??>"custom": <#noescape>${doclist.customJSON},</#noescape></#if>
"onlineEditing": ${doclist.onlineEditing?string},
"itemCounts":
{
"folders": ${(doclist.itemCount.folders!0)?c},
"documents": ${(doclist.itemCount.documents!0)?c}
},
"workingCopyLabel": "${workingCopyLabel}"
},
"items":
[
    <#list doclist.items as item>
        {
        "node": <#noescape>${item.nodeJSON}</#noescape>,
            <#if item.parent??>"parent": <#noescape>${item.parent.nodeJSON},</#noescape></#if>
            <@itemLib.itemJSON item=item />
        }<#if item_has_next>,</#if>
        <#--</#if>-->
    </#list>
],
"caseCardView" : ${doclist.caseCardView?c}<#if doclist.caseCardView>,
"mainDocument": {
        "node": <#noescape>${doclist.mainDocument.nodeJSON}</#noescape>,
        <#if doclist.mainDocument.parent??>"parent": <#noescape>${doclist.mainDocument.parent.nodeJSON},</#noescape></#if>
        <@itemLib.itemJSON item=doclist.mainDocument />
        }</#if><#if doclist.caseDetails?? && doclist.caseDetails?has_content>,
    "caseDetails" : {
        "caseOwner"    : "${doclist.caseDetails.owner?string}",
        "responsible"  : "${doclist.caseDetails.responsible?string}",
        "createdOn"    : "${xmldate(doclist.caseDetails.createdOn)}",
        "caseDocType"  : "${doclist.caseDetails.caseDocType?string}",
        "attachements" : "${doclist.caseDetails.attachements?c}"
    }
</#if>
}
</#escape>