package dk.openesdh.repo.webscripts.cases

import com.typesafe.scalalogging.slf4j.StrictLogging
import dk.openesdh.repo.model.OpenESDHModel
import dk.openesdh.repo.services.cases.CaseService
import org.alfresco.service.cmr.repository.{InvalidNodeRefException, NodeService}
import org.json.JSONObject
import org.springframework.extensions.webscripts._

/**
  * @author Lanre Abiwon
  */
class getCaseNodeRefById( val caseService: CaseService, val nodeService: NodeService) extends AbstractWebScript with StrictLogging{

    protected override def execute(req: WebScriptRequest, resp: WebScriptResponse) = {

      val templateArgs = req.getServiceMatch.getTemplateVars

      try {
        val caseId: String = templateArgs.get ("caseId")
        val caseNodeRef = caseService.getCaseById(caseId)
        val obj: JSONObject = new JSONObject()

        obj.put("caseNodeRef", caseNodeRef)
        //The next three properties initially used for the module extension evaluator
        obj.put("caseId", this.nodeService.getProperty(caseNodeRef,OpenESDHModel.PROP_OE_ID))
        obj.put("caseStatus", this.nodeService.getProperty(caseNodeRef,OpenESDHModel.PROP_OE_STATUS))
        obj.put("caseType", this.nodeService.getType(caseNodeRef))
        resp.getWriter.write(obj.toString)
      }
      catch {
        case inre: InvalidNodeRefException => {
          //logger.error("", inre)
        }
      }
    }

 }
