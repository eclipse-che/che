---
tags: [ "eclipse" , "che" ]
title: Native HTML/JS
excerpt: ""
layout: docs
permalink: /:categories/embed-htmljs/
---
The Che IDE is developed based on GWT. If you want to extend the Che UI with new UI components, the default is to develop those custom components using GWT, too. Please refer to [this tutorial](doc:parts) to learn how to extend Che with new parts (view or editors).

However, as Che is a browser application based on HTML and JavaScript, it is also possible to embed native web components. Those do not have to be based on GWT. This also enables you to reuse any kind of existing UI component in Che. As an existing example, Che embeds the Orion code editor.
# Details  
For instructions on how to build and run an extension, see [Building Extensions](doc:create-and-build-extensions).
```javascript  
Location:     github.com/eclipse/che/samples/sample-plugin-embedjs
Type:         IDE extension
groupId:      org.eclipse.che.sample
artifactId:   che-sample-plugin-embedjs-ide\
```

# Steps  
In this tutorial, we demonstrate, how to embed a minimal HTML/JavaScript component into Che. We will create a custom part, which shows a "Hello World" produced by a simple JavaScript snippet (see screenshot below). You can extend this example, to embed any HTML/JavaScript component you like.
![che_helloworld.png]({{ base }}/assets/imgs/che_helloworld.png)
The following example is based on a simple part, which is opened by a sample action. Therefore, we recommend to get familiar with the implementation of [Parts](doc:parts) and [Actions](doc:actions) first.

The `HelloWorldView` is a default view, in this example, the `HelloWorldView` just creates an empty Panel. The panel will finally be represented by a HTML element in the running browser application.

In the `HelloWorldPresenter` we use the GWT `ScriptInjector` library to inject a custom script (helloWorld.js) into the main window of the browser application. Finally, we use `HelloWorldOverlay` to call the custom JavaScript from within our GWT application. In our example, it will modify the HTML element, which represents the Panel, and will add the "Hello World from JavaScript" text to it.
![Selection_017.png]({{ base }}/assets/imgs/Selection_017.png)
The `HelloWorld.js` contains a simple function, which replaces the text content of an arbitrary element in the DOM. It could also add new elements and therefore embed an arbitrary sub component to running the browser application.

```javascript  
function HelloWorld(element, contents) {
    element.textContent = contents;
};\
```
To add the custom JavaScript function to the running application, we use the GWT ScriptInjector library. We load the JavaScript file and add it to the top window of the application. If adding the script was successful, we directly call the method `#sayHelloWorld` of the `HelloWordView`, which we explain in the following.
```java  
@Singleton
public class HelloWorldViewPresenter extends BasePresenter implements HelloWorldView.ActionDelegate, HasView {

    private final HelloWorldView helloWorldView;

    @Inject
    public HelloWorldViewPresenter(final HelloWorldView helloWorldView) {
        this.helloWorldView = helloWorldView;

        ScriptInjector.fromUrl(GWT.getModuleBaseURL() + Constants.JAVASCRIPT_FILE_ID)
                .setWindow(ScriptInjector.TOP_WINDOW)
                .setCallback(new Callback<Void, Exception>() {
                    @Override
                    public void onSuccess(final Void result) {
                        Log.info(HelloWorldViewPresenter.class, Constants.JAVASCRIPT_FILE_ID + " loaded.");
                        sayHello();
                    }

                    @Override
                    public void onFailure(final Exception e) {
                        Log.error(HelloWorldViewPresenter.class, "Unable to load "+Constants.JAVASCRIPT_FILE_ID, e);
                    }
                }).inject();

    }

    private void sayHello() {
        this.helloWorldView.sayHello("Hello from Java Script!");
    }

```
The `HelloWorldViewImpl` creates an empty panel (which is defined in `che/samples/sample-plugin-embedjs/che-sample-plugin-embedjs-ide/src/main/java/org/eclipse/che/plugin/embedjsexample/ide/view/HelloWorldViewImpl.ui.xml`).

Furthermore, it implement the method `#sayHello` and forwards it to `HelloWorldViewOverlay`.
```java  
public class HelloWorldViewImpl extends BaseView<HelloWorldView.ActionDelegate> implements HelloWorldView {

    interface HelloWorldViewImplUiBinder extends UiBinder<Widget, HelloWorldViewImpl> {
    }

    private final static HelloWorldViewImplUiBinder UI_BINDER = GWT.create(HelloWorldViewImplUiBinder.class);

    @UiField
    FlowPanel helloWorldPanel;

    @Inject
    public HelloWorldViewImpl(PartStackUIResources resources) {
        super(resources);
        setContentWidget(UI_BINDER.createAndBindUi(this));
    }

    @Override
    public void sayHello(String content) {
        HelloWorldViewOverlay.sayHello(helloWorldPanel.getElement(), content);
        helloWorldPanel.setVisible(true);
    }

}\
```
Finally the `HelloWorldOverlay` provides access to the JavaScript function and therefore redirects the Java method to a call of the `HelloWorld` function that we added before. Such overlays are used for communicating between the Che IDE, written in Java/GWT and native JavaScript components, which are embedded into it.
```java  
public class HelloWorldViewOverlay extends JavaScriptObject {

    protected HelloWorldViewOverlay() {
    }

    public final static native void sayHello(final Element element, String message) /*-{
        new $wnd.HelloWorld(element, contents);
    }-*/;

}\
```

# Use  
This particular extension adds an action to the main context menu group. This is the group that appears when you right click on the project tree. To verify that your plugin is installed, you can also check the Profile > Preferences > Plugins > List to verify that the "Hello world from JavaScript example" plugin has been installed.
![Capture_embed.PNG]({{ base }}/assets/imgs/Capture_embed.PNG)
