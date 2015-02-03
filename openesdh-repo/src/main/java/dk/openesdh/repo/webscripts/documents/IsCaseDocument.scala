package dk.openesdh.repo.webscripts.documents

import com.typesafe.scalalogging.slf4j.StrictLogging
import dk.openesdh.repo.model.OpenESDHModel
import dk.openesdh.repo.services.cases.CaseService
import dk.openesdh.repo.services.documents.DocumentService
import org.alfresco.service.cmr.repository.{NodeService, InvalidNodeRefException, NodeRef}
import org.apache.commons.lang.StringUtils
import org.json.JSONObject
import org.springframework.extensions.webscripts._

/**
  * @author Lanre Abiwon
  */
class IsCaseDocument( val documentService: DocumentService, val nodeService: NodeService, val caseService: CaseService) extends AbstractWebScript with StrictLogging{

    protected override def execute(req: WebScriptRequest, resp: WebScriptResponse) = {
      val templateArgs = req.getServiceMatch.getTemplateVars

      try {
          val storeType: String = templateArgs.get ("store_type")
          val storeId: String = templateArgs.get ("store_id")
          val id: String = templateArgs.get ("id")
          val docNodeRefStr = s"$storeType://$storeId/$id"
          val documentNode: NodeRef = new NodeRef (docNodeRefStr)
          val caseNodeRef = documentService.getCaseNodeRef(documentNode)
          val caseId =  caseService.getCaseId(caseNodeRef)
          val obj: JSONObject = new JSONObject()

          if (caseNodeRef != null ){
            obj.put("isCaseDoc", true)
            obj.put("caseId", caseId)
          }
          else
            obj.put("isCaseDoc", false)

          resp.getWriter.write(obj.toString)
      }
      catch {
        case inre: InvalidNodeRefException => {
          //logger.error("", inre)
        }
      }
    }

 }
