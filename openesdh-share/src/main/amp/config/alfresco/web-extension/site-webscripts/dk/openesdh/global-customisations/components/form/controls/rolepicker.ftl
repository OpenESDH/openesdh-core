<#assign controlId = fieldHtmlId + "-cntrl">

<script type="text/javascript">//<![CDATA[
(function()
{
	var rp = new Alfresco.ESDHRolePicker('${controlId}', '${fieldHtmlId}', '${(field.control.params.multipleSelectMode!false)?string}').setMessages(${messages});
})();
//]]></script>

<div class="form-field">
   <#if form.mode == "view">
      <div id="${controlId}" class="viewmode-field">
         <#if field.endpointMandatory && field.value == "">
            <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <span id="${controlId}-displayValue" class=""></span>
      </div>
   <#else>
      <label for="${controlId}">${field.label?html}:<#if field.endpointMandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      
      <div id="${controlId}">
         
         <#if field.disabled == false>
<input type="hidden" id="${controlId}-added" name="${field.name}_added" value="" />
<input type="hidden" id="${controlId}-removed" name="${field.name}_removed" value="" />
<input type="hidden" id="${fieldHtmlId}" name="-" value="${field.value?html}" />


			<select name="${fieldHtmlId}" id="${fieldHtmlId}-select" disabled="disabled">
			    <option value="">-- ${msg('rolepicker.SelectRole')} --</option>
			</select><br />
			
			<div id="${controlId}-selectedItems"></div>
			
			<span id="${controlId}-notice"><i>Bemærk: Sag skal vælges før rolle kan vælges</i></span>
         </#if>
      </div>
   </#if>
</div>
