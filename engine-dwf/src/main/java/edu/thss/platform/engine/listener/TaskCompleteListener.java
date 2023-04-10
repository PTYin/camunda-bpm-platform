package edu.thss.platform.engine.listener;

import edu.thss.platform.engine.util.GlobalVariables;
import edu.thss.platform.engine.util.RestTemplateUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class TaskCompleteListener implements TaskListener {
    private final Logger logger = LoggerFactory.getLogger(TaskCompleteListener.class);

    @Override
    public void notify(DelegateTask delegateTask) {

        JacksonJsonNode env = (JacksonJsonNode) delegateTask.getVariable("_env");
        JacksonJsonNode obj = (JacksonJsonNode) delegateTask.getVariable("_obj");
        JacksonJsonNode param = (JacksonJsonNode) delegateTask.getVariable("_param");
        JacksonJsonNode user = (JacksonJsonNode) delegateTask.getVariable("_user");
        String dwfCallbackServerURL;


        String processInstanceId = delegateTask.getExecution().getProcessInstanceId();
        DelegateExecution delegateExecution = delegateTask.getExecution();

        if (GlobalVariables.variables.containsKey("callbackURL")){
            // 如果通过前端配置了回调地址
            delegateTask.getExecution().setVariable("_dwfCallbackServerURL", GlobalVariables.variables.get("callbackURL"));
            dwfCallbackServerURL = GlobalVariables.variables.get("callbackURL");
        }else {
            // 否则使用默认回调地址
            dwfCallbackServerURL = (String) delegateTask.getVariable("_dwfCallbackServerURL");
        }



        String taskInstanceId = delegateTask.getId();
        logger.info("--------------------TaskCompleteListener execute, processInstanceId:{}, taskInstanceId:{}, taskName:{}-----------------\n", processInstanceId, taskInstanceId,delegateTask.getName());
        Map<String, Object> payload = new HashMap<>();
        payload.put("processInstanceId",processInstanceId);
        payload.put("taskInstanceId",taskInstanceId);
        payload.put("targetOid", obj.prop("oid").stringValue());

        Map<String, Object> variables = new HashMap<>();
        variables.put("_obj", obj.toString());
        variables.put("_user", user.toString());
        variables.put("_env", env.toString());
        variables.put("_param",  param.toString());

        payload.put("variables", variables);

        // java8版本
        String url = dwfCallbackServerURL + "/dwf/v1/workflow/callback/task-complete-listener";
        /*if (GlobalVariables.variables.containsKey("callbackURL")){
            url = GlobalVariables.variables.get("callbackURL") + "/dwf/v1/workflow/callback/task-complete-listener";
        }else
            url = "http://"+ dwfCallbackServerIp+":9090/dwf/v1/workflow/callback/task-complete-listener";*/
        logger.info("--------------------TaskCompleteListener send request, processInstanceId:{}, taskInstanceId:{}, taskName:{}, url:{}-----------------\n", processInstanceId, taskInstanceId,delegateTask.getName(), url);

        ResponseEntity<String> response = new RestTemplateUtils().post(url, payload, String.class);
        if (response != null && response.getStatusCodeValue() == 200){
            logger.info("TaskCompleteListener ends, processInstanceId:{}, taskInstanceId:{}, taskName:{}\n", processInstanceId, taskInstanceId,delegateTask.getName());
        }
    }
}