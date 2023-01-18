package imserver.session;


import io.netty.channel.Channel;

public interface Session {
    /**
     * 绑定会话
     * @param channel 哪个channel要绑定会话
     * @param username 会话绑定用户
     */
    void bind(Channel channel, String username);

    /**
     * 解绑会话
     * @param channel 哪个 channel 要解绑会话
     */
    void unbind(Channel channel);

    /**
     * 获取属性
     * @param channel
     * @param name
     * @return
     */
    Object getAttribute(Channel channel,String name);

    /**
     * 设置属性
     * @param channel
     * @param name
     * @param value
     */
    void setAttribute(Channel channel,String name,Object value);

    /**
     * 根据用户名获取channel
     * @param username
     * @return
     */
    Channel getChannel(String username);
}
