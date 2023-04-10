package edu.thss.platform.engine.listener;

import edu.thss.platform.engine.util.GlobalVariables;
import edu.thss.platform.engine.util.RestTemplateUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class UniformExecutionListener implements ExecutionListener {
    private final Logger logger = LoggerFactory.getLogger(UniformExecutionListener.class);

    private Expression oprName;

    private Expression type;

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        if (type.getValue(delegateExecution).toString().equals("startEvent")) {
            logger.info("[UniformExecutionListener:notify] startEvent is illegal, please update process definition");
            return;
        }
        // 准备需要传递的变量
        Map<String, Object> _wf = new HashMap();
        _wf.put("taskId", delegateExecution.getCurrentActivityId());
        _wf.put("taskName", delegateExecution.getCurrentActivityName());
        _wf.put("eventName", delegateExecution.getEventName());
        _wf.put("processDefinitionId", delegateExecution.getProcessDefinitionId());
        _wf.put("processInstanceId", delegateExecution.getProcessInstanceId());
        String dwfCallbackServerURL = (String) delegateExecution.getVariable("_dwfCallbackServerURL");
        if (GlobalVariables.variables.containsKey("callbackURL")){
            // 如果通过前端配置了回调地址
            delegateExecution.setVariable("_dwfCallbackServerURL", GlobalVariables.variables.get("callbackURL"));
            dwfCallbackServerURL = GlobalVariables.variables.get("callbackURL");
        }else {
            // 否则使用默认回调地址
            dwfCallbackServerURL = (String) delegateExecution.getVariable("_dwfCallbackServerURL");
        }

        logger.info("========CallbackDWF Start========"+ _wf.toString() + " url:" + dwfCallbackServerURL);

        Map<String,Object> extension = new HashMap();
        extension.put("oprName", oprName.getValue(delegateExecution).toString());
        extension.put("type", type.getValue(delegateExecution).toString());

        _wf.put("extensions", extension);

        Map<String,Object> payload = new HashMap();
        payload.put("processInstanceId", delegateExecution.getProcessInstanceId());
        payload.put("oprName", oprName.getValue(delegateExecution).toString());
        payload.put("_obj", delegateExecution.getVariable("_obj").toString());
        payload.put("_user", delegateExecution.getVariable("_user").toString());
        payload.put("_env", delegateExecution.getVariable("_env").toString());
        payload.put("_param",  delegateExecution.getVariable("_param").toString());
        payload.put("_wf", _wf);

        JacksonJsonNode env = (JacksonJsonNode) delegateExecution.getVariable("_env");

        String url = dwfCallbackServerURL +"/dwf/v1/workflow/callback/listener";
        /*if (GlobalVariables.variables.containsKey("callbackURL")){
            url = GlobalVariables.variables.get("callbackURL") + "/dwf/v1/workflow/callback/listener";
        }else
            url = "http://"+ dwfCallbackServiceIp +"/dwf/v1/workflow/callback/listener";*/
        logger.info("========CallbackDWF Start========, url:{}, payload:{}", url, payload.toString());
        ResponseEntity<String> response = new RestTemplateUtils().post(url, payload, String.class);
        if (response != null && response.getStatusCodeValue() == 200){
            logger.info("connector ends, processInstanceId:"+delegateExecution.getProcessInstanceId());
        }
    }
}
