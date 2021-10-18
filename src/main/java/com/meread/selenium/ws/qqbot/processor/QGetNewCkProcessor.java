package com.meread.selenium.ws.qqbot.processor;

import com.meread.selenium.service.BotService;
import com.meread.selenium.ws.qqbot.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yangxg
 * @date 2021/10/17
 */
@Component
@Slf4j
public class QGetNewCkProcessor implements QCommandProcessor {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}");
    private static final Pattern AUTHCODE_PATTERN = Pattern.compile("\\d{6}");
    private static final Pattern QLID_PATTERN = Pattern.compile("\\d+");


    //        Matcher matcher = PATTERN.matcher(content);
//        Matcher matcher2 = PATTERN2.matcher(content);
//        Matcher matcher3 = PATTERN3.matcher(content);
//        Matcher matcher4 = PATTERN4.matcher(content);

    @Autowired
    private BotService botService;

    @Override
    public void process(long senderQQ, String content, QQAiFlow qqAiFlow) {
        log.info("senderQQ = " + senderQQ + ", content = " + content);
        QA last = qqAiFlow.getLast();
        QCommand qCommand = last.getQCommand();
        log.info("qCommand = " + qCommand);
        if (qCommand == QCommand.GET_NEW_CK) {
            botService.sendMsgWithRetry(senderQQ, "请选择登陆方式：\n1.手机号\n2.qq扫码");
            last.setStatus(ProcessStatus.FINISH);
            QA qa1 = new QA(System.currentTimeMillis(), "", QCommand.GET_NEW_CK_LOGIN_TYPE, ProcessStatus.WAIT_NEXT_Q);
            qqAiFlow.getQas().add(qa1);
        } else if (qCommand == QCommand.GET_NEW_CK_LOGIN_TYPE) {
            int code = Integer.parseInt(content);
            if (code == 1) {
                botService.sendMsgWithRetry(senderQQ, "请输入11位手机号：");
                last.setStatus(ProcessStatus.FINISH);
                QA qa1 = new QA(System.currentTimeMillis(), "", QCommand.GET_NEW_CK_PHONE, ProcessStatus.WAIT_NEXT_Q);
                qqAiFlow.getQas().add(qa1);
            } else if (code == 2) {
                botService.sendMsgWithRetry(senderQQ, "正在生成二维码...");
                botService.genQQQR(senderQQ, last, true);
                last.setStatus(ProcessStatus.PROCESSING);
            } else {
                botService.sendMsgWithRetry(senderQQ, "输入有误，请输入1或2!");
            }
        } else if (qCommand == QCommand.GET_NEW_CK_PHONE) {
            Matcher matcher = PHONE_PATTERN.matcher(content);
            if (matcher.matches()) {
                botService.doSendSMS(senderQQ, content, last);
                last.setStatus(ProcessStatus.PROCESSING);
            } else {
                botService.sendMsgWithRetry(senderQQ, "手机号格式有误");
                last.setStatus(ProcessStatus.FAIL);
            }
        } else if (qCommand == QCommand.GET_NEW_CK_AUTHCODE_PHONE) {
            Matcher matcher = AUTHCODE_PATTERN.matcher(content);
            if (matcher.matches()) {
                botService.doLoginAndGetCK(senderQQ, content, last);
                last.setStatus(ProcessStatus.PROCESSING);
            } else {
                botService.sendMsgWithRetry(senderQQ, "验证码格式有误");
                last.setStatus(ProcessStatus.FAIL);
            }
        } else if (qCommand == QCommand.GET_NEW_CK_AUTHCODE_QQ) {
            Matcher matcher = AUTHCODE_PATTERN.matcher(content);
            if (matcher.matches()) {
                botService.processCubeAuthCode(senderQQ, content, last);
                last.setStatus(ProcessStatus.PROCESSING);
            } else {
                botService.sendMsgWithRetry(senderQQ, "验证码格式有误");
                last.setStatus(ProcessStatus.FAIL);
            }
        } else if (qCommand == QCommand.GET_NEW_CK_REMARK) {
            botService.trackRemark(senderQQ, content, last);
        } else if (qCommand == QCommand.GET_NEW_CK_QLID) {
            Matcher matcher = QLID_PATTERN.matcher(content);
            if (matcher.matches()) {
                char[] chars = content.toCharArray();
                Set<Integer> qlIds = new HashSet<>();
                for (char c : chars) {
                    int qlId = Integer.parseInt(String.valueOf(c));
                    qlIds.add(qlId);
                }
                botService.doUploadQinglong(senderQQ, qlIds, last);
            } else {
                botService.sendMsgWithRetry(senderQQ, "青龙格式有误");
                last.setStatus(ProcessStatus.FAIL);
            }
        }
    }

}
