package me.qtill.commons.concurrent.tryit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public interface Promise<T> {


    void setResult(T outcome);

    T getResult() throws InterruptedException;


}
