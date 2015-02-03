package dk.openesdh.share.evaluator

import java.util
import com.typesafe.scalalogging.slf4j.StrictLogging
import org.json.JSONObject
import org.springframework.extensions.surf.RequestContext
import org.springframework.extensions.surf.extensibility.ExtensionModuleEvaluator

/**
 * @author Lanre Abiwon.
 */
class OpenESDHCaseModuleEvaluator(val caseEvaluatorUtil: CaseEvaluatorUtil) extends ExtensionModuleEvaluator with StrictLogging{

  val CASE_STATUS = "caseStatus"
  val CASE_TYPE = "caseType"

  override def getRequiredProperties: Array[String] = Array(CASE_TYPE, CASE_STATUS)

  override def applyModule(context: RequestContext, params: util.Map[String, String]): Boolean = {
    val caseDetails: JSONObject = caseEvaluatorUtil.getCaseDetails(context)

    // Meaning we got the case details back
    if (caseDetails != null) {
      val caseType: String = caseDetails.get("caseType").asInstanceOf[String]
      val caseStatus: String = caseDetails.get("caseStatus").asInstanceOf[String]

      // Test case types filter
      if (!caseType.matches(caseEvaluatorUtil.getEvaluatorParam(params, CASE_TYPE, ".*"))) {
        return false
      }

      // Test case status filter
      if (caseStatus == null || !caseStatus.matches(caseEvaluatorUtil.getEvaluatorParam(params, CASE_STATUS, ".*"))) {
        return false
      }

      return true
    }

    false
  }

}
