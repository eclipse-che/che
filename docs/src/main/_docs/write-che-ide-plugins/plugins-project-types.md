---
tags: [ "eclipse" , "che" ]
title: Project Types
excerpt: ""
layout: docs
permalink: /:categories/project-types/
---
{% include base.html %}
Project types allow you to provide custom project behavior for a certain language. Additionally, they allow you to specify specific project templates, which can be instantiated and already contain language specific content. Further, they allow the user to specify language specific properties for a project, e.g. compiler settings or dependencies. Finally, specific actions, e.g. in the context menu, can be associated with a project type.

In this part of the tutorial, we describe how to define a custom project type, how to provide a custom creation wizard, and how to add project-specific actions.

##Custom Project Type
In this part of the tutorial, we describe how to define a new custom project type including a project initialization (e.g. to add default content). The following diagram shows all components of a project type registration. The classes highlighted in dark grey are to be implemented for the extension.

![ProjectType.png]({{ base }}/assets/imgs/ProjectType.png)
The custom `ProjectTypeDef` implementation defines the actual project type. Therefore, it defines an ID, a name and some configuration options. As the ID is referenced from other classes, it is retrieved from a shared constant class `MyConstants`.
A custom `ProjectCreateHandler` is responsible for creating a new project of the custom type. As an example, it can create some default files on project creation.
`ProjectCreateHandler` is a subtype of ProjectHandler, other sub types, e.g. `PostImportProjectHandler` and `ProjectInitHandler` provide further hooks to configure projects.

Both, the custom `ProjectTypeDef` as well as the custom `ProjectCreateHandler` are bound by a `GuiceModule` to make them available for the Che Framework. Please note that all these components are part of a server plugin. Necessary adaptations within the IDE, e.g. the extension of the “New” menu are done automatically by Che.


As an example, we will describe in the following how to add a simple project type for managing JSON files, although in a real use case JSON files are usually embedded into other projects (e.g. a JavaScript project). We will also add a default initialization to the project type, which already creates a new JSON file in any created project.
As a first step, we implement a custom subclass of `ProjectTypeDef` (see code example below). Its constructor calls the default super constructor to define the ID and the name for the custom project type.
Further, it specifies with the remaining three boolean parameters:
* **primary**=true: That the project can be a top-level project, meaning that it can be created on the root level of a workspace
* **mixin**=false: That the project cannot be embedded into other projects (as sub-projects)

After specifying the project, we add a constant and a variable definition to the project type. Constants can not be changed, once they are defined and therefore contain static information about the project type. In our example, we add the information, that the project’s language is “json”. The first parameter specifies a key, the second a description of the Constant, and the third the corresponding value.
```java  
Server-side: org.eclipse.che.plugin.jsonexample.projecttype.JsonExampleProjectType
public class JsonExampleProjectType extends ProjectTypeDef {

	@Inject
	public JsonExampleProjectType() {
		super(JSON_EXAMPLE_PROJECT_TYPE_ID, "JSON Example\ true, false);
  	addConstantDefinition(LANGUAGE, LANGUAGE, JSON_EXAMPLE_PROJECT_TYPE_ID);
  	addVariableDefinition("json-schema-ref\ "Referenced base schema\ /*required*/ true);
	}
}
```
Variables can be changed, e.g. to store values that the user enters on project creation. In the example, we define a custom variable to store a reference to a JSON schema. We will allow the user to set this variable in a custom project wizard in the corresponding part of this tutorial. You can define your own variables to store project specific properties. All String constants of the following code example are defined in a shared constant class, which is listed below.
```java  
org.eclipse.che.plugin.jsonexample.shared.Constants
public final class Constants {

	/**
 	* Language attribute name.
 	*/
	public static final String LANGUAGE            	= "language";

	/**
 	* Language attribute value.
 	*/
	public static final String JSON_EXAMPLE_LANG   	= "json";

	/**
 	* JSON Example Project Type ID.
 	*/
	public static final String JSON_EXAMPLE_PROJECT_TYPE_ID = "json-example";

	/**
 	* JSON Example Category.
 	*/
	public static final String JSON_EXAMPLE_CATEGORY  	= "JSON Example";

	/**
 	* JSON Schema reference attribute name.
 	*/
	public static final String JSON_EXAMPLE_SCHEMA_REF_ATTRIBUTE = "json-schem-ref";

	private Constants() {

	}
}

```

To make our new project type available in Che, we need to register it using Guice. The following example code registers the `JsonExampleProjectType` from above as a `ProjectTypeDef`. Che will automatically pick up all bound `ProjectTypeDefs`. Please see our [Dependency Injection Basics] (doc:dependency-injection-basics) section for a general introduction of this mechanism.
```java  
org.eclipse.che.plugin.jsonexample.inject.JsonExampleGuiceModule
@DynaModule
public class JsonExampleGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
  	Multibinder<ProjectTypeDef> projectTypeDefMultibinder = newSetBinder(binder(),
  	ProjectTypeDef.class);
    projectTypeDefMultibinder.addBinding().to(JsonExampleProjectType.class);
  }
}
\
```
By defining the new project type, Che will add a new entry in the “New” menu of the IDE and allow us to create a new and empty project:


![image03.png]({{ base }}/assets/imgs/image03.png)

Typical project types often need to be initialized with some default content, e.g. some files. This can be done by implementing a `CreateProjectHandler` (subtype of `ProjectHandler`). In the method `#onProjectCreate`, you can access the base folder, as well as the attributes and options of the project.

In the following example, we will create the following files: a "person.json" file with some default content that will be stored in a folder named "myJsonFiles" as well as a "package.json" file, which we’ll need later on. The method `#getProjectType` needs to provide the project type ID to allow Che to map the `ProjectHandler` to the correct type.

```java  
org.eclipse.che.plugin.jsonexample.generator.JsonExampleProjectGenerator
public class JsonExampleCreateProjectHandler implements CreateProjectHandler {

	private static final String FILE_NAME = "package.json";

	@Override
	public void onCreateProject(FolderEntry baseFolder,
                            	Map<String, AttributeValue> attributes,
                            	Map<String, String> options) throws /.../
  {
		InputStream packageJson = null;
    InputStream personJson = null;
    try {
    	FolderEntry myJsonFiles = baseFolder.createFolder("myJsonFiles");
    	packageJson = getClass().getClassLoader()
                .getResourceAsStream("files/default_package");
      personJson = getClass().getClassLoader()
                .getResourceAsStream("files/default_person");
     	baseFolder.createFile(FILE_NAME, packageJson);
      myJsonFiles.createFile("person.json\ personJson);
    } finally {
     	Closeables.closeQuietly(packageJson);
     	Closeables.closeQuietly(personJson);
    }
	}

	@Override
	public String getProjectType() {
  	return Constants.JSON_EXAMPLE_PROJECT_TYPE_ID;
	}
}

```
Finally, the ProjectHandler needs to be bound using Guice just as the project type was bound before:

```java  
org.eclipse.che.plugin.jsonexample.inject.JsonExampleGuiceModule

/...
Multibinder<ProjectHandler> projectHandlerMultibinder = newSetBinder(binder(),
     ProjectHandler.class);
projectHandlerMultibinder.addBinding().to(JsonExampleCreateProjectHandler.class);
/...
\
```
Once the ProjectHandler has been added and executed, the example project will already contain the files  in the IDE:

![image08.png]({{ base }}/assets/imgs/image08.png)
##Project Creation Wizard
Project creation wizards are executed once the user creates a new project. They allow you to enter general properties (such as a name and a description), but also project-specific properties (e.g. a compiler option, a project dependency, etc.). Without providing a specific project creation wizard, Che already allows you to enter the general properties available for all projects as shown in the following screenshot for the JSON example project type we have defined in the previous section of the tutorial:


![image03.png]({{ base }}/assets/imgs/image03.png)

In this section, we will describe how to extend the default project creation wizard with a new page allowing it to enter an additional property. As part of the JSON example, we will allow the user to enter the URL of a JSON Schema. We will later use the schema to validate JSON files on the server.
Therefore, we will add a new page to the JSON project creation wizard allowing to enter the schema url property:

![image14.png]({{ base }}/assets/imgs/image14.png)
This page serves as a simple example, it can be adapted for any other project specific property.

The following diagram shows all components for the extension of the project wizard. The classes highlighted in dark grey are to be implemented for the project wizards extension.

![ProjectType-JsonExample.png]({{ base }}/assets/imgs/ProjectType-JsonExample.png)
Before we look at the detailed implementations, we will first give an overview of all participating components.
As a first step, we need to implement a `ProjectWizardRegistrar`. It holds a set of `AbstractWizardPages`. These pages are added to the default wizard and displayed during project creation. Our implementation of a `ProjectWizardRegistrar` is in `JsonExampleProjectWizardRegistrar` and contributes one wizard page (see its method `#getWizardPages`) which will contain exactly one field for entering a JSON schema URL.

The page itself is implemented in `SchemaUrlWizardPage`. To actually display a UI, it configures a GWT view defined in `SchemaUrlPageViewImpl` and its corresponding `SchemaUrlPageViewImpl.ui.xml`. Furthermore, the wizard page will create and configure a handler for URL changes called `SchemaUrlChangedDelegate`.

Now all required classes are set up and the actual runtime behavior can be performed. Whenever the user performs a change in the textbox for the schema URL, GWT will trigger the method `#onSchemaUrlChanged` in `SchemaUrlPageViewImpl` since it is annotated as a handler for changes on this textbox. The method will then notify the `SchemaUrlChangedDelegate`. The `SchemaUrlChangedDelegate` will in turn write the changed URL into a `ProjectConfigDto` owned by the `SchemaUrlWizardPage`.

Finally, to wire everything up with Gin, all we need to do is to define a module to register our class `JsonExampleProjectWizardRegistrar` as an implementation of `ProjectWizardRegistrar`:

```java  
org.eclipse.che.plugin.jsonexample.ide.inject.JsonExampleModule
@ExtensionGinModule
public class JsonExampleModule extends AbstractGinModule {

	@Override
	protected void configure() {
    	GinMultibinder
            	.newSetBinder(binder(), ProjectWizardRegistrar.class)
            	.addBinding()
            	.to(JsonExampleProjectWizardRegistrar.class);
       }
    	//...
}

\
```
Now let us look at the implementation of all required classes in more detail.
The `JsonExampleProjectWizardRegistrar` is responsible for setting up the `SchemaUrlWizardPage` as one of its wizard pages. To do this, it requests a provider for a `SchemaUrlWizardPage` injected in its constructor. The provider is just a wrapper around the actual wizard page which is required by the Che framework. In the method `#getWizardPages` we can then just return a list of providers for wizard pages containing only the injected provider.

In addition to setting up the wizard page we need to declare the project type and category for which the project wizard is responsible for.

```java  
org.eclipse.che.plugin.jsonexample.ide.project.JsonExampleProjectWizardRegistrar
public class JsonExampleProjectWizardRegistrar implements ProjectWizardRegistrar {
	private final List<Provider<? extends WizardPage<ProjectConfigDto>>> wizardPages;

	@Inject
	public JsonExampleProjectWizardRegistrar(
         Provider<SchemaUrlWizardPage> wizardPage) {
  	wizardPages = new ArrayList<>();
    wizardPages.add(provider);
	}

	@NotNull
	public String getProjectTypeId() {
  	return Constants.JSON_EXAMPLE_PROJECT_TYPE_ID;
	}

	@NotNull
	public String getCategory() {
  	return JSON_EXAMPLE_CATEGORY;
	}

	@NotNull
	public List<Provider<? extends WizardPage<ProjectConfigDto>>> getWizardPages()	{
  	return wizardPages;
	}
}
\
```

The `SchemaUrlWizardPage` class defines the actual wizard page for entering a schema URL. In the constructor it requires the injection of a view for displaying the UI of the page called `SchemaUrlPageViewImpl`. In the method `#go`, which is called when the page is about to be displayed, it will set this view as the only widget on the page and pass a new `SchemaUrlChangedDelegate` to the view. The view will later use this delegate to trigger  changes on the page's `ProjectConfigDto` whenever something is entered into the schema URL text box on the view.

```java  
org.eclipse.che.plugin.jsonexample.ide.project.SchemaUrlWizardPage
public class SchemaUrlWizardPage extends AbstractWizardPage<ProjectConfigDto> {

	private final SchemaUrlChangedDelegate view;

	@Inject
	public SchemaUrlWizardPage(SchemaUrlPageViewImpl view) {
  	this.view = view;
	}

	@Override
	public void go(AcceptsOneWidget container) {
  	container.setWidget(view);
  	view.setDelegate(new SchemaUrlChangedDelegate (this.dataObject));	  
  }

}

\
```
The `SchemaUrlChangedDelegate` receives a `ProjectConfigDto` in its constructor which holds all the values that are defined during project creation including the schema URL. Whenever its `#schemaUrlChanged` method is fired, it will write the new value into the `ProjectConfigDto`.

```java  
org.eclipse.che.plugin.jsonexample.ide.project.SchemaUrlChangedDelegate   
public class SchemaUrlChangedDelegate {

	private ProjectConfigDto dataObject;

	public SchemaUrlChangedDelegate(ProjectConfigDto dataObject) {
  	this.dataObject = dataObject;
	}

	public void schemaUrlChanged(String value) {
  	dataObject.getAttributes().put("json-schema-ref\
           Collections.singletonList(value));
	}
}


```

`SchemaUrlPageView` is just a marker interface required by the framework to declare that our `SchemaUrlPageViewImpl` is an implementation of a view with a `SchemaUrlChangedDelegate`.
```java  
org.eclipse.che.plugin.jsonexample.ide.project.SchemaUrlPageView   
public interface SchemaUrlPageView extends View<SchemaUrlChangedDelegate> {}
\
```

`SchemaUrlPageViewImpl` is the class which will actually create the UI with a TextBox for entering the schema URL. It is a GWT Composite with its contents defined in `SchemaUrlPageViewImpl.ui.xml`.
To receive all changes of the schema URL in the UI it declares a method `#onSchemaUrlChanged` with an annotation @UiHandler("schemaUrl"). This annotation defines that the method is to be called whenever the text in the schemaUrl text box as defined in `SchemaUrlPageViewImpl.ui.xml` is changed.
The method will just forward any call to the `SchemaUrlChangedDelegate` which was configured earlier by the `SchemaUrlWizardPage`.
In its constructor the view gets a `JsonExamplePageViewUiBinder` injected which is used to create and bind the UI defined in `SchemaUrlPageViewImpl.ui.xml`.
This requires you to define `JsonExamplePageViewUiBinder` as a marker interface extending `UiBinder<DockLayoutPanel, SchemaUrlPageViewImpl>`.

More about declarative UIs with GWT UI binder can be found on the [GWT homepage](http://www.gwtproject.org/doc/latest/DevGuideUiBinder.html).

```java  
org.eclipse.che.plugin.jsonexample.ide.project.SchemaUrlPageViewImpl
class SchemaUrlPageViewImpl extends Composite implements SchemaUrlPageView {

	interface JsonExamplePageViewUiBinder extends UiBinder<DockLayoutPanel, SchemaUrlPageViewImpl> {
	}

	@UiField
	TextBox schemaUrl;

	private SchemaUrlChangedDelegate delegate;

	@Inject
	public SchemaUrlPageViewImpl(JsonExamplePageViewUiBinder uiBinder) {
  	initWidget(uiBinder.createAndBindUi(this));
	}

	/** {@inheritDoc} */
	@Override
	public void setDelegate(SchemaUrlChangedDelegate delegate) {
  	this.delegate = delegate;
	}

	@UiHandler("schemaUrl")
	void onSchemaUrlChanged(KeyUpEvent event) {
   	delegate.schemaUrlChanged(schemaUrl.getValue());
	}
}
```

```xml  
SchemaUrlPageViewImpl.ui.xml
<ui:UiBinder xmlns:ui='urn:ui:com.google.gwt.uibinder'
         	xmlns:g='urn:import:com.google.gwt.user.client.ui'
         	xmlns:ide='urn:import:org.eclipse.che.ide.ui'>
	<g:DockLayoutPanel unit="PX" >
    	<g:north size="200">
        	<g:FlowPanel ui:field="panel">
            	<g:FlowPanel height="90px" >
                	<g:Label text="JSON Schema URL" />
                	<ide:TextBox ui:field="schemaUrl"
                             	tabIndex="0"
                             	debugId="file-createProject-schemaUrl"/>
                	<g:Label ui:field="labelUrlError" width="100%" 		wordWrap="true"/>
            	</g:FlowPanel>
        	</g:FlowPanel>
    	</g:north>
	</g:DockLayoutPanel>
</ui:UiBinder>

```
By adapting the `SchemaUrlPageViewImpl.ui.xml` you can customize the layout of the final wizard page. The example page will look like this:
![image14.png]({{ base }}/assets/imgs/image14.png)
##Project-specific Actions
Actions allow you to add custom behavior to the Che IDE. They can be placed in menus, toolbars or context menus. Some actions shall only be available on a specific project type. In the JSON example, we place two actions in the context menu of the defined project type. The screenshot shows a project-specific `HelloWorldAction`, as well as another project specific action implemented in the section [Server/Workspace Access](doc:serverworkspace-access).


![image00.png]({{ base }}/assets/imgs/image00.png)
