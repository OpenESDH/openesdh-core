<@markup id="css" >
<#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/object-finder/object-finder.css" group="console" />
</@>

<@markup id="js">
<#-- JavaScript Dependencies -->
<#-- Admin Console Tag Management Tool -->
  <@script type="text/javascript" src="${page.url.context}/components/console/consoletool.js" group="console"></@script>
  <@script type="text/javascript" src="${url.context}/res/modules/form/control-wrapper.js" group="console"></@script>
</@>

<@markup id="widgets">
  <@createWidgets group="console"/>
  <@processJsonModel group="console" rootModule="alfresco/core/Page"/>
</@>

<@markup id="html">
<!-- todo use component's id here so we get a unique id -->
<div id="${args.htmlid?html}"></div>
</@>
