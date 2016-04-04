{
    "status" : {
        "code" : ${status.code},
        "name" : "${status.codeName}",
        "description" : "${status.codeDescription}"
    },
    "error" : {
        <#if status.exception.domain??>
        "domain" : true,
        "code" : "${jsonUtils.encodeJSONString(status.message)}"
        <#else>
        "domain" : false,
        "code" : "${jsonUtils.encodeJSONString('ERROR.UNEXPECTED_ERROR')}"
        </#if>
    }
}