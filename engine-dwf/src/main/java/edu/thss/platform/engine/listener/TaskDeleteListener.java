package edu.thss.platform.engine.listener;

import edu.thss.platform.engine.util.GlobalVariables;
import edu.thss.platform.engine.util.RestTemplateUtils;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class TaskDeleteListener implements TaskListener {
    private final Logger logger = LoggerFactory.getLogger(TaskDeleteListener.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        JacksonJsonNode env = (JacksonJsonNode) delegateTask.getVariable("_env");
        JacksonJsonNode obj = (JacksonJsonNode) delegateTask.getVariable("_obj");
        String dwfCallbackServerURL;

        if (GlobalVariables.variables.containsKey("callbackURL")){
            // 如果通过前端配置了回调地址
            delegateTask.getExecution().setVariable("_dwfCallbackServerURL", GlobalVariables.variables.get("callbackURL"));
            dwfCallbackServerURL = GlobalVariables.variables.get("callbackURL");
        }else {
            // 否则使用默认回调地址
            dwfCallbackServerURL = (String) delegateTask.getVariable("_dwfCallbackServerURL");
        }

        String processInstanceId = delegateTask.getExecution().getProcessInstanceId();
        System.out.println(delegateTask.getProcessInstanceId() + "------\n" + delegateTask.getExecution().getProcessInstance().getProcessInstanceId());
        String taskInstanceId = delegateTask.getId();


        Map<String, Object> payload = new HashMap<>();
        payload.put("taskInstanceId",taskInstanceId);

        logger.info("--------------------TaskDeleteListener execute, processInstanceId:%s, taskInstanceId:%s, taskName:%s-----------------\n", processInstanceId, taskInstanceId,delegateTask.getName());

        // java8版本
        String url = dwfCallbackServerURL + "/dwf/v1/workflow/callback/delete-task-listener";
        /*if (GlobalVariables.variables.containsKey("callbackURL")){
            url = GlobalVariables.variables.get("callbackURL") + "/dwf/v1/workflow/callback/delete-task-listener";
        }else
            url = "http://"+ dwfCallbackServerIp +":9090/dwf/v1/workflow/callback/delete-task-listener";*/
        ResponseEntity<String> response = new RestTemplateUtils().post(url, payload, String.class);
        if (response != null && response.getStatusCodeValue() == 200){
            logger.info("TaskDeleteListener ends, processInstanceId:%s, taskInstanceId:%s, taskName:%s\n", processInstanceId, taskInstanceId,delegateTask.getName());
        }
    }
}
