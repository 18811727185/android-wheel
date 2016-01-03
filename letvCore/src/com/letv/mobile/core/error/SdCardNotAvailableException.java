package com.letv.mobile.core.error;

/**
 * SDcard不可用
 * @author Ryan
 */
public class SdCardNotAvailableException extends BaseException {

    private static final long serialVersionUID = -2743718059057235893L;

    public SdCardNotAvailableException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public SdCardNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public SdCardNotAvailableException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * @param cause
     */
    public SdCardNotAvailableException(Throwable cause) {
        super(cause);
    }

}
