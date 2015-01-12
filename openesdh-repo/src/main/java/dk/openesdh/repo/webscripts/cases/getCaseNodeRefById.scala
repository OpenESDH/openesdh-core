package dk.openesdh.repo.webscripts.cases

import org.json.JSONObject
import org.springframework.extensions.webscripts._
import dk.openesdh.repo.services.cases.CaseService
import com.typesafe.scalalogging.slf4j.StrictLogging
import org.alfresco.service.cmr.repository.{InvalidNodeRefException, NodeRef}

/**
  * @author Lanre Abiwon
  */
class getCaseNodeRefById( val caseService: CaseService) extends AbstractWebScript with StrictLogging{
    protected override def execute(req: WebScriptRequest, resp: WebScriptResponse) = {

      val templateArgs = req.getServiceMatch.getTemplateVars

      try {
        val caseId: String = templateArgs.get ("caseId")
        val caseNodeRef = caseService.getCaseById(caseId)
        val obj: JSONObject = new JSONObject()

        obj.put("caseNodeRef", caseNodeRef)
        resp.getWriter.write(obj.toString)
      }
      catch {
        case inre: InvalidNodeRefException => {
          logger.error(inre.getMessage)
        }
      }
    }

 }
