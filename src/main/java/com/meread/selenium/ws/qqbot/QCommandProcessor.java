package com.meread.selenium.ws.qqbot;

/**
 * @author yangxg
 * @date 2021/10/17
 */
public interface QCommandProcessor {
    void process(long senderQQ, String content, QQAiFlow qqAiFlow);
}
