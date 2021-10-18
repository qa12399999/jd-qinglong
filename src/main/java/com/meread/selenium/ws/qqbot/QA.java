package com.meread.selenium.ws.qqbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author yangxg
 * @date 2021/10/17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QA {
    private long requestTime;
    private String requestRaw;
    private QCommand qCommand;
    private ProcessStatus status;

    public QCommand getTopCommand() {
        if (qCommand == null) {
            return null;
        }
        QCommand res = qCommand;
        int parentCode = qCommand.getParentCode();
        while (parentCode != 0) {
            res = QCommand.parse(parentCode);
            parentCode = res.getParentCode();
        }
        return res;
    }
}
