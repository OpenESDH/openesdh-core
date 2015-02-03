package dk.openesdh.repo.services.contacts

import java.io.Serializable
import scala.collection.immutable._
import org.apache.commons.lang3.tuple.Pair

import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.QName

/**
 * @author Lanre Abiwon.
 */

trait ContactService {

  /**
   * Prefix used for authorities of type contact.
   */
  val CONTACT_PREFIX: String = "CONTACT_"

  /**
   * Get the noderef for the storage folder.
   *
   * @return NodeRef
   */
//  def getContactsStorageRoot: NodeRef

  /**
   * check to see if the contact has an associated login
   * @param contactRef
   * @return a tuple containing the noderef and a true or false if the ndeRef exists.
   */
  def hasAssociatedLogin(contactRef: NodeRef): Pair[Boolean, NodeRef]

  /**
   *
   * @param email
   * @param contactType - Constrained to PERSON or ORGANIZATION.
   * @return the NodeRef of the newly created contact.
   */
  def createContact(email: String, contactType: String): NodeRef

  /**
   *
   * @param email
   * @param contactType - Constrained to PERSON or ORGANIZATION.
   * @param properties - The map of additional properties that are mapped to the aspect properties to be applied.
   * @return the NodeRef of the newly created contact.
   */
  def createContact(email: String, contactType: String, properties: Map[QName, Serializable]): NodeRef

  /**
   * A property map specifying at least first name is required for this method so as to
   * satisfy the alfresco ootb requirements.
   *
   * @param email
   * @param contactType - Constrained to PERSON or ORGANIZATION.
   * @param properties - The map of additional properties that are mapped to the aspect properties to be applied.
   * @param createAssociatedLogin - Boolean variable to indicate whether to create an associated account or not.
   * @return the NodeRef of the newly created contact
   */
  def createContact(email: String, contactType: String, properties: Map[QName, Serializable], createAssociatedLogin: Boolean): NodeRef
}