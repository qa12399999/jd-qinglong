package com.meread.selenium.service;

import com.meread.selenium.bean.JDScreenBean;
import com.meread.selenium.bean.MyChromeClient;
import com.meread.selenium.bean.QLConfig;
import com.meread.selenium.bean.QLToken;
import com.meread.selenium.ws.qqbot.ProcessStatus;
import com.meread.selenium.ws.qqbot.QA;
import com.meread.selenium.ws.qqbot.QCommand;
import com.meread.selenium.ws.qqbot.QQAiFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by yangxg on 2021/10/12
 *
 * @author yangxg
 */
@Slf4j
@Service
public class ScheduleService {

    @Autowired
    private BaseWebDriverManager driverFactory;

    @Autowired
    private JDService jdService;

    @Autowired
    private BotService botService;

    /**
     * 和grid同步chrome状态，清理失效的session，并移除本地缓存
     */
    @Scheduled(initialDelay = 10000, fixedDelay = 2000)
    public void heartbeat() {
        driverFactory.heartbeat();
    }

    /**
     * 监控用户是否扫描qq二维码，监控是否获取到了ck
     */
    @Scheduled(fixedDelay = 2000)
    public void qqBotScanQR() {
        Map<Long, QQAiFlow> qqAiFlowMap = botService.getQqAiFlowMap();
        for (long qq : qqAiFlowMap.keySet()) {
            QQAiFlow qqAiFlow = qqAiFlowMap.get(qq);
            QA qa = qqAiFlow.getLast();
            if (qa == null) {
                continue;
            }
            if (qa.getQCommand() == QCommand.GET_NEW_CK_REQUIRE_SCAN_QR || qa.getQCommand() == QCommand.GET_NEW_CK_WAIT_CK) {
                log.info("schedule process " + qq + " : " + qa.getQCommand());
                MyChromeClient myChromeClient = driverFactory.getCacheMyChromeClient(String.valueOf(qq));
                if (myChromeClient != null) {
                    JDScreenBean screen = jdService.getScreen(myChromeClient);
                    if (screen.getPageStatus() == JDScreenBean.PageStatus.REQUIRE_REFRESH) {
                        botService.sendMsgWithRetry(qq, "二维码失效，等待生成新的...");
                        botService.genQQQR(qq, qa, false);
                    } else if (screen.getPageStatus() == JDScreenBean.PageStatus.REQUIRE_SCANQR) {
                        log.info("怎么还不扫二维码");
                    } else if (screen.getPageStatus() == JDScreenBean.PageStatus.WAIT_QR_CONFIRM) {
                        botService.sendMsgWithRetry(qq, "扫描成功，请确认...");
                    } else if (screen.getPageStatus() == JDScreenBean.PageStatus.SUCCESS_CK) {
                        qa.setStatus(ProcessStatus.FINISH);
                        botService.sendMsgWithRetry(qq, "获取ck成功：" + screen.getCk().toString());
                        QA qa1 = new QA(System.currentTimeMillis(), "", QCommand.GET_NEW_CK_REMARK, ProcessStatus.WAIT_NEXT_Q);
                        qqAiFlow.getQas().add(qa1);
                    } else if (screen.getPageStatus() == JDScreenBean.PageStatus.WAIT_CUBE_SMSCODE) {
                        qa.setStatus(ProcessStatus.FINISH);
                        botService.sendMsgWithRetry(qq, screen.getMsg());
                        QA qa1 = new QA(System.currentTimeMillis(), "", QCommand.GET_NEW_CK_AUTHCODE_QQ, ProcessStatus.WAIT_NEXT_Q);
                        qqAiFlow.getQas().add(qa1);
                    }
                }
            }
        }
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 30 * 60000)
    public void syncCK_count() {
        List<QLConfig> qlConfigs = driverFactory.getQlConfigs();
        if (qlConfigs != null) {
            for (QLConfig qlConfig : qlConfigs) {
                int oldSize = qlConfig.getRemain();
                Boolean exec = driverFactory.exec(webDriver -> {
                    jdService.fetchCurrentCKS_count(webDriver, qlConfig, "");
                    return true;
                });
                if (exec != null && exec) {
                    int newSize = qlConfig.getRemain();
                    log.info(qlConfig.getQlUrl() + " 容量从 " + oldSize + "变为" + newSize);
                } else {
                    log.error("syncCK_count 执行失败");
                }
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void refreshOpenIdToken() {
        List<QLConfig> qlConfigs = driverFactory.getQlConfigs();
        if (qlConfigs != null) {
            for (QLConfig qlConfig : qlConfigs) {
                if (qlConfig.getQlLoginType() == QLConfig.QLLoginType.TOKEN) {
                    QLToken qlTokenOld = qlConfig.getQlToken();
                    jdService.fetchNewOpenIdToken(qlConfig);
                    log.info(qlConfig.getQlToken() + " token 从" + qlTokenOld + " 变为 " + qlConfig.getQlToken());
                }
            }
        }
    }


}
