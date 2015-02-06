package dk.openesdh.repo.webscripts.contacts

import com.typesafe.scalalogging.slf4j.StrictLogging
import dk.openesdh.repo.model.OpenESDHModel
import dk.openesdh.repo.services.contacts.ContactService
import org.alfresco.service.cmr.repository.{InvalidNodeRefException, NodeService}
import org.json.JSONObject
import org.springframework.extensions.webscripts._

/**
  * @author Lanre Abiwon
  */
class CreateContact( val nodeService: NodeService, val contactService: ContactService) extends AbstractWebScript with StrictLogging{

    protected override def execute(req: WebScriptRequest, resp: WebScriptResponse) = {

      try {
          val email: String = req.getParameter("email")
          val contactType: String = "PERSON"

          val createdContact = contactService.createContact(email, contactType)

          val obj: JSONObject = new JSONObject()

          if (createdContact != null ){
            obj.put("contactNodeRef", createdContact.toString)
            obj.put("type", this.nodeService.getProperty(createdContact, OpenESDHModel.PROP_CONTACT_TYPE))
          }
          else
            obj.put("message", "uncreated")

          resp.getWriter.write(obj.toString)
      }
      catch {
        case inre: InvalidNodeRefException => {
          //logger.error("", inre)
        }
      }
    }

 }
