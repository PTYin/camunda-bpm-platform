package edu.thss.platform.engine.listener;

import edu.thss.platform.engine.util.GlobalVariables;
import edu.thss.platform.engine.util.RestTemplateUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class TaskCreateListener implements TaskListener {
    private final Logger logger = LoggerFactory.getLogger(TaskCreateListener.class);

    @Override
    public void notify(DelegateTask delegateTask) {

        JacksonJsonNode env = (JacksonJsonNode) delegateTask.getVariable("_env");
        JacksonJsonNode obj = (JacksonJsonNode) delegateTask.getVariable("_obj");
        JacksonJsonNode param = (JacksonJsonNode) delegateTask.getVariable("_param");
        JacksonJsonNode user = (JacksonJsonNode) delegateTask.getVariable("_user");
        String _dwfCallbackServerURL;

        if (GlobalVariables.variables.containsKey("callbackURL")){
            // 如果通过前端配置了回调地址
            delegateTask.getExecution().setVariable("_dwfCallbackServerURL", GlobalVariables.variables.get("callbackURL"));
            _dwfCallbackServerURL = GlobalVariables.variables.get("callbackURL");
        }else {
            // 否则使用默认回调地址
            _dwfCallbackServerURL = (String) delegateTask.getVariable("_dwfCallbackServerURL");
        }


        Context.getCommandContext().getTransactionContext()
                .addTransactionListener(TransactionState.COMMITTED, new TransactionListener() {

                    @Override
                    public void execute(CommandContext commandContext) {
                        // perform your query to get all executed activities
                        String processInstanceId = delegateTask.getExecution().getProcessInstanceId();
                        DelegateExecution delegateExecution = delegateTask.getExecution();
                        System.out.println(delegateTask.getProcessInstanceId() + "------\n" + delegateTask.getExecution().getProcessInstance().getProcessInstanceId());
                        String taskInstanceId = delegateTask.getId();


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

                        logger.info("--------------------TaskCreateListener execute, processInstanceId:{}, taskInstanceId:{}, taskName:{}-----------------\n", processInstanceId, taskInstanceId,delegateTask.getName());

                        // java8版本

                        String url = _dwfCallbackServerURL + "/dwf/v1/workflow/callback/create-task-listener";
                        /*if (GlobalVariables.variables.containsKey("callbackURL")){
                            url = GlobalVariables.variables.get("callbackURL") + "/dwf/v1/workflow/callback/create-task-listener";
                        }else
                            url = "http://"+ dwfCallbackServerIp +":9090/dwf/v1/workflow/callback/create-task-listener";
*/
                        logger.info("--------------------TaskCreateListener send callback request, processInstanceId:{}, taskInstanceId:{}, taskName:{}, url:{} -----------------\n", processInstanceId, taskInstanceId,delegateTask.getName(), url);
                        ResponseEntity<String> response = new RestTemplateUtils().post(url, payload, String.class);
                        if (response != null && response.getStatusCodeValue() == 200){
                            logger.info("TaskCreateListener ends, processInstanceId:{}, taskInstanceId:{}, taskName:{}\n", processInstanceId, taskInstanceId,delegateTask.getName());
                        }

                    }
                });


    }
}
