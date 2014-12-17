package dk.openesdh.repo.webscripts.documents

import dk.openesdh.repo.services.cases.CaseService
import dk.openesdh.repo.services.documents.DocumentService
import org.alfresco.service.cmr.repository.{InvalidNodeRefException, NodeRef}
import org.apache.commons.logging.{Log, LogFactory}
import org.springframework.extensions.webscripts.{Cache, DeclarativeWebScript, Status, WebScriptRequest}

/**
 * Created by lanre on 16/12/2014.
 */
class DocumentCaseContainers(val caseService: CaseService, val documentService:DocumentService) extends DeclarativeWebScript {
   protected override def executeImpl(req: WebScriptRequest, status: Status, cache: Cache) : Map[String, AnyRef] = {

     val templateArgs = req.getServiceMatch.getTemplateVars
     val logger: Log = LogFactory.getLog(classOf[DocumentCaseContainers])

     try {
       val storeType: String = templateArgs.get ("store_type")
       val storeId: String = templateArgs.get ("store_id")
       val id: String = templateArgs.get ("id")
       val docNodeRefStr: String = storeType + "://" + storeId + "/" + id
       val documentNode: NodeRef = new NodeRef (docNodeRefStr)
       val caseNodeRef = documentService.getCaseNodeRef(documentNode)
       val caseDocumentNodeRef = caseService.getDocumentsFolder(caseNodeRef)
       val model = Map ("caseNodeRef" -> caseNodeRef, "caseDocumentNodeRef" -> caseDocumentNodeRef)
       logger.warn("\n\n**** caseNodeRef ==> " + caseNodeRef)

       model
     }
     catch {
       case inre: InvalidNodeRefException => {
         logger.error (inre.getMessage)
         return null
       }
     }
   }

}
