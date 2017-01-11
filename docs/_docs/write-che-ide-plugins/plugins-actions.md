---
tags: [ "eclipse" , "che" ]
title: Actions
excerpt: "Action API"
layout: docs
permalink: /:categories/actions/
---
{% include base.html %}
Actions allow you to add custom behavior to the Che IDE. They can be placed in menus, toolbars or context menus. An Action is a Java class, which implements the behavior to be executed. Additionally, it defines a text to be shown, a tooltip and an icon. In the following section, we describe the implementation of Actions more in detail.
To make Actions available in the Che IDE, they need to be registered and placed into ActionGroups. Thereby, you specify the location (e.g. a menu or toolbar), where the actions is shown. The registration of actions is described in the subsequent section [Registering Actions](actions#section-registering-actions).

## Authoring Actions
Simple `Actions` directly inherit from `org.eclipse.che.ide.api.action.Action`. In the constructor, we use the super class to configure our action with the following parameters:
* **text** (String): The name of the action shown in the UI, in our case defined by concrete Actions (sub classes).
* Optional: **description** (String): The description of the action shown in the UI, in our case defined by concrete Actions (sub classes).

In the example action below, the constructor also gets the `NotificationManager` injected, which is used to display a "Hello World" message.
A custom `Action` need to implement the `#actionPerformed` method, which is called when it is invoked. Actions can be associated with a variety of triggers within the system such as buttons, menu item selections, or user input (see following section).

```java  
@Singleton
public class HelloWorldAction extends Action {

	private NotificationManager notificationManager;

	@Inject
	public HelloWorldAction(NotificationManager notificationManager) {
    	super("Say Hello World\ "Say Hello World Action");
    	this.notificationManager = notificationManager;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
    	this.notificationManager.notify("Hello World\ StatusNotification.Status.SUCCESS,                      	StatusNotification.DisplayMode.FLOAT_MODE);
	}
}
```
Additionally, you can optionally define an icon for an Action. This is done by calling an alternative constructor including the following two parameters:

* **imageResource**: An icon for the action shown in the UI, in our case defined by concrete Actions, we pass in null, as we alternatively use svgResource.
* **svgResource** (SVGResource): An Icon for the the action shown in the UI, in our case defined by concrete Actions (sub classes).

The following code example defines another "Hello World" action including a corresponding icon:
```java  
@Singleton
public class HelloWorldActionWithIcon extends Action {

    private NotificationManager notificationManager;

    @Inject
    public HelloWorldActionWithIcon(
            NotificationManager notificationManager) {
        super("Say Hello World\ "Say Hello World Action\ null, SampleActionsResources.INSTANCE.icon());
        this.notificationManager = notificationManager;
    }

    /...
}
```
## Registering Actions
Once we have implemented a custom action, we must register it. This is done in the custom `Extension` class , which is used for other extensions, too (see [Dependency Injection Basics](doc:dependency-injection-basics#section-extension-classes) ).

As a first step, we register the `HelloWorldAction` itself at the `ActionManager`. Thereby, Che is aware of the action to be executed. Along with the registration, an action must be associated with a unique ID, which allows to reference the `Action`.

Second, to define a place in the IDE where the Action is visible to the user, we  place the Action in an existing `ActionGroup`. Actions are organized into groups, which, in turn, can contain other groups. A group of actions can form a toolbar or a menu. Subgroups of the group can form submenus of the menu. You can  directly place an `Action` into an existing group. Alternatively, you can create a custom group containing your action and add this group into Che.
In the following example, a custom group is created (`SampleGroup`), the `HelloWorldAction` is added to it and the Group is placed in the main menu of Che.
Additionally, the `HelloWorldActionWithIcon` is directly placed into the main menu.
Please see the following section on more details about existing action locations (i.e. groups) in Che and how to specify the order of actions within those groups.
```java  
@Extension(title = "Sample Actions Extension\ version = "1.0.0")
public class SampleActionsExtensions {
    @Inject
    public SampleActionsExtensions(HelloWorldAction helloWorldAction, ActionManager actionManager) {

      actionManager.registerAction("helloWorldAction\ helloWorldAction);
      actionManager.registerAction("helloWorldActionWithIcon\ helloWorldActionWithIcon);
      /...

    	DefaultActionGroup sampleGroup = new DefaultActionGroup("Sample actions\ true, actionManager);

      sampleGroup.add(helloWorldAction);

      // add sample group after help menu entry
      DefaultActionGroup mainMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_MENU);
        mainMenu.add(sampleGroup);

      // add the sample group to the beginning of the toolbar as well
      DefaultActionGroup toolbar = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_MAIN_TOOLBAR);
      toolbar.add(helloWorldActionWithIcon);
			/...
    }
}
```

##Action Locations
In this section, we describe more in detail, how actions can be placed at specific locations within Che and how the order within toolbars and menus can be specified. Both, the location and the order is specified along with the registration of an action.

Every action and action group in Che has a unique identifier. This allows to reference existing groups and actions when registering a new element and thereby specify its location. In the example registration in the previous section, we have used the ID of the Che main menu to place our custom action group and action in it. All existing identifiers for existing che action groups can be found in  [org.eclipse.che.ide.api.action.IdeActions](https://github.com/eclipse/che/blob/master/core/ide/che-core-ide-api/src/main/java/org/eclipse/che/ide/api/action/IdeActions.java).
Additionally, you can find a collection of examples within the example extension for actions (che/samples/sample-plugin-actions/che-sample-plugin-actions-ide).

In addition to placing actions in groups, you can define a relative order for actions and groups within their parent container. Therefore, a constraint needs to be specified when adding an element to a group. The constraint defines an anchor (ID of an existing element) and a relation to it (`BEFORE` or `AFTER`). Alternatively, you can use `Constraints.FIRST` and `Constraints.LAST` (default) to add an element at the beginning or at the end, respectively.

The following code example shows the registration of the group containing `HelloWorldAction` after the existing help menu, as well as the action itself at the beginning of the main toolbar.

```java  
DefaultActionGroup sampleGroup = new DefaultActionGroup("Sample actions\ true, actionManager);
sampleGroup.add(helloWorldAction);

// add sample group after help menu entry
DefaultActionGroup mainMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_MENU);
mainMenu.add(sampleGroup, new Constraints(AFTER, GROUP_HELP));

// add the sample group to the beginning of the toolbar as well
DefaultActionGroup toolbar = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_MAIN_TOOLBAR);
toolbar.add(helloWorldActionWithIcon, Constraints.FIRST);
```
##Visibility and Enablement

By default, actions will always be visible to the user and enabled. However, certain actions shall only be visible or enabled based on the current state of Che. The implementation of an action is responsible for managing its visibility and enabled state.

Therefore, you need to implement the method `Action.update()` in a custom action. The method is periodically called by the IDE for updating the state. The object of type `ActionEvent` passed to this method carries the information about the current context for the action, e.g. the current perspective of the current selection. Additional information about the current state of the IDE can be retrieved form the service `AppContext` (see [here](docs:section-project-perspective-specific-actions-json-example-) for an example).

The `ActionEvent` allows access to the specific presentation which needs to be updated. As every action can be included in multiple groups and appear in multiple places within the IDE user interface, the visibility and enabled state can not centrally be controlled for an action. For every place where the action appears, a new Presentation is created on which the visibility and enabled state is set alternatively. Please note, that the `ActionEvent` instance is also passed to the `actionPerformed()` method, when the action is executed.

The following example shows the `OnProjectHelloWorldAction`, which is placed in the main menu of Che. It controls its visibility based on the state and is only visible if a project is selected in the navigator.
```java  
public class OnProjectHelloWorldAction extends Action {

    private AppContext appContext;
    private final NotificationManager notificationManager;

    /**
     * Constructor.
     * @param appContext
     *           the application context
     * @param notificationManager
     *           the notification manager
     */
    @Inject
    public OnProjectHelloWorldAction(
            final AppContext appContext,
            final NotificationManager notificationManager) {
        super("Project specific Hello World\ "We have a project");
        this.appContext = appContext;
        this.notificationManager = notificationManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.notificationManager.notify(
                "Hello World in the context of a project\n                StatusNotification.Status.SUCCESS,
                StatusNotification.DisplayMode.FLOAT_MODE);
    }

    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setEnabledAndVisible(appContext.getRootProject() != null);
    }
}
```

##Reusable Actions
For common operations such as creating files, Che provides reusable default actions. Custom implementations can inherit from those and thereby only need to specify their specifics, while reusing most of the default behavior. In this section, we provide an overview of the most common reusable actions in Che.

###Create File Actions
Che provides a template implementation for actions to create new resources (i.e. files). When using the template, you only need to specify the name of the action as well as the file extension to be created (as shown in the following code example).
```java  
org.eclipse.che.plugin.myextension.ide.action.CreateMyFileAction
public class CreateMyFileAction extends AbstractNewResourceAction {

	@Inject
	public CreateMyFileAction(MyResources myResources) {
  	super("Create my File\ "Create a new file \ myResources.icon());
	}

	@Override
	protected String getExtension() {
  	return "my";
	}
}
```
##Project/Perspective-specific Actions (JSON Example)
In this part of the tutorial, as part of the [JSON example](docs:introduction-1#section-the-json-example) we describe how to add project- and perspective-specific actions, meaning  actions that are only available for a specific project type and within specific perspectives. As we want to define several actions of this type, we will create a template implementation and then inherit from it for the implementation of several actions.

These example actions will be placed in the context menu on the specific [JSON project type](doc:custom-project-types)  defined before. The following diagram shows all components of a project type registration. The classes highlighted in dark grey are to be implemented for the extension.

First, our actions must determine whether they are available based on the current app context, in our case, based on the current project type. As we want to add several project specific actions, it makes sense to extract this behavior into an abstract class, in our case `MyAbstractProjectSpecificAction`. By inheriting from this abstract base class, we can now easily add project specific actions implementing the actual behavior to be executed.

As described before, to make an action available in Che, it needs to be registered at the `ActionManager`. This is done in an `Extension`.

![image04.png]({{ base }}/assets/imgs/image04.png)
In the following example, we first define the perspective- and project specific template action. Then, we define a simple action for the JSON example and register it in the context menu of the JSON project type. The action itself will trigger a simple notification once executed. However, the action could be adapted to execute any kind of behavior.

To make our abstract template action perspective-specific, we inherit from a reusable action implementation `AbstractPerspectiveAction` provided by Che. Compared to the basic `Action` its constructor allows the definition of a list of perspectives, in which the action is visible, referenced by ID. Null or empty list means the action is enabled everywhere. In the example, the project perspective, only.

The constructor also gets the `AppContext` injected, which is used in the following to control the project-specific visiblity of the action (see description below).
```java  
org.eclipse.che.plug.plugin.jsonexample.ide.action.JsonExampleProjectAction
public abstract class JsonExampleProjectAction extends AbstractPerspectiveAction {

	private AppContext appContext;

	public JsonExampleProjectAction(AppContext appContext,
                                	@NotNull String text,
                                	@NotNull String description,
                                	@Nullable SVGResource svgResource) {

		super(Collections.singletonList(ProjectPerspective.PROJECT_PERSPECTIVE_ID),
            	text,
            	description,
            	null,
            	svgResource);
  	this.appContext = appContext;
	}

	@Override
	public void updateInPerspective(@NotNull ActionEvent event) {
  	CurrentProject currentProject = appContext.getCurrentProject();
  	event.getPresentation().setEnabledAndVisible(
                 	isJsonExampleProjectType(currentProject));
	}

	private static boolean isJsonExampleProjectType(CurrentProject currentProject)	{
  	if (currentProject == null) {
    	return false;
    }
    return Constants.JSON_EXAMPLE_PROJECT_TYPE_ID.equals(
            	currentProject.getProjectConfig().getType());
	}

}

\
```
The `#updateInPerspective` method is responsible for updating the enablement and the visibility of the action. In this example, we only want to show the action, if the current project is a JSON project. Therefore, we retrieve the current project from the `AppContext`, check whether there is a current project and if so, whether it has the expected project type.
Calling `event.getPresentation().setEnabledAndVisible(true/false)` will set the enablement and the visibility accordingly.

After defining a project specific action, we can now define an arbitrary number of concrete implementations to add custom behavior. The example below inherits from our `JsonExampleProjectAction` and uses the super constructor to configure the specificity of the action. Further, the constructor gets the `NotificationManager` injected, which is used in the implementation of the action below. The method `#actionPerformed` will be called once the user has clicked on an action.

In the example, we trigger a simple notification. However, this simple behavior could be replaced with any custom operation.
```java  
org.eclipse.che.plug.plugin.jsonexample.ide.action.HelloAction
@Singleton
public class HelloWorldAction extends JsonExampleProjectAction {

	private NotificationManager notificationManager;

	@Inject
	public HelloWorldAction(AppContext appContext,
                        	NotificationManager notificationManager) {
    	  super(appContext,
            	"Say Hello World\n            	"Say Hello World Action\n            	null);
    	  this.notificationManager = notificationManager;
	}


	 @Override
	public void actionPerformed(ActionEvent e) {
    	  this.notificationManager.notify(
            	"Hello World\n            	StatusNotification.Status.SUCCESS,
            	StatusNotification.DisplayMode.FLOAT_MODE
    	  );
	}
}

```
Once we have implemented a custom action, we must register it. This is done in the custom extension class `JsonExampleExtension`, which has been used for other extensions before (see [Dependency Injection Basics](doc:dependency-injection-basics) ).

To keep all JSON example related actions together, we define a new `ActionGroup` called “JSON Example”. The second parameter defines that the group is displayed as a popup. After registering the new group at the `ActionManager`, we add our custom `HelloWorldAction` to it.

To define a place in the IDE where the Action is visible to the user, we further place the Action in an existing `ActionGroup`, in our case, the context menu of a project.
```java  
org.eclipse.che.plugin.jsonexample.ide.JsonExampleExtension
@Extension(title = "JSON Example Extension\ version = "0.0.1")
public class JsonExampleExtension {
	@Inject
	public JsonExampleExtension(
        	ActionManager actionManager,
        	HelloWorldAction helloWorldAction,
        	JsonExampleResources jsonExampleResources,
        	IconRegistry iconRegistry) {

		actionManager.registerAction("helloWorldAction\ helloWorldAction);

    DefaultActionGroup jsonGroup = new DefaultActionGroup("JSON Example\
         true, actionManager);
    actionManager.registerAction("jsonExample\ jsonGroup);
    jsonGroup.add(helloWorldAction);

    DefaultActionGroup mainContextMenuGroup = (DefaultActionGroup) actionManager.getAction("resourceOperation");
    mainContextMenuGroup.add(jsonGroup);

  }
}


```
Finally, we can open the context menu on our custom project type and trigger the example action, the screenshot show the `HelloWorldAction`, as well as another project specific action defined in the section [Server/Workspace Access](doc:serverworkspace-access).

![image00.png]({{ base }}/assets/imgs/image00.png)
##Further Example Actions
In this section, we provide a collection of existing example actions to demonstrate the variety of possible locations and behavior to be executed.

The following example creates a `RedirectToDashboardWorkspacesAction` which is the behavior that redirects the IDE back into the user dashboard application.
```java  
package org.eclipse.che.ide.actions;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;

public class RedirectToDashboardWorkspacesAction extends Action {

    private static final String REDIRECT_URL = "/dashboard/#/workspaces";

    @Inject
    public RedirectToDashboardWorkspacesAction(CoreLocalizationConstant localization) {
        super(localization.actionRedirectToDashboardWorkspacesTitle(),
              localization.actionRedirectToDashboardWorkspacesDescription(),
              null,
              null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Window.open(REDIRECT_URL, "_blank\ "");
    }
}

```
The following example (from the [JSON example](docs:section-the-json-example)) executes a server call to retrieve and display the number of lines of code from all JSON files within a project:
```java  
/**
 * Action for counting lines of code of all JSON files within the current project.
 * Line counting is implemented by consuming a RESTful service.
 */
@Singleton
public class CountLinesAction extends JsonExampleProjectAction {

    private final AppContext            appContext;
    private final StringMapUnmarshaller unmarshaller;
    private final AsyncRequestFactory   asyncRequestFactory;
    private final NotificationManager   notificationManager;

    /**
     * Constructor
     *
     * @param appContext
     *         the IDE application context
     * @param resources
     *         the JSON Example resources that contain the action icon
     * @param asyncRequestFactory
     *         asynchronous request factory for creating the server request
     * @param notificationManager
     *         the notification manager used to display the lines of code per file
     */
    @Inject
    public CountLinesAction(AppContext appContext,
                            JsonExampleResources resources,
                            AsyncRequestFactory asyncRequestFactory,
                            NotificationManager notificationManager) {

        super(appContext,
              "Count JSON Lines of Code\n              "Counts lines of code for all JSON Files in the project\n              resources.icon());

        this.appContext = appContext;
        this.asyncRequestFactory = asyncRequestFactory;
        this.notificationManager = notificationManager;
        this.unmarshaller = new StringMapUnmarshaller();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

	    String url = this.appContext.getDevMachine().getWsAgentBaseUrl() + "/json-example/" + this.appContext.getWorkspaceId() +
this.appContext.getCurrentProject().getRootProject().getPath();

  	  asyncRequestFactory.createGetRequest(url, false).send(
        new AsyncRequestCallback<Map<String, String>>(unmarshaller) {

    		@Override
      	protected void onSuccess(Map<String, String> linesPerFile) {
      		for (Map.Entry<String, String> entry : linesPerFile.entrySet()) {
        		String fileName = entry.getKey();
          	String loc = entry.getValue();
          	notificationManager.notify("File " + fileName + " has " + loc + " lines.\ StatusNotification.Status.SUCCESS,                                   StatusNotification.DisplayMode.FLOAT_MODE);
         	}
         }

         @Override
         protected void onFailure(Throwable exception) {
         	notificationManager.notify(exception.getMessage(), StatusNotification.Status.FAIL, StatusNotification.DisplayMode.FLOAT_MODE);
         }
       });
    }
}
```


The following example shows how Che registers all of the actions for the Git menu.
```java  
DefaultActionGroup git = new DefaultActionGroup(GIT_GROUP_MAIN_MENU, true, actionManager);
actionManager.registerAction("git\ git);
mainMenu.add(git, new Constraints(BEFORE, GROUP_HELP));

DefaultActionGroup commandGroup = new DefaultActionGroup(COMMAND_GROUP_MAIN_MENU, false, actionManager);
actionManager.registerAction("gitCommandGroup\ commandGroup);
git.add(commandGroup);
git.addSeparator();

```
