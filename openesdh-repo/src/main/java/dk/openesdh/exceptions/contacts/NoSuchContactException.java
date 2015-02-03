package dk.openesdh.exceptions.contacts;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * @author Lanre Abiwon.
 */
public class NoSuchContactException extends AlfrescoRuntimeException {
    private static final long serialVersionUID = -3514678431095433997L;


    public NoSuchContactException(String prop) {
        super(String.format("Contact does not exist or is not of type contact : %s", prop));
    }

}
