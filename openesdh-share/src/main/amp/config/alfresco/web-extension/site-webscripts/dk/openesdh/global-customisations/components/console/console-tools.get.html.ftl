<@markup target="html" action="replace" id="openesdh-console-tools-override">
   <@uniqueIdDiv>
      <div id="${args.htmlid?html}-body" class="tool tools-link">
         <#assign listType = args.listType!"grouped"/>
         <#if listType == "simple">
         <ul class="simpleLink">
            <#list tools as group>
               <#list group as tool>
                  <li class="<#if tool.selected>selected</#if>">
                     <span><a href="${tool.id}" class="tool-link" title="${tool.description?html}">${tool.label?html}</a></span>
                  </li>
               </#list>
            </#list>
         </ul>
         <#elseif listType == "grouped">
         <h2>${msg("header.tools")}</h2>
         <ul class="toolLink">
           <#list tools as group>
             <#list group as tool>
               <#if tool.group == "">
                   <li class="<#if tool_index=0>first-link</#if><#if tool.selected> selected</#if>"><span><a href="${tool.id}" class="tool-link" title="${tool.description?html}">${tool.label?html}</a></span></li>
               </#if>
             </#list>
           </#list>
           <#list tools as group>
             <#list group as tool>
               <#if tool.group != "">
                 <#if tool_index = 0></ul><h3>${tool.groupLabel}</h3><ul class="toolLink"></#if>
                   <li class="<#if tool_index=0>first-link</#if><#if tool.selected> selected</#if>"><span><a href="${tool.id}" class="tool-link" title="${tool.description?html}">${tool.label?html}</a></span></li>
               </#if>
             </#list>
           </#list>
         </ul>
         </#if>
      </div>
   </@>
</@>
