package com.letv.mobile.core.error;

/**
 * Sd卡存储空间已满
 */
public class SdCardFullException extends BaseException {

    private static final long serialVersionUID = -2743718059057235893L;

    public SdCardFullException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public SdCardFullException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public SdCardFullException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * @param cause
     */
    public SdCardFullException(Throwable cause) {
        super(cause);
    }

}
