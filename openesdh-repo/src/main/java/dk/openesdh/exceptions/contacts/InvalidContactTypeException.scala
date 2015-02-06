package dk.openesdh.exceptions.contacts

import org.alfresco.error.AlfrescoRuntimeException

/**
 * @author Lanre Abiwon.
 */
@SerialVersionUID(-320467751095433377L)
class InvalidContactTypeException (errorMessage: String) extends AlfrescoRuntimeException (errorMessage) {
}