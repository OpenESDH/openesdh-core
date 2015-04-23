<#include "/org/alfresco/components/form/controls/common/picker.inc.ftl" />

<#assign controlId = fieldHtmlId + "-cntrl">

<script type="text/javascript">//<![CDATA[
(function()
{
   <@renderPickerJS field "picker" />
   picker.setOptions(
   {
   <#if field.control.params.showTargetLink??>
      showLinkToTarget: ${field.control.params.showTargetLink},
      <#if page?? && page.url.templateArgs.site??>
         targetLinkTemplate: "${url.context}/page/site/${page.url.templateArgs.site!""}/document-details?nodeRef={nodeRef}",
      <#else>
         targetLinkTemplate: "${url.context}/page/document-details?nodeRef={nodeRef}",
      </#if>
   </#if>
   <#if field.control.params.allowNavigationToContentChildren??>
      allowNavigationToContentChildren: ${field.control.params.allowNavigationToContentChildren},
   </#if>
      itemType: "${field.control.params.itemType!"cm:content"}",
      multipleSelectMode: ${field.control.params.multipleSelectMode},
      parentNodeRef: "alfresco://company/home",
   <#if field.control.params.rootNode??>
      rootNode: "${field.control.params.rootNode}",
   </#if>
      itemFamily: "${field.control.params.itemFamily!"node"}",
      displayMode: "${field.control.params.displayMode!"items"}",
      maintainAddedRemovedItems: false
   });
   
   
   <#if field.control.params.linkPhaseParentWithCase??>
   ESDH.Helper.PhasePicker.setCasePicker(picker);
   </#if>
   
   <#if field.control.params.linkPackageParentWithCase??>
   ESDH.Helper.WorkflowPackagePicker.setCasePicker(picker);
   </#if>   

   <#if field.control.params.isfacetsearch??>
   ESDH.Helper.FacetPicker.setFacetPicker(picker);
      <#else>
        // NOFACETNEL
   </#if>


})();
//]]></script>

<div class="form-field">
   <#if form.mode == "view">
      <div id="${controlId}" class="viewmode-field">
         <#if (field.endpointMandatory!false || field.mandatory!false) && field.value == "">
            <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <span id="${controlId}-currentValueDisplay" class="viewmode-value current-values"></span>
      </div>
   <#else>
      <label for="${controlId}">${field.label?html}:<#if field.endpointMandatory!false || field.mandatory!false><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>      
      <div id="${controlId}" class="object-finder">         
         <div id="${controlId}-currentValueDisplay" class="current-values"></div>         
         <#if field.disabled == false>
            <input value="${field.value?html}" type="hidden" id="${fieldHtmlId}" name="${field.name}"  />
            <input type="hidden" id="${controlId}-added" name="${field.name}_added" />
            <input type="hidden" id="${controlId}-removed" name="${field.name}_removed" />
            <div id="${controlId}-itemGroupActions" class="show-picker"></div>         
            <@renderPickerHTML controlId />
         </#if>
      </div>
   </#if>
</div>
