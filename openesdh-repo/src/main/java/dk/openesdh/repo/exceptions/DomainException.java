package dk.openesdh.repo.exceptions;

import org.json.JSONObject;

public class DomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final boolean domain = true;

    private final JSONObject props;

    /**
     * exception is handled in front-end
     *
     * @param errorCode - code will be used as translate code in front-end
     */
    public DomainException(String errorCode) {
        this(errorCode, (JSONObject) null);
    }

    /**
     * exception is handled in front-end
     *
     * @param errorCode - code will be used as translate code in front-end
     * @param cause
     */
    public DomainException(String errorCode, Throwable cause) {
        this(errorCode, cause, null);
    }

    /**
     * exception is handled in front-end
     *
     * @param errorCode - code will be used as translate code in front-end
     * @param props - json will be added to response
     */
    public DomainException(String errorCode, JSONObject props) {
        super(errorCode);
        this.props = props;
    }

    /**
     * exception is handled in front-end
     *
     * @param errorCode - code will be used as translate code in front-end
     * @param cause
     * @param props - json will be added to response
     */
    public DomainException(String errorCode, Throwable cause, JSONObject props) {
        super(errorCode, cause);
        this.props = props;
    }

    public boolean isDomain() {
        return domain;
    }

    public String getErrorCode() {
        return getMessage();
    }

    public JSONObject getProps() {
        return props;
    }
}
