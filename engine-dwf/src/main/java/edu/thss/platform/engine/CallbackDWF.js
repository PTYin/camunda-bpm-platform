var _camunda = null;
var listenerClass = Java.type('org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener');
var taskName = '';
try
{
	_camunda = execution;//CamundaExecutionListener
	taskName = _camunda.getCurrentActivityName();
}catch(e)
{
	_camunda = task;//CamundaTaskListener
	listenerClass = Java.type('org.camunda.bpm.model.bpmn.instance.camunda.CamundaTaskListener');
	taskName = _camunda.getName();
}

var _wf = 
{
	"taskId": _camunda.activityId
	, "taskName": taskName
	, "eventName": _camunda.eventName
	, "processDefinitionId": _camunda.processDefinitionId
	, "processInstanceId":_camunda.processInstanceId
}
print("========CallbackDWF Start========"+JSON.stringify(_wf));
var ProcessInstance = Java.type('org.camunda.bpm.engine.runtime.ProcessInstance')
var isProcess = _camunda instanceof ProcessInstance;
var elemIns = _camunda.getBpmnModelElementInstance();
var extensionElements = elemIns.getExtensionElements();
if(isProcess)
{
	// startEvent start > process start
	var listenersCnt = extensionElements == null? 0 : extensionElements.getElementsQuery()
		.filterByType(listenerClass.class)
		.count();
	if(listenersCnt == 0)
	{
		elemIns = _camunda.getBpmnModelElementInstance().getParentElement();
		extensionElements = elemIns.getExtensionElements();
	}
}

var _exts = {};
if (extensionElements != null)
{
	// var ExtensionElements = Java.type('org.camunda.bpm.model.bpmn.instance.ExtensionElements');
	var listeners = extensionElements.getElementsQuery()
		.filterByType(listenerClass.class)
		.list();
	var currListener = null;
	for (var idx in listeners)
	{
		var one = listeners[idx];
		if(one.getCamundaEvent() == _camunda.eventName)
		{
			currListener = one;
			break;
		}
	}
	if(currListener)
	{
		var fields = currListener.getCamundaFields().toArray();
		for (var idx in fields)
		{
			var one = fields[idx];
			var key1 = one.getCamundaName();
			var value1 = one.getCamundaString().getTextContent();
			_exts[key1] = value1;
		}
	}
	
}
// _wf['e'] = isEndFlow;
_wf['extensions'] = _exts;
// print("========CallbackDWF extensions========"+JSON.stringify(_wf));
var payload = {
	"processInstanceId": _camunda.processInstanceId
	, "oprName": _exts["oprName"]
	, "_obj": JSON.parse(_obj.toString())
	, "_user": JSON.parse(_user.toString())
	, "_env": JSON.parse(_env.toString())
	, "_param": JSON.parse(_param.toString())
	, "_wf":_wf
}

var HttpRequest = Java.type('java.net.http.HttpRequest');
var HttpResponse = Java.type('java.net.http.HttpResponse');
var HttpClient = Java.type('java.net.http.HttpClient');
var URI = Java.type('java.net.URI');
var client = HttpClient.newHttpClient();
var request = HttpRequest.newBuilder().version(HttpClient.Version.HTTP_1_1)
	.POST(HttpRequest.BodyPublishers.ofString(JSON.stringify(payload)))
	.uri(URI.create("http://"+_env.prop("serverIp").value()+":"+_env.prop("objServicePort").value()+"/dwf/v1/workflow/callback/connector"))
	.header("Content-Type", "application/json")
	.build();
var response = client.send(request, HttpResponse.BodyHandlers.ofString());
print(response);
if (response != null && response.statusCode() == 200)
{
	print("========CallbackDWF End========"+JSON.stringify(_wf));
}
