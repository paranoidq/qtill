package me.qtill.commons.base.exception;

/**
 * CheckedException的wrapper.
 *
 * 返回Message时, 将返回内层Exception的Message.
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class UncheckedException extends RuntimeException {

    private static final long serialVersionUID = 4140223302171577501L;

    public UncheckedException(Throwable wrapped) {
        super(wrapped);
    }

    @Override
    public String getMessage() {
        return super.getCause().getMessage();
    }
}
