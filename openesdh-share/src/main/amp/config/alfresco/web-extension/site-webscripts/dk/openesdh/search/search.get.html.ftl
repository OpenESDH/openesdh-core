<@processJsonModel group="share"/>
<@markup id="css" >
<#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/object-finder/object-finder.css" group="share" />
</@>

<@markup id="js">
<#-- JavaScript Dependencies -->
  <@script type="text/javascript" src="${url.context}/res/modules/form/control-wrapper.js" group="share"></@script>
  <@script type="text/javascript" src="${page.url.context}/res/components/object-finder/object-finder.js" group="share"></@script>
  <@script type="text/javascript" src="${page.url.context}/res/components/form/date-range.js" group="share"></@script>
  <@script type="text/javascript" src="${page.url.context}/res/components/form/date.js" group="share"></@script>
</@>
