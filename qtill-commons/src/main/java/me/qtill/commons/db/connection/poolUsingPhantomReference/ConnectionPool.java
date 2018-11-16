package me.qtill.commons.db.connection.poolUsingPhantomReference;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ConnectionPool {

    private Queue<Connection> poolQueue = new LinkedList<>();


    private ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();

    private IdentityHashMap<Object, Connection> ref2Cxt = new IdentityHashMap<>();
    private IdentityHashMap<Connection, Object> cxt2Ref = new IdentityHashMap<>();


    public ConnectionPool(String driver, String url, String user, String password, int maxConn) {
        try {
            Class.forName(driver);
            for (int i = 0; i < maxConn; i++) {
                poolQueue.add(
                    DriverManager.getConnection(url, user, password)
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("unable to initialize", e);
        }
    }


    public Connection getConnection() {
        while (true) {
            synchronized (this) {
                if (poolQueue.size() > 0) {
                    return wrapConnection(poolQueue.remove());
                }
            }
            // 尝试从回收队列中找到connection
            tryWaitingForGC();
        }

    }


    private void tryWaitingForGC() {
        try {
            Reference<?> ref = referenceQueue.remove(100);
            if (ref != null) {
                releaseConnection(ref);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void releaseConnection(Reference<?> reference) {
        // 先根据ref找到connection对象，然后再移除
        Connection cxt = ref2Cxt.remove(reference);
        if (cxt != null) {
            releaseConnection(cxt);
        }
    }

    synchronized void releaseConnection(Connection cxt) {
        Object ref = cxt2Ref.remove(cxt);
        ref2Cxt.remove(ref);
        poolQueue.offer(cxt);
        System.out.println("Release connection " + cxt);

    }

    private synchronized Connection wrapConnection(Connection cxt) {
        Connection wrapped = PooledConnection.newInstance(this, cxt);
        // 关联虚引用
        PhantomReference<Connection> ref = new PhantomReference<>(wrapped, referenceQueue);
        cxt2Ref.put(cxt, ref);
        ref2Cxt.put(ref, cxt);
        System.out.println("Acquired connection :" + cxt);
        return wrapped;
    }
}
