package dk.openesdh.share.evaluator

import java.util.Map

import com.typesafe.scalalogging.slf4j.StrictLogging
import org.apache.commons.lang.StringUtils
import org.json.{JSONException, JSONObject}
import org.springframework.extensions.surf.support.ThreadLocalRequestContext
import org.springframework.extensions.surf.{RequestContext, WebFrameworkServiceRegistry}
import org.springframework.extensions.webscripts.ScriptRemote
import org.springframework.extensions.webscripts.connector.Response

/**
 * Utility class for evaluators to pick values from the request and get case information etc.
 * Based on Erik Winlof's SlingshotEvaluatorUtil.java ootb share code
 *
 * @author Lanre Abiwon
 */
object CaseEvaluatorUtil{
  val CASE_STATUS: String = "caseStatus"
  val CASE_ID = "caseId"
  //Added this to catch nodeRefs in the pages where caseIDs aren't possible
  //e.g when we need to create content(create-content page) or view document details
  val CASE_NODEREF = "nodeRef"
  val DESTINATION = "destination"
}

class CaseEvaluatorUtil(val serviceRegistry: WebFrameworkServiceRegistry) extends StrictLogging{

  /**
   * Helper for getting an evaluator parameter trimmed OR defaultValue if no value has been provided.
   *
   * @param params
   * @param name
   * @param defaultValue
   * @return A trimmed evaluator parameter OR defaultValue if no value has been provided.
   */
  def getEvaluatorParam(params: Map[String, String], name: String, defaultValue: String): String = {
    val value: String = params.get(name)
    if (value != null && !value.trim.isEmpty) {
      return value.trim
    }
    return defaultValue
  }

  /**
   * Returns the current site id OR null if we aren't in a site
   *
   * @param context
   * @return The current site id OR null if we aren't in a site
   */
  def getCaseDetails(context: RequestContext): JSONObject = {
    var caseId: String = context.getUriTokens().get(CaseEvaluatorUtil.CASE_ID)

    val nodeRefexists: Boolean = nodeRefExists(context)._1

    if(StringUtils.isEmpty(caseId)) {
      caseId = context.getParameter(CaseEvaluatorUtil.CASE_ID)

      if(StringUtils.isEmpty(caseId) && nodeRefexists)
        caseId = nodeRefExists(context)._2
    }

    if(StringUtils.isNotEmpty(caseId)) {
      try {
        val caseDetails: JSONObject = jsonGet("/api/openesdh/case/noderef/" + caseId)
        if (caseDetails != null && StringUtils.isNotEmpty(caseDetails.getString("caseNodeRef"))) {
          return caseDetails
        }
        null
      }
      catch {
        case npe: NullPointerException => {
          //        logger.warn(s"======>Error in determining if nodeRef is case: ($npe)\n\n")
          return null
        }
      }
    }
    null
  }

  def nodeRefExists(context: RequestContext): (Boolean, String) ={
      val destination: String = context.getParameter(CaseEvaluatorUtil.DESTINATION)
      val urlNodeRef : String = context.getParameter(CaseEvaluatorUtil.CASE_NODEREF)

      val findNodeInUrl = (dst: String, nde: String) => if(StringUtils.isNotEmpty(dst) ) dst
                                                        else if(StringUtils.isNotEmpty(nde) ) nde
                                                        else null

      val retPair = (nodeStr: String) => if(StringUtils.isNotEmpty(nodeStr) ) new Tuple2(true, nodeStr) else new Tuple2(false, null)
      val node = findNodeInUrl(destination, urlNodeRef)
      if(StringUtils.isEmpty(node))
        return Tuple2(false, null)

      val isCaseDocResult: JSONObject = jsonGet("/api/openesdh/documents/isCaseDoc/" + node.replace("://", "/"))
      if (isCaseDocResult == null || StringUtils.isEmpty(isCaseDocResult.getString("caseId")) )
        return Tuple2(false, null)
      val result = retPair(isCaseDocResult.getString("caseId"))

      result
  }

  /**
   * Helper method for making a json get remote call to the default repository.
   *
   * @param uri The uri to get the content for (MUST contain a json response)
   * @return The content of the uri resource parsed into a json object.
   */
  def jsonGet(uri: String): JSONObject = {
    val scriptRemote: ScriptRemote = serviceRegistry.getScriptRemote
    val response: Response = scriptRemote.connect.get(uri)
    if (response.getStatus.getCode == 200) {
      try {
        return new JSONObject(response.getResponse)
      }
      catch {
        case e: JSONException => {
//            logger.error("An error occurred when parsing response to json from the uri '" + uri + "': " + e.getMessage)
        }
      }
    }
    return null
  }

}