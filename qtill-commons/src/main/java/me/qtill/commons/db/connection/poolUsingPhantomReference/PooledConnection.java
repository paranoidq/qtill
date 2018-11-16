package me.qtill.commons.db.connection.poolUsingPhantomReference;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 代理类，通过{@link #newInstance(ConnectionPool, Connection)}实现对Connection的包装
 * 包装内部的实现将close和isClosed方法逻辑改变为return to pool，而非真正的关闭
 * 从而实现对客户端调用透明的效果
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class PooledConnection implements InvocationHandler {

    private ConnectionPool pool;
    private Connection     cxt;


    public PooledConnection(ConnectionPool pool, Connection cxt) {
        this.pool = pool;
        this.cxt = cxt;
    }

    private Connection getConnection() {
        try {
            if (cxt == null || cxt.isClosed()) {
                throw new RuntimeException("Connection is closed");
            }
        } catch (SQLException e) {
            throw new RuntimeException("unable to determine if underlying connection is open", e);
        }
        return cxt;
    }

    /**
     * 将SQL Connection包装代理，实现池的borrow和return逻辑，做到对使用方透明
     *
     * @param pool
     * @param connection
     * @return
     */
    public static Connection newInstance(ConnectionPool pool, Connection connection) {
        return (Connection) Proxy.newProxyInstance(
            PooledConnection.class.getClassLoader(), new Class[]{Connection.class}, new PooledConnection(pool, connection)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {

            switch (method.getName()) {
                case "close":
                    close();
                    return null;
                case "isClosed":
                    return isClosed();
                default:
                    return method.invoke(getConnection(), args);
            }

        } catch (Throwable ex) {
            if (ex instanceof InvocationTargetException) {
                ex = ((InvocationTargetException) ex).getTargetException();
            }

            if ((ex instanceof Error)
                || (ex instanceof RuntimeException)
                || (ex instanceof SQLException)) {
                throw ex;
            }
            // 反射调用会产生受检异常，需要包装成非受检异常
            throw new RuntimeException("exception during reflective invocation", ex);
        }
    }

    /**
     * Connection正常的close方法被这个方法代理掉了
     * 实现了池的return效果，而不是真正的close
     *
     * @throws SQLException
     */
    private void close() throws SQLException {
        if (cxt != null) {
            pool.releaseConnection(cxt);
            cxt = null;
        }
    }

    private boolean isClosed() throws Exception {
        // ctx==null表示回收到池中去了，也是一种closed的状态
        // ctx.isClosed则表示Connection物理关闭了，jdbc本身的方法
        return cxt == null || cxt.isClosed();
    }
}
