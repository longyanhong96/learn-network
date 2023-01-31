package rpc.service;

public class RpcServiceImpl implements RpcService {
    @Override
    public String result() {
        return "hello world!";
    }

    @Override
    public String say(String username, String content) {
        return String.format("%s say : '%s'", username, content);
    }
}
