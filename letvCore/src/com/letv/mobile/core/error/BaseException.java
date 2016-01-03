package com.letv.mobile.core.error;

/**
 * @author Ryan
 */
public class BaseException extends Exception {

    private static final long serialVersionUID = -5514287093051368522L;

    String description = null;

    public BaseException() {
        super();
    }

    /**
     * @param detailMessage
     */
    public BaseException(String detailMessage) {
        super(detailMessage);
        this.description = detailMessage;
    }

    /**
     * @param throwable
     */
    public BaseException(Throwable throwable) {
        super(throwable);
    }

    /**
     * @param detailMessage
     * @param throwable
     */
    public BaseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public String getDescription() {
        return this.description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

}
