package simpleRpc;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class RpcConsumer {

    public static void main(String[] args) throws Exception {
        HelloService service = RpcFramework.refer(HelloService.class, "127.0.0.1", 1234);
        for (int i = 0; i < 10; i++) {
            String hello = service.hello("paranoidq-" + i);
            System.out.println(hello);
        }
    }
}
