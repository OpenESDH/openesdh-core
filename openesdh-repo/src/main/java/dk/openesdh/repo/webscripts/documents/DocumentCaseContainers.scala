package dk.openesdh.repo.webscripts.documents

import com.typesafe.scalalogging.slf4j.StrictLogging
import dk.openesdh.repo.services.cases.CaseService
import dk.openesdh.repo.services.documents.DocumentService
import org.alfresco.service.cmr.repository.{InvalidNodeRefException, NodeRef}
import org.springframework.extensions.webscripts.{Cache, DeclarativeWebScript, Status, WebScriptRequest}
import scala.collection.JavaConversions._
import scala.collection.mutable._

/**
 * @author lanre
 */
class DocumentCaseContainers(val caseService: CaseService, val documentService: DocumentService) extends DeclarativeWebScript with StrictLogging{
   protected override def executeImpl(req: WebScriptRequest, status: Status, cache: Cache) :  java.util.Map[String, Object] = {

     val templateArgs = req.getServiceMatch.getTemplateVars

     try {
       val storeType: String = templateArgs.get ("store_type")
       val storeId: String = templateArgs.get ("store_id")
       val id: String = templateArgs.get ("id")
       val docNodeRefStr = s"$storeType://$storeId/$id"
       val documentNode: NodeRef = new NodeRef (docNodeRefStr)
       val caseNodeRef = documentService.getCaseNodeRef(documentNode)
       val caseDocumentNodeRef: Object = caseService.getDocumentsFolder(caseNodeRef)

       val model: Map[java.lang.String, java.lang.Object] = new HashMap[java.lang.String, java.lang.Object] ()
       model.put("caseNodeRef", caseNodeRef)
       model.put("caseDocumentNodeRef", caseDocumentNodeRef)

       model
     }
     catch {
       case inre: InvalidNodeRefException => {
         //logger.error("", inre)
         null
       }
     }
   }

}
