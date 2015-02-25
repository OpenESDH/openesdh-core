package dk.openesdh.exceptions.contacts;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * @author Lanre Abiwon.
 */
public class GenericContactException extends AlfrescoRuntimeException {

    private static final long serialVersionUID = -3514678458987433997L;

    public GenericContactException(String message){
        super(message);
    }
}
