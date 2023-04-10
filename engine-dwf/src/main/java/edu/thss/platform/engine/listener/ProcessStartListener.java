package edu.thss.platform.engine.listener;

import edu.thss.platform.engine.util.GlobalVariables;
import edu.thss.platform.engine.util.RestTemplateUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ProcessStartListener implements ExecutionListener {
    private final Logger logger = LoggerFactory.getLogger(ProcessStartListener.class);

    private Expression oprName;

    private Expression type;

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        // 准备需要传递的变量
        String dwfCallbackServerURL = (String) delegateExecution.getVariable("_dwfCallbackServerURL");
        if (GlobalVariables.variables.containsKey("callbackURL")){
            // 如果通过前端配置了回调地址
            delegateExecution.setVariable("_dwfCallbackServerURL", GlobalVariables.variables.get("callbackURL"));
            dwfCallbackServerURL = GlobalVariables.variables.get("callbackURL");
        }else {
            // 否则使用默认回调地址
            dwfCallbackServerURL = (String) delegateExecution.getVariable("_dwfCallbackServerURL");
        }

        logger.info("========CallbackDWF Start======== url:" + dwfCallbackServerURL);

        Map<String,Object> payload = new HashMap();
        payload.put("oprName", oprName.getValue(delegateExecution).toString());
        payload.put("type", type.getValue(delegateExecution).toString());

        String url = dwfCallbackServerURL +"/dwf/v1/workflow/callback/process-start-listener";

        logger.info("========CallbackDWF Start========, url:{}", url);
        ResponseEntity<String> response = new RestTemplateUtils().post(url, payload, String.class);
        if (response != null && response.getStatusCodeValue() == 200){
            logger.info("connector ends, processInstanceId:"+delegateExecution.getProcessInstanceId());
        }
    }
}
