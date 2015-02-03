package dk.openesdh.repo.model

/**
 * Auto converted by IDEA, Needs to be inspected and (possibly) refactored
 *
 * @author Lanre.
 * @see org.alfresco.service.cmr.security.AuthorityType
 */
object ContactType extends Enumeration {
  type ContactType = Value
  val ORGANIZATION, PERSON, UNSUPPORTED = Value

  def getContactType(contact: String): ContactType = {
    var contactType: ContactType = null

    if (contact == ContactType.ORGANIZATION.toString) {
      contactType = ContactType.ORGANIZATION
    }
    else if (contact == ContactType.PERSON.toString) {
      contactType = ContactType.PERSON
    }
    else contactType = ContactType.UNSUPPORTED

    contactType
  }
}

abstract class ContactType {
  def isFixedString: Boolean

  def getFixedString: String

  def isPrefixed: Boolean

  def getPrefixString: String

  def getOrderPosition: Int

  def equals(contactType: String): Boolean = this equals ContactType.getContactType(contactType)
}