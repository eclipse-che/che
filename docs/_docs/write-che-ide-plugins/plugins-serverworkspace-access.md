---
tags: [ "eclipse" , "che" ]
title: Services
excerpt: ""
layout: docs
permalink: /:categories/serverworkspace-access/
---
Many simple extensions to Che are directly implemented in the IDE running in the browser. This has the advantage that the execution of simple actions do not require any server round-trips and are therefore typically very fast.
However, more complex operations, especially when accessing resources from a project or the workspace require you to run on the server or within the workspace. Examples for such operations are validating files, compiling code, providing auto-completion or creating workspaces. All those operations are implemented as REST services and can be consumed by the client IDE.

Services can either be deployed on the server or directly in a workspace. Server services provide generic features which are not depending on the Workspace or its content, e.g. user management. Workspace services can access the contents of a project (e.g. source files) and also trigger operations in the workspace (such as compilation).
In the following, we will describe how to create server and workspace services and how they are consumed from the client IDE. As the specification of workspace services is a special case of specifying server services, we will start with a simple server service, including an introduction of general basics about services.
Based on that, in the subsequent section “Workspace Services”, we will describe a more complex example, which accesses the files of a specific project.
Server and Workspace services need to be deployed differently, please refer to the section [Create and build extensions](doc:create-and-build-extensions) to learn how.

##Server Services
The following diagram shows all components of server services. The classes highlighted in dark grey are to be implemented for the extension. The `ServerService` offers a REST service to be consumed.
The IDE plugin implements a client class (`MyClient`), which calls the REST service using Che helper classes. The result is made available by a Java API, which is to be defined based on the result type. By calling the client class, different components of the IDE, such as `Actions` or `CodeCompletionProcessors`, can consume the `ServerService` without having to deal with the REST API itself.
![image15.png]({{ base }}/assets/imgs/image15.png)
In the following, we will describe how to build a simple example server service that accepts a String {name} and responds with "Hello {name} !".
Further, we will demonstrate how this service can be consumed from within the client IDE.

Every REST service defines the path under which it is reachable. In the following code example, the path consists of two parts. The first one identifies the service itself and is specified with the @Path annotation of the class itself, in this case “hello”.
The second part of the path defines a parameter for the @GET method to be called and is specified with the @Path annotation at the method `#sayHello` itself, in this case “name”.
In combination, this example will register a service that will listen to localhost:8080/api/hello/{name}, where {name} is an arbitrary String to be passed by the client.
As specified using the @PathParam annotation, the {name} parameter will be passed as an input and then be used in the return statement.
```java  
@Path("hello")
public class MyService {

  @GET
  @Path("{name}")
  public String sayHello(@PathParam("name") String name) {
  	return "Hello " + name + " !";
  }
}

```
To make the server service consumable within the IDE, we implement a client encapsulating the REST call (see following code example).
Therefore, the client will offer a method `#getHello`, which can be called by any IDE component with a parameter “name”. To send an asynchronous REST request, the client uses two Che utilities, the `AsyncRequestFactory` and the `LoaderFactory`, which both get injected into the constructor.

The `AsyncRequestFactory` simplifies the creation of REST calls by providing a method `#createGetRequest` which will create a request using the provided parameter as a path. It will automatically prefix this path with the current server URL used by the IDE, so the parameter “hello” would be bound to [http://serveradress/hello](http://serveradress/hello).

The request is sent by passing in an `Unmarshaller`. It is responsible for unmarshalling the response from the transport format (JSON) to a Java type, e.g. a String. Che already provides a collection of `Unmarshallers`, please see the section [Calling Workspace APIs](doc:calling-workspace-apis)  for details.

Finally, the send method returns a `Promise`, which can be consumed by callers on `MyClient` (see below) to retrieve the answer from the server.
```java  
/che-ide-extension/src/main/java/examples/MyServiceClient.java
public class MyServiceClient {
    private final AsyncRequestFactory asyncRequestFactory;
    private final String helloPath;
    private final LoaderFactory loaderFactory;

    @Inject
    public MyServiceClient(AsyncRequestFactory asyncRequestFactory,
                           LoaderFactory loaderFactory) {
    	this.asyncRequestFactory = asyncRequestFactory;
      this.loaderFactory = loaderFactory;
    }

    //Can be invoked by consumers
    public Promise<String> getHello(String name) {
    	return asyncRequestFactory.createGetRequest(“hello” + "/" + name)
					.loader(loaderFactory.newLoader("Waiting for hello..."))
					.send(new StringUnmarshaller());
    }

}

```
Now, `MyClient` can be consumed from any other component in the IDE without having to deal with the REST call itself.
The following example action gets the `MyServiceClient` injected and calls the `#getHello` method to retrieve the `Promise`.
On the `Promise` you can pass in an operation to be executed if the server call has been successful using the `#then` method. As we have used a `StringUnmarshaller` before, the parameter will be the String created by the server service.
In the example, it is passed to the Che `NotificationManager`. Using the `#catchError` method on the `Promise`, you can define an operation to be executed on an error during the server call.
```java  

public class MyAction extends Action {

    private final NotificationManager notificationManager;
    private final MyServiceClient serviceClient;

    @Inject
    public MyAction(MyResources resources, NotificationManager
                    notificationManager, MyServiceClient serviceClient) {

      super("My Action\ "My Action Description\ null,
            resources.MyProjectTypeIcon());
      this.notificationManager = notificationManager;
      this.serviceClient = serviceClient;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // This calls the service in the workspace.
        // This method is in our MyServiceClient class
        // This is a Promise, so the .then() method is invoked after the response is made

    	serviceClient.getHello("CheTheAllPowerful!").then(new Operation<String>()
      {

      	@Override
        public void apply(String arg) throws OperationException {
        	// This passes the response String to the notification manager.
        	notificationManager.notify(arg, StatusNotification.Status.SUCCESS, StatusNotification.DisplayMode.FLOAT_MODE);
        }

       }).catchError(new Operation<PromiseError>() {

        	@Override
          public void apply(PromiseError arg) throws OperationException {
            notificationManager.notify("Fail\ StatusNotification.Status.FAIL, StatusNotification.DisplayMode.FLOAT_MODE);
          }

       });
    }


```

##Workspace Services
Workspace services are special types of server services, they are deployed directly within the workspace agent. Therefore, they can access the content of a workspace, e.g. projects, source files, etc. Furthermore, they can trigger native operations in the running workspace.
Besides their different scope, workspace services are developed like standard server services using REST. Therefore, we recommend to first cover the previous section about [server services](serverworkspace-access#section-server-services). The main difference between workspace and server services is where they are actually deployed to. Please refer to the section [Create and build extensions](doc:create-and-build-extensions)  to learn how to deploy services correctly.

In the following, we describe an example workspace service from the JSON example, which accesses a selected project and counts the number of lines in all JSON files. This demonstrates how to access files and their content. In a custom use case, this could be adapted to do any kind of file operation, e.g. parsing contents for auto completion. After introducing the service, we demonstrate how it can be consumed by an action from within the IDE.

Like a server service, workspace services need to define the path in which they are reachable at. As they are running for a specific workspace, the path includes the variable {ws-id}, which identifies the workspace the service is running in (see @Path annotation in the following code example). The example service gets the Che `ProjectManager` injected in the constructor. It allows to access projects and their contents. Please note that this service is not available for server services.

The method `#countLinesPerFile` receives a `projectPath` as a parameter for which it should count the number of lines. It uses the `ProjectManager` to retrieve the project. Using the project, it then navigates over all JSON files in the project and accesses the contents of those files to count the lines. Finally, it returns the result as a map.
```java  
org.eclipse.che.plugin.jsonexample.JsonLocService
@Path("json-example/{ws-id}")
public class JsonLocService {

	private ProjectManager projectManager;

	@Inject
	public JsonLocService(ProjectManager projectManager) {
  	this.projectManager = projectManager;
	}


	@GET
	@Path("{projectPath}")
	public Map<String, String> countLinesPerFile(
         @PathParam("projectPath") String projectPath)
        	throws ServerException, NotFoundException, ForbiddenException {

  	Map<String, String> linesPerFile = new LinkedHashMap<>();
    RegisteredProject project = projectManager.getProject(projectPath);

    for (FileEntry child : project.getBaseFolder().getChildFiles()) {
    	if (isJsonFile(child)) {
      	linesPerFile.put(child.getName(), Integer.toString(countLines(child)));
      }
    }

    return linesPerFile;
	}

	private static int countLines(FileEntry fileEntry)
         throws ServerException, ForbiddenException {
  	String content = fileEntry.getVirtualFile().getContentAsString();
    String[] lines = content.split("\r\n|\r|\n");
    return lines.length;
	}

	private static boolean isJsonFile(FileEntry fileEntry) {
  	return fileEntry.getName().endsWith("json");
	}

}

```
To access workspace services on the client-side, the path needs to include the workspace ID as well as the project ID. Both can be retrieved using the `AppContext`. Furthermore, all services running within the workspace have a basic context path which can be retrieved from the `AppContext`.

The following example creates a path, which would access the service above with the current project as a parameter:

```java  
String url = this.appContext.getDevMachine().getWsAgentBaseUrl()
  + "/json-example/"
  + appContext.getWorkspaceId()
  + appContext.getCurrentProject().getProjectConfig().getPath();
```
Besides the specific path, workspace services can be consumed like any other server service using the REST utilities of Che. Please have a look at the sections [Server Services](serverworkspace-access#section-server-services) and Client Server Communication for more details.

The following example action consumes the workspace service defined above and shows the result using the notification manager.

```java  
org.eclipse.che.plugin.jsonexample.ide.action.CountLocAction

@Singleton
public class CountLinesAction extends JsonExampleProjectAction {

	private final AppContext appContext;
	private final StringMapUnmarshaller unmarshaller;
	private AsyncRequestFactory asyncRequestFactory;
	private NotificationManager notificationManager;

	@Inject
	public CountLinesAction(AppContext appContext,
                      	JsonExampleResources resources,
                      	AsyncRequestFactory asyncRequestFactory,
                      	NotificationManager notificationManager) {

  	super(appContext,
            	"Count JSON Lines of Code\n            	"Counts lines of code for all JSON Files in the project\n            	resources.icon());

  	this.appContext = appContext;
    this.asyncRequestFactory = asyncRequestFactory;
    this.notificationManager = notificationManager;
    this.unmarshaller = new StringMapUnmarshaller();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
  	String url = this.appContext.getDevMachine().getWsAgentBaseUrl()
      + "/json-example/"
      + this.appContext.getWorkspaceId()
      + this.appContext.getCurrentProject().getProjectConfig().getPath();

    	asyncRequestFactory.createGetRequest(url, false).send(
	     	new AsyncRequestCallback<Map<String, String>>(unmarshaller) {

          @Override
          protected void onSuccess(Map<String, String> linesPerFile) {

            for (Map.Entry<String, String> entry : linesPerFile.entrySet()) {
            	String fileName = entry.getKey();
              String loc = entry.getValue();
              notificationManager.notify(
              	"File " + fileName + " has " + loc + " lines.\n                StatusNotification.Status.SUCCESS,
                StatusNotification.DisplayMode.FLOAT_MODE
              );
             }
           }

           @Override
           protected void onFailure(Throwable exception) {
           	notificationManager.notify(
            	exception.getMessage(),
              StatusNotification.Status.FAIL,
              StatusNotification.DisplayMode.FLOAT_MODE
              );
           }
        });

	}
}

```
