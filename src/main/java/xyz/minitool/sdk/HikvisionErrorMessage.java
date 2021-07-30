package xyz.minitool.sdk;

/**
 * 海康错误消息封装
 */
public enum HikvisionErrorMessage {

    NET_DVR_PASSWORD_ERROR("用户名或密码错误", 1),
    NET_DVR_NOENOUGHPRI("权限不足", 2),
    NET_DVR_NOINIT("视频组件未初始化", 3),
    NET_DVR_CHANNEL_ERROR("通道号错误", 4),
    NET_DVR_OVER_MAXLINK("设备总的连接数超过最大", 5),
    NET_DVR_NETWORK_FAIL_CONNECT("设备不在线或网络原因引起的连接超时", 7),
    NET_DVR_NETWORK_SEND_ERROR("向设备发送失败", 8),
    NET_DVR_USERNOTEXIST("用户不存在，注册的用户ID已注销或不可用", 47),
    NET_DVR_MAX_USERNUM("登录设备的用户数达到最大", 52);

    private String message;
    private Integer code;

    HikvisionErrorMessage(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public static String getMessage(Integer code) {
        HikvisionErrorMessage[] values = HikvisionErrorMessage.values();
        for (HikvisionErrorMessage value : values) {
            if (code.equals(value.getCode())) {
                return value.getMessage();
            }
        }
        return "未知错误";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
