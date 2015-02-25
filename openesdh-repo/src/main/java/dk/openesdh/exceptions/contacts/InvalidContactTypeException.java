package dk.openesdh.exceptions.contacts;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * @author Lanre Abiwon.
 */
public class InvalidContactTypeException extends AlfrescoRuntimeException {
    private static final long serialVersionUID = -320467751095433377L;

    public InvalidContactTypeException(String message){
        super(String.format("Invalid contact : "+ message));
    }
}
