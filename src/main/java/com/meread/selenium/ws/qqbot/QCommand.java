package com.meread.selenium.ws.qqbot;

/**
 * @author yangxg
 * @date 2021/10/17
 */
public enum QCommand {

    GET_NEW_CK("获取CK", 1, 0),
    GET_NEW_CK_LOGIN_TYPE("获取CK-输入登陆方式", 2, 1),
    GET_NEW_CK_PHONE("获取CK-接收手机号", 3, 2),
    GET_NEW_CK_AUTHCODE_QQ("获取CK-接收QQ登陆验证码", 4, 2),
    GET_NEW_CK_REQUIRE_SCAN_QR("获取CK-等待用户扫描二维码确认", 5, 2),
    GET_NEW_CK_WAIT_CK("获取CK-等待获取ck", 6, 2),
    GET_NEW_CK_AUTHCODE_PHONE("获取CK-接收手机登陆验证码", 7, 3),
    GET_NEW_CK_REMARK("获取CK-接收备注", 8, 7),
    GET_NEW_CK_QLID("获取CK-接收青龙配置id", 9, 8),

    EXIT("直接退出", 10, 0),
    HELP("帮助菜单", 11, 0);

    private String desc;
    private int code;
    private int parentCode;

    QCommand(String desc, int code, int parentCode) {
        this.parentCode = parentCode;
        this.desc = desc;
        this.code = code;
    }

    public static QCommand parse(int code) {
        for (QCommand qc : QCommand.values()) {
            if (qc.getCode() == code) {
                return qc;
            }
        }
        return null;
    }

    public String getDesc() {
        return desc;
    }

    public int getCode() {
        return code;
    }

    public int getParentCode() {
        return parentCode;
    }

}
