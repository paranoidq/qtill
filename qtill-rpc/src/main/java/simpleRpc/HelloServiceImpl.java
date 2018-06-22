package simpleRpc;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "Hello " + name;
    }
}
