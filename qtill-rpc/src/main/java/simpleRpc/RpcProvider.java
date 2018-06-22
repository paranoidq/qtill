package simpleRpc;

/**
 *
 * Provider和Consumer的设计模式
 *
 *（这里其实也只是一种思想，体现在命名上，到没有具体的代码结构来佐证）
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class RpcProvider {

    public static void main(String[] args) throws Exception {
        /*
            provider按照接口暴露，但是本地具体在处理时还是由实现类去进行处理的

            如果类名也要传输的话，就需要做好接口 - 实现的关联，能够按照传输的接口名，找到对应的实现类，然后进行RPC远程的处理
         */
        HelloService service = new HelloServiceImpl();
        RpcFramework.export(service, 1234);

    }
}
