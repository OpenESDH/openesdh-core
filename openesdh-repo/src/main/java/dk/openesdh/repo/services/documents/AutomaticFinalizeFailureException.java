package dk.openesdh.repo.services.documents;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Created by syastrov on 9/11/15.
 */
public class AutomaticFinalizeFailureException extends AlfrescoRuntimeException {

    private static final long serialVersionUID = 7096431148933236438L;

    public AutomaticFinalizeFailureException(String msgId) {
        super(msgId);
    }

    public AutomaticFinalizeFailureException(String msgId, Object[] msgParams) {
        super(msgId, msgParams);
    }

    public AutomaticFinalizeFailureException(String msgId, Throwable cause) {
        super(msgId, cause);
    }

    public AutomaticFinalizeFailureException(String msgId, Object[] msgParams, Throwable cause) {
        super(msgId, msgParams, cause);
    }
}
