package dk.openesdh.repo.services.contacts

import java.io.Serializable
import org.alfresco.model.ContentModel

import scala.collection.immutable._
import scala.collection.JavaConversions._

import com.typesafe.scalalogging.slf4j.StrictLogging
import dk.openesdh.exceptions.contacts.InvalidContactTypeException
import dk.openesdh.repo.model.{OpenESDHModel, ContactInfo}
import org.alfresco.service.cmr.repository._
import org.alfresco.service.cmr.search.{ResultSet, SearchService}
import org.alfresco.service.cmr.security.{AuthorityType, AuthorityService}
import org.alfresco.service.namespace.QName
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.Pair

/**
 * @author Lanre Abiwon.
 * Guided by the ootb AuthorityServiceImpl
 */
class ContactServiceImpl (val nodeService: NodeService, val searchService: SearchService, val authorityService: AuthorityService, val contactDAO: ContactDAOImpl) extends ContactService with StrictLogging{

  val CONTACT_ZONE = "CONTACT.STORE"

  val DEFAULT_ZONES:Set[String] = Set(AuthorityService.ZONE_APP_DEFAULT)

/*
  def getContactsStorageRoot: NodeRef = {
    val storeRef: StoreRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore")
    val rs: ResultSet = this.searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home/app:dictionary/contact:contacts\"")
    val contactStorageRootNodeRef: NodeRef = rs.getNodeRef(0)
    rs.close
    contactStorageRootNodeRef
  }
*/

  /**
   * It should never be the case that a contact has multiple logins associated.
   * If only because of the cardinality specified in the model.
   * We return the associated person nodeRef so as to only retrieve if only it exists (via the service)
   *
   * @param contactRef
   * @return Pair<Boolean, NodeRef>
   */
  def hasAssociatedLogin(contactRef: NodeRef): Pair[Boolean, NodeRef] = {
    try {
      val contactLogin = this.nodeService.getTargetAssocs(contactRef, ContentModel.ASSOC_MEMBER)
      if (!contactLogin.isEmpty) 
        return Pair.of(true, contactLogin.get(0).getTargetRef)
    }
    catch {
      case npe: NullPointerException => {
      //logger.warn("***** Error *****\n\t\t The contact has no associated login or somehow has too many logins assigned")
      }
    }

    Pair.of(false, null)
  }

  def createContact(email: String, contactType: String): NodeRef = {
    createContact(email, contactType, null, false)
  }

  def createContact(email: String, contactType: String, properties: Map[QName, Serializable]): NodeRef = {
    createContact(email, contactType, properties, false)
  }

  def createContact(email: String, contactType: String, properties: Map[QName, Serializable], createAssociatedLogin: Boolean): NodeRef = {

    if (!contactType.equalsIgnoreCase(ContactInfo.ContactType.PERSON.name) && !contactType.equalsIgnoreCase(ContactInfo.ContactType.ORGANIZATION.name))
      throw new InvalidContactTypeException("The type of contact is not recognised. Can only create types PERSON/ORGANIZATION")
    else
    if (StringUtils.isEmpty(email)) throw new NullPointerException("Email is mandatory for contact creation")

    val groupNodeRef: NodeRef = contactDAO.createContact(email, contactType, DEFAULT_ZONES, null)

    groupNodeRef



   /* val typeProps = Map(OpenESDHModel.PROP_CONTACT_EMAIL -> email, OpenESDHModel.PROP_CONTACT_TYPE -> contactType.toUpperCase)

    nodeService.setType(authNode, OpenESDHModel.TYPE_CONTACT_BASE)

    nodeService.setProperties(authNode, typeProps)
*/

  }

}