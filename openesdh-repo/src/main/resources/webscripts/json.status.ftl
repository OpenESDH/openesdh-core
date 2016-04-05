{
    "status" : {
        "code" : ${status.code},
        "name" : "${status.codeName}",
        "description" : "${status.codeDescription}"
    },
    "error" : {
        <#if status.exception??>
            <#if status.exception.domain??>
                "domain" : true,
                "code" : "${jsonUtils.encodeJSONString(status.message)}"
            <#else>
                "domain" : false,
                "message" : <@recursestack exception=status.exception/>
            </#if>
            <#if status.exception.props??>
                ,"props": ${jsonUtils.encodeJSONString(status.exception.props)}
            </#if>
        <#else>
            "domain" : false,
            "message" : "${jsonUtils.encodeJSONString(status.message)}"
        </#if>
    }
}

<#macro recursestack exception>
    <#if exception.cause??>
       <@recursestack exception=exception.cause/>
    </#if>
    <#if !exception.cause??>
       "${jsonUtils.encodeJSONString(exception.message)}"
    </#if>
</#macro>