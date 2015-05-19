<#assign el=args.htmlid?js_string>
<script type="text/javascript">//<![CDATA[
new Alfresco.component.TaskDetailsHeader("${el}").setOptions(
{
   referrer: <#if page.url.args.referrer??>"${page.url.args.referrer?js_string}"<#else>null</#if>,
   nodeRef: <#if page.url.args.nodeRef??>"${page.url.args.nodeRef?js_string}"<#else>null</#if>
}).setMessages(
   ${messages}
);
//]]></script>
<div id="${el}-body" class="form-manager task-details-header">
   <div class="links hidden">
      <span class="theme-color-2">${msg("label.taskDetails")}</span>
      <span class="separator">|</span>
      <a href="">${msg("label.workflowDetails")}</a>
   </div>
   
  <div id="${el}-backlink-container" style="display: none; margin: 0 0 1em;" class="caselink">
      <span class="yui-button">
        <span class="first-child">
          <a class="caselink-link" href="#" id="${el}-backlink" style="background: url('../documentlibrary/actions/case-view-details-16.png') no-repeat scroll 5px 5px transparent; padding-left: 25px;">
            <span>${msg("button.goback")}</span>
          </a>
        </span>
      </span>
  </div>
   
   <h1>${msg("header")}: <span></span></h1>
   <div class="clear"></div>
</div>
<script type="text/javascript">//<![CDATA[
YAHOO.Bubbling.on("taskDetailedData", function(layer, args) {
   var bl = document.getElementById("${el}-backlink");
   var blc = document.getElementById("${el}-backlink-container")
   if(bl && blc) {
      if(document.referrer && document.referrer.match(/\/case-view/)) {
         blc.style.display = 'block';
         bl.href = document.referrer;
      }
   }
});
//]]></script>
