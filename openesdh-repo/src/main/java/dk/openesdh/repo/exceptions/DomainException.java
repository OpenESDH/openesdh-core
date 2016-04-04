package dk.openesdh.repo.exceptions;

public class DomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final boolean domain = true;

    /**
     * exception is handled in front-end
     *
     * @param errorCode - code will be used as translate code in front-end
     */
    public DomainException(String errorCode) {
        super(errorCode);
    }

    /**
     * exception is handled in front-end
     *
     * @param errorCode - code will be used as translate code in front-end
     * @param cause
     */
    public DomainException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public boolean isDomain() {
        return domain;
    }

    public String getErrorCode() {
        return getMessage();
    }
}
