---
tags: [ "eclipse" , "che" ]
title: Parts
excerpt: "Part API"
layout: docs
permalink: /:categories/parts/
---
Parts represent the content of the Che workbench, i.e. views and editors within the IDE. Che already provides various parts such as the project explorer, the output console, the build result view, file outline and the code editor. In this part of the tutorial, we describe how to implement a custom view and embed it into the Che IDE. Furthermore, we demonstrate how to open and hide views.

## Create a custom Part
Creating a part in Che consists of two four components, which are marked in grey in the diagram below. In this section, we provide a general overview, in the following sections, we describe the concrete implementation more in detail.
The central component is the implementation of the view itself (`MyViewImpl`). It will create all the UI widgets, which are shown within a part. `MyViewImpl` inherits from `BaseView`, a base implementation of common functionality for all views provided by Che. If the view needs to be accessed by other components, e.g. to set a selection, public methods should be extracted to an interface (`MyView`). To allow other components to get an instance of `MyView`, the interface is bound to the implementation within `MyGinModule`. See the section [Dependency Injection Basics](doc:dependency-injection-basics) for more details about this.
As mentioned before, the view implementation is responsible for the content of a view. The integration into the Che IDE, including configuring the tab (title, icon, etc.) is done by a part presenter (`MyPartPresenter`), which inherits from `BasePresenter`. Part presenter are called by Che or a custom action to interact with a part, e.g. to open it or to fill it with content. The part presenter forwards relevant calls to the implementation of a view (encapsulated by the interface).


![Selection_005.png]({{ base }}/assets/imgs/Selection_005.png)
In the following sections, we describe the implementation of the mentioned components more in detail. As an example, we create a part displaying "Hello World" and define an action to open it.

###Implementing a View
In this section, we describe the implementation of a simple "Hello World" view. The implementation is shown in the following listing. All views in Che inherit from `org.eclipse.che.ide.api.parts.base.BaseView`, which implements basic features for views embedded into the Che IDE. The super constructor requires the `PartStackUIResources` which we get injected as a parameter.
Views in Che are implemented using GWT. Therefore, we can use any GWT widgets or framework capabilities to actually implement the views. In the following example, we simply create a label and set its text. To implement more complex views and use other GWT features, such as describing the UI using XML, please refer to the [GWT project page](http://www.gwtproject.org/).

In the last line of the example, we call the method `#setContenWidget` of the base class to specifiy the root widget to be shown in the view. In our case, this is the `Label`, if you create a more complex layout of widgets, this would be the root container of the view.
```java  
package org.eclipse.che.plugin.parts.ide.helloworldview;

import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;

public class HelloWorldViewImpl extends BaseView<HelloWorldView.ActionDelegate> implements HelloWorldView {

    @Inject
    public HelloWorldViewImpl(PartStackUIResources resources){
        super(resources);
        Label label = new Label("Hello World");
        setContentWidget(label);
    }


}
```
As mentioned in the introduction of this section, an explicit interface should defined, when implementing a view, encapsulating all interaction with other components (see following listing). Therefore, the interface contains all methods, which shall be accessible by other components. In the following example, the interface defines a method `#setVisible` to allow controlling the visibility of the view. This method is already implemented by `BaseView` so we do not need to implement it in `HelloWorldViewImpl`. If you need to provide any other methods for a view, e.g. to pass in some input parameters to be shown, you should extend the view interface accordingly.

Following the GWT pattern, the view interface also defines an `ActionDelegate`. This interface can be implemented by components, which want to listen to events triggered with the view, e.g. a button click. Our `HelloWorldView`is currently not triggering any actions, so the interface is empty. Please see the section "Interacting from within views" below for more details.
```java  
package org.eclipse.che.plugin.parts.ide.helloworldview;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

public interface HelloWorldView extends View<HelloWorldView.ActionDelegate> {


    void setVisible(boolean visible);

    interface ActionDelegate extends BaseActionDelegate {

    }
}\
```
Finally, we have to make our view available for other components, using dependency injection. This is done in `MyGinModule`, which can contain other bindings, too. Please see the section [Dependency Injection Basics](doc:dependency-injection-basics) for more details about this binding.
```java  
package org.eclipse.che.plugin.parts.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.plugin.parts.ide.helloworldview.HelloWorldView;
import org.eclipse.che.plugin.parts.ide.helloworldview.HelloWorldViewImpl;

@ExtensionGinModule
public class MyGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(HelloWorldView.class).to(HelloWorldViewImpl.class);
    }

}\
```
###Implementing a Part Presenter
To connect the view implementation to the Che workbench, we need to implement a part presenter. It defines, how a view is embedded into Che (e.g. a title and an icon). Furthermore, it handles all interactions with the view. This goes in both directions. As a first example, if you want to hide a view, you will call the presenter. As a second example, if you click a button within a view, which should trigger something in Che, the presenter will receive this event and trigger the specified action.

The following listing shows the `HelloWorldPresenter` for the previous example view. It retrieves the `HelloWorld`view using dependency injection in its constructor. The following methods define, how the view is presented as a tab in Che: a title, an icon and a tooltip. The method `#setVisible` delegates to the view itself.
The method `#go` is called, when a view is opened. As a parameter, it receives a callback, which expects a view implementation to be set. With this call, the view implementation is wired to the Che workbench.
```java  
package org.eclipse.che.plugin.parts.ide.helloworldview;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.plugin.parts.ide.SamplePartsResources;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Presenter for the sample Hello World View.
 */
@Singelton
public class HelloWorldPresenter extends BasePresenter {

    private HelloWorldView view;

    @Inject
    public HelloWorldPresenter(HelloWorldView view){
        this.view = view;
    }

    @Override
    public String getTitle() {
        return "Hello World View";
    }

 		@Override
    public SVGResource getTitleImage() {
        return (SamplePartsResources.INSTANCE.icon());
    }

  	@Override
    public String getTitleToolTip() {
        return "Hello World Tooltip";
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}

```
###Interacting from within a view
To trigger any behavior from with views, the `ActionDelegate` is used as a receiver of events following the GWT MVP pattern. Therefore, you extend the interface by the required methods, in the following listing a method `#onButtonClicked`.
```java  
/** Required for delegating functions in view. */
public interface ActionDelegate extends BaseActionDelegate {
  /** Performs some actions in response to a user's clicking on Button */
  void onButtonClicked();
}\
```
The `ActionDelegate` interface has to be implemented and provided to the view. For a part, the part presenter is a good component to do both, especially, if the relevant operations to be triggered are related to the Che workbench or to Che services. Therefore, the part presenter implements the interface `MyView.ActionDelegate`, implements the defined method and sets itself as a delegate (see listing below).
```java  
@Singelton
public class MyPartPresenter extends BasePresenter implements MyView.ActionDelegate {

    private MyView view;

    @Inject
    public MyPartPresenter(MyView view){
        this.view = view;
        view.setDelegate(this);
    }

  	public void onButtonClicked(){
    	//Do sth.
    }\
```
Finally, the action delegate can be called from within the view implementation, as shown below.
```java  
public class MyViewImpl extends BaseView<HelloWorldView.ActionDelegate> implements MyView {

/...

public void onButtonClicked(ClickEvent event) {
    delegate.onButtonClicked();
}\
```
##Opening Parts

To open parts, the service `WorkspaceAgent` is used. It provides a method `#openPart` which accepts two parameters:
* The part presenter of the part to be opened
* The location, where the part is to be opened

The following locations are supported by Che:
* `EDITING`: area just above the editor, like a file tab
* `NAVIGATION`: area on the left to project explorer
* `TOOLING`: area to the right of the editor
* `INFORMATION`: area under the editor, 'console' area

After a pat has been opened, it must be activated to ensure that it gets visible and receives the focus. This is done using `WorkspaceAgent#setActivePart`.
The following code example shows an action, which opens the "Hello World" part defined before. Please see the section [Actions](doc:actions) for more details about the implementation of actions.
```java  
package org.eclipse.che.plugin.parts.ide.helloworldview;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;

/**
 * Action for showing a the Hello World View.
 */
@Singleton
public class HelloWorldViewAction extends Action {

    private WorkspaceAgent workspaceAgent;
    private HelloWorldPresenter helloWorldPresenter;

    /**
     * Constructor.
     *
     */
    @Inject
    public HelloWorldViewAction(WorkspaceAgent workspaceAgent, HelloWorldPresenter helloWorldPresenter) {
        super("Show Hello World View");
        this.workspaceAgent = workspaceAgent;
        this.helloWorldPresenter = helloWorldPresenter;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        workspaceAgent.openPart(helloWorldPresenter, PartStackType.INFORMATION);
        workspaceAgent.setActivePart(helloWorldPresenter);
    }
}
```
