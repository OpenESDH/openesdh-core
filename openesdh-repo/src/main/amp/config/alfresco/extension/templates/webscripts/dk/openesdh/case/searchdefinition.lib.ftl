<#macro classDefJSON classdef key>
<#escape x as jsonUtils.encodeJSONString(x)>
<#local classdefprefix=classdef.name.toPrefixString()?replace(":","_")>
            <#list propertydefs as propertydef>
<#--
            "${propertydef.name.toPrefixString()}": {
                "name": "${propertydef.name.toPrefixString()}",
                "title": "${propertydef.getTitle(messages)!""}",
                "description": "${propertydef.getDescription(messages)!""}",
                "dataType": <#if propertydef.dataType??>"${propertydef.dataType.name.toPrefixString()}"<#else>"<unknown>"</#if>,
                "defaultValue": <#if propertydef.defaultValue??>"${propertydef.defaultValue}"<#else>null</#if>,
                "multiValued": ${propertydef.multiValued?string},
                "mandatory": ${propertydef.mandatory?string},
                "enforced": ${propertydef.mandatoryEnforced?string},
                "protected": ${propertydef.protected?string},
                "indexed": ${propertydef.indexed?string},
                "url": "/api/classes/${classdefprefix}/property/${propertydef.name.toPrefixString()?replace(":","_")}",
                "constraints" :
                [
                    <#list propertydef.constraints as constraintdefs>
                {
                    "type" : "${constraintdefs.getConstraint().getType()}",
                    "parameters" :
                    [
                        <#assign params = constraintdefs.getConstraint().getParameters()>
                        <#assign keys = params?keys>
                        <#list keys as key>
                        {
                            "${key}" : <#rt><#if params[key]?is_enumerable>[<#list params[key] as mlist>"${mlist}"<#if mlist_has_next>,</#if></#list>]
                                        <#t><#else><#if params[key]?is_boolean>${params[key]?string}<#else>"${params[key]?string}"</#if></#if>
                        }
                            <#if key_has_next>,</#if>
                        </#list>
                    ]
                }<#if constraintdefs_has_next>,</#if>
                    </#list>
                ]
            }<#if propertydef_has_next>,</#if>
-->
            </#list>
</#escape>
</#macro>