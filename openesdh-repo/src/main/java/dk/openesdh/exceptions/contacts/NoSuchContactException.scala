package dk.openesdh.exceptions.contacts

import org.alfresco.error.AlfrescoRuntimeException

/**
 * @author Lanre Abiwon.
 */
@SerialVersionUID(-351467831095433997L)
class NoSuchContactException(errorMessage:String) extends AlfrescoRuntimeException(errorMessage) {}