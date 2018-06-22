package simpleRpc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * 演示了RPC服务的几个要素：
 *  - 网络传输（本例：socket)
 *  - 协议（约定的报文格式和含义）
 *  - 动态代理 (本例：JDK动态代理，基于接口)
 *  - 序列化（本例：简单的JDK手动序列化来代替）
 *
 * 本例中RPC的服务端和客户端已知服务类，因此没有传递这个类，而只传递了类的方法
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class RpcFramework {

    public static final void export(final Object service, int port) throws Exception {
        if (service == null) {
            throw new IllegalArgumentException("service instance == null");
        }
        if (port <= 0 || port > 65536) {
            throw new IllegalArgumentException("Invalid port " + port);
        }
        System.out.println("Export service " + service.getClass().getName() + " on port " + port);

        ServerSocket server = new ServerSocket(port);
        for (; ; ) {
            try {
                final Socket socket = server.accept();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            try {
                                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                                try {
                                    // 只是个示例，约定的RPC协议格式，并且只传递了方法名，远程服务和本地stub都提前知道类
                                    String methodName = input.readUTF();
                                    Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
                                    Object[] arguments = (Object[]) input.readObject();

                                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                                    try {
                                        Method method = service.getClass().getMethod(methodName, parameterTypes);
                                        Object result = method.invoke(service, arguments);

                                        output.writeObject(result);
                                    } catch (Throwable t) {
                                        output.writeObject(t);
                                    } finally {
                                        output.close();
                                    }
                                } finally {
                                    input.close();
                                }
                            } finally {
                                socket.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static final <T> T refer(final Class<T> interfaceClass, final String host, final int port) throws Exception {
        if (interfaceClass == null) {
            throw new IllegalArgumentException();
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException();
        }
        if (host == null || host.length() == 0) {
            throw new IllegalArgumentException();
        }
        if (port <= 0 || port > 65536) {
            throw new IllegalArgumentException();
        }

        System.out.println("Get remote serice " + interfaceClass.getName() + " from server " + host + ":" + port);

        // 动态代理，动态代理内部实际上时远程调用，但对客户端调用者来讲是透明的，它不知道是远程调用
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Socket socket = new Socket(host, port);
                try {
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    try {
                        // 只是个示例，约定的RPC协议格式，并且只传递了方法名，远程服务和本地stub都提前知道类
                        output.writeUTF(method.getName());
                        output.writeObject(method.getParameterTypes());
                        output.writeObject(args);

                        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                        try {
                            Object result = input.readObject();
                            if (result instanceof Throwable) {
                                throw (Throwable) result;
                            }
                            return result;
                        } finally {
                            input.close();
                        }
                    } finally {
                        output.close();
                    }
                } finally {
                    socket.close();
                }
            }
        });
    }


}
