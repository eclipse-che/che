---
tags: [ "eclipse" , "che" ]
title: REST APIs
excerpt: ""
layout: docs
permalink: /:categories/calling-workspace-apis/
---
{% include base.html %}
Many of the IDE components that you build into your extension will need to communicate directly with the Che server or to the workspace the IDE is currently bound to. Che provides helper utilities to make REST calls simpler. Che's REST library is built on top of Google's HTTP Java client libraries.

In your extension code, you can create an AsyncRequestFactory object, which has helper methods for creating requests that will have responses.
```java  
private void getProjectType(@NotNull String workspaceId,
                            @NotNull String id,
                            @NotNull AsyncCallback<ProjectTypeDto> callback) {

    final String url = extPath + "/project-type/" + workspaceId + '/' + id;
    asyncRequestFactory.createGetRequest(url)
                       .header(ACCEPT, APPLICATION_JSON)
                       .loader(loaderFactory.newLoader("Getting info about project type..."))
                       .send(newCallback(callback,
                                         dtoUnmarshallerFactory.newUnmarshaller(ProjectTypeDto.class)));

}
```
This example comes from the class used by the IDE to ask the server to provide a response on what the current project type is within the currently active workspace. The `asyncRequestFactory` object was instantiated by the system as an input parameter. Calling the `createGetRequest()` method with the GET REST URL as an input will generate a request and a response. The `.loader()` method is an optional display component that will appear on the screen while the contents of the response are loading. The `send()` method takes a callback object which will be invoked by the system when a response is delivered.

In the [debugger implementation class](https://github.com/eclipse/che/blob/e3407ae74674c5f84af89341826ec5e98106f90e/plugins/plugin-java/che-plugin-java-ext-debugger-java-client/src/main/java/org/eclipse/che/ide/ext/java/jdi/client/debug/DebuggerServiceClientImpl.java), you can see a range of REST calls for different individual functions such as step into, step over, and  so forth.

In the [Java content assist class](https://github.com/eclipse/che/blob/e3407ae74674c5f84af89341826ec5e98106f90e/plugins/plugin-java/che-plugin-java-ext-lang-client/src/main/java/org/eclipse/che/ide/ext/java/client/editor/JavaCodeAssistClient.java), you can see the sequence of REST calls that are made for generating requests for information from the server about intellisense features that can only be processed on the server side.

### Callbacks
In Che, you will frequently see `AsyncRequestCallback<T>` objects passed into an `AsyncRequestFactory` object. Callbacks will be invoked by the system when a response is returned. This class inherits from `com.google.gwt.http.client.RequestCallback` and we add in a few additional objects:
1. `Unmarshallable<T>` which is logic to convert the response payload from data into a Java object.
2. `AsyncRequestLoader` which is a visual loader to display while downloading data.
3. `AsyncRequest` which is the original request.

Che provides different types of `Unmarshallable` objects including [StringUnmarshaller](https://github.com/eclipse/che/blob/0d0bbf900114e9c9964d386b02f0904a913ae4e0/core/commons/che-core-commons-gwt/src/main/java/org/eclipse/che/ide/rest/StringUnmarshaller.java), [StringMapUnmarshaller](https://github.com/eclipse/che/blob/0d0bbf900114e9c9964d386b02f0904a913ae4e0/core/commons/che-core-commons-gwt/src/main/java/org/eclipse/che/ide/rest/StringMapUnmarshaller.java), [StringMapListUnmarshaller](https://github.com/eclipse/che/blob/0d0bbf900114e9c9964d386b02f0904a913ae4e0/core/commons/che-core-commons-gwt/src/main/java/org/eclipse/che/ide/rest/StringMapListUnmarshaller.java), [DtoUnmarshaller](https://github.com/eclipse/che/blob/0d0bbf900114e9c9964d386b02f0904a913ae4e0/core/commons/che-core-commons-gwt/src/main/java/org/eclipse/che/ide/rest/DtoUnmarshaller.java), and [LocationUnmarshaller](https://github.com/eclipse/che/blob/0d0bbf900114e9c9964d386b02f0904a913ae4e0/core/commons/che-core-commons-gwt/src/main/java/org/eclipse/che/ide/rest/LocationUnmarshaller.java).  These different marshallers represent the most common types of JSON to Java payload conversions.

For example, this logic comes from the git plugin and is the method that is called when a user asks to delete the local git repository contained within the project.
```java  
public void deleteRepository() {
    final CurrentProject project = appContext.getCurrentProject();
    final GitOutputConsole console = gitOutputConsoleFactory.create(DELETE_REPO_COMMAND_NAME);

     service.deleteRepository(workspaceId, project.getRootProject(),
                              new AsyncRequestCallback<Void>() {
        @Override
        protected void onSuccess(Void result) {
            console.print(constant.deleteGitRepositorySuccess());
            consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
            notificationManager.notify(constant.deleteGitRepositorySuccess(), project.getRootProject());
            getRootProject(project.getRootProject());
        }

        @Override
        protected void onFailure(Throwable exception) {
            // The logic for what to do if the response generated a failure message
        }
    });
}\
```
