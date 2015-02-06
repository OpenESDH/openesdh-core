package dk.openesdh.exceptions.contacts;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Created by lanre on 06/02/2015.
 */
public class NoSuchContactException extends AlfrescoRuntimeException {

    private static final long serialVersionUID = -351467831095433997L;

    public NoSuchContactException(){
        super(String.format("Contact does not exist and could not be created"));
    }
}
