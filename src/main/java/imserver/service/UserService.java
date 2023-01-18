package imserver.service;

public interface UserService {
    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     * @return 登陆成功放回 true
     */
    boolean login(String username,String password);
}
