{
"data":
[
<#list workflowDefinitions as workflowDefinition>
    <@workflowDefinitionJSON workflowDefinition=workflowDefinition />
    <#if workflowDefinition_has_next>,</#if>
</#list>
]
}


<#-- Renders a workflow definition. -->
<#macro workflowDefinitionJSON workflowDefinition detailed=false>
    <#escape x as jsonUtils.encodeJSONString(x)>
    {
    "id" : "${workflowDefinition.id}",
    "url": "${workflowDefinition.url}",
    "name": "${workflowDefinition.name}",
    "title": "${workflowDefinition.title!""}",
    "description": "${workflowDefinition.description!""}",
    "version": "${workflowDefinition.version}"
        <#if detailed>,
        "startTaskDefinitionUrl": "${workflowDefinition.startTaskDefinitionUrl}",
        "startTaskDefinitionType": "${shortQName(workflowDefinition.startTaskDefinitionType)}",
        "taskDefinitions":
        [
            <#list workflowDefinition.taskDefinitions as taskDefinition>
            {
            "url": "${taskDefinition.url}",
            "type": "${shortQName(taskDefinition.type)}"
            }
                <#if taskDefinition_has_next>,</#if>
            </#list>
        ]
        </#if>
    }
    </#escape>
</#macro>