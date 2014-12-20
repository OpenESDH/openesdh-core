package dk.openesdh.repo.webscripts.documents

import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.slf4j.Logger
import dk.openesdh.repo.services.cases.CaseService
import dk.openesdh.repo.services.documents.DocumentService
import org.alfresco.service.cmr.repository.{InvalidNodeRefException, NodeRef}
import org.springframework.extensions.webscripts.{Cache, DeclarativeWebScript, Status, WebScriptRequest}
/**
 * @author lanre
 */
class DocumentCaseContainers(val caseService: CaseService, val documentService:DocumentService) extends DeclarativeWebScript{
   protected override def executeImpl(req: WebScriptRequest, status: Status, cache: Cache) :  java.util.Map[String, Object] = {

     val templateArgs = req.getServiceMatch.getTemplateVars
     val logger = Logger(LoggerFactory.getLogger(classOf[DocumentCaseContainers]))

     try {
       val storeType: String = templateArgs.get ("store_type")
       val storeId: String = templateArgs.get ("store_id")
       val id: String = templateArgs.get ("id")
       val docNodeRefStr = s"$storeType://$storeId/$id"
       val documentNode: NodeRef = new NodeRef (docNodeRefStr)
       val caseNodeRef = documentService.getCaseNodeRef(documentNode)
       val caseDocumentNodeRef = caseService.getDocumentsFolder(caseNodeRef)
       val model = Map ("caseNodeRef" -> caseNodeRef, "caseDocumentNodeRef" -> caseDocumentNodeRef).asInstanceOf[java.util.Map[java.lang.String, Object]]

       logger.debug(s"*** The caseNodeRef: $caseNodeRef  ***")

       model
     }
     catch {
       case inre: InvalidNodeRefException => {
         logger.error(inre.getMessage)
         null
       }
     }
   }

}
