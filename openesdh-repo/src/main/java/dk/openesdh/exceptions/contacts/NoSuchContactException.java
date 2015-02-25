package dk.openesdh.exceptions.contacts;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * @author Lanre Abiwon.
 */
public class NoSuchContactException extends AlfrescoRuntimeException {

    private static final long serialVersionUID = -351467831095433997L;

    public NoSuchContactException(){
        super(String.format("Contact does not exist or could not be created"));
    }
}
