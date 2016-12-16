---
tags: [ "eclipse" , "che" ]
title: Editors
excerpt: ""
layout: docs
permalink: /:categories/code-editors/
---
This part of the tutorial describes how to extend the Eclipse Che code editor to support a new language. It starts with defining a custom file type and associating it with the specific editor to be opened. Subsequently, we describe how to adapt and enhance the syntax highlighting as well as the code completion of the code editor.

##File Types
In this part of the tutorial, we describe, how new file types can be defined in Che and how those file types can be associated with a specific editor to be opened with. File types can be anything, from a source file to a configuration or properties file. By defining a new file type, it will be displayed in the project explorer using a specific icon. Further, it can be opened and modified with the associated editor. Please note, that Che already provides support for many common file types, so before defining a new one, you should check whether it is already supported.

Defining a new file type consists of three basic steps:
  1. Define the file type itself, including specifying a name, a file extension and an icon
  2. Register the new file type in the file type registry
  3. Optional: Register the file type in the editor registry and thereby associate it with a specific editor to be opened with

A simplified version of a registration of a new file type with the extension `.my` covering exactly these three necessary steps in correct order looks like this:
```java  
FileType myFileType = new FileType("My FileType\ anIcon, "my");
fileTypeRegistry.registerFileType(myFileType);
editorRegistry.registerDefaultEditor(myFileType, defaultTextEditorProvider);
```
In the first line, the new `FileType` is defined, the parameters of its constructor define a name (visible in the UI), an icon, the mime type and a file extension. In line 2, the new file type is registered in Che’s `FileTypeRegistry`. In line 3 the file type is added to Che’s editor registry and thereby associated with Che’s default editor. Please note that step three is optional, as Che will associate all file types with the default text editor by itself. However, this step is necessary, if you later want to implement a custom editor provider.

Following a modular design, and following the guideline for the structure of Che plugins, the creation of the file type and the registration should be kept in two separate components (Java Classes).
The following diagram shows all components of a typical file type registration. The three classes highlighted in dark grey are to be implemented or adapted for the extension.
The class `MyGinModule` is responsible for creating the new file type. The icon for the new file type will go to a GWT resource class (`MyRessources`). Finally, the class `MyExtension` creates a `FileTypeRegistration` in Che's `FileTypeRegistry`.

![image11.png]({{ base }}/assets/imgs/image11.png)
If you haven’t used Gin or dependency injection before, we recommend you have a look at our brief [dependency injection introduction](introduction-1#section-dependency-injection).

First, we define a new class `GinModule` for the instantiation of the custom `FileType`. It enables other classes to access the new file type using dependency injection. When adding more extensions later, the `GinModule` class can also create other components and mappings. So we will not call it `FileTypeGinModule`, but more generically `MyGinModule`. For now, the `GinModule` just provides the custom file type using the ID `MyFileType`. This makes the custom file type available for injection for other components using the annotation `@Named(“MyFileType”)`.

The creation of the file type defines a name, a custom icon and the file extension. If Strings, such as the file extension, are used at other places later, they should be externalized to a common place, for simpler reading, we keep them inlined for now. If those Strings also need to be consumed by a server component later, it should go to a “shared” module, for now, it is kept in the IDE (client) module.
```java  
org.eclipse.che.plugin.myextension.ide.inject.MyGinModule
@ExtensionGinModule
public class MyGinModule extends AbstractGinModule {

	@Override
	protected void configure() {
  	//Nothing to do here, yet
	}

	@Provides
	@Singleton
	@Named("MyFileType")
	protected FileType provideMyFile() {
     return new FileType(MyResources.INSTANCE.icon(), "my");
  }
}
```
The custom file type consumes an icon, which is retrieved from a GWT resource:
```java  
org.eclipse.che.plugin.myextension.ide.MyResources
public interface MyResources extends ClientBundle {
	 MyResources INSTANCE = GWT.create(MyResources.class);

	 @Source("icons/my.svg")
   SVGResource icon();
}

```
The icon itself is a svg image located in the resources of the extension:
[https://github.com/eclipse/che/tree/master/samples/sample-plugin-json/che-sample-plugin-json-ide/src/main/resources/org/eclipse/che/plugin/jsonexample](https://github.com/eclipse/che/tree/master/samples/sample-plugin-json/che-sample-plugin-json-ide/src/main/resources/org/eclipse/che/plugin/jsonexample)

To register the custom file type at Che’s Editor registry, we create another class called `MyExtension`. Again, we name this class more generically, as it will additionally contain other extensions to the IDE. The extension gets the new file type and the `FileTypeRegistry` injected and creates the file type registration.

```java  
org.eclipse.che.plugin.myextension.ide.MyExtension
@Extension(title = "My FileType Extension")
public class MyFileTypeExtension {

	@Inject
	private void registerFileType(
        	final FileTypeRegistry fileTypeRegistry,
        	final @Named("MyFileType") FileType myFileType) {
  	/...
    fileTypeRegistry.registerFileType(myFileType);
	}
}
```
After registering the file type, Che can map the extension to the definition of the file type. Therefore, Che will use the defined icon, if you create a file with the new extension `my` (as shown in the following screenshot). As we have not yet defined any editor type, Che will open the new file type in the default text editor and it will assume, that the content type is plain text.


![image.png]({{ base }}/assets/imgs/image.png)
As you can see in the screenshot above, Che will open any new file type in the default editor. This even works without defining any editor extension. You might want to contribute another editor type for the new file type later. This is done by adding an editor extension and associating the file type with an editor provider. We will cover this more in detail in the section [Code Completion](code-editors#section-code-completion). As we do not have a custom editor provider, yet, the following example code associates the example file type with the default text editor. Please note, this step is redundant in this example, as Che will associate any unknown file type with the default editor anyways.

Since we might want to add more extensions to the editor, again, we use a more generic name for the extension class. As we extend the Che default editor, written in JavaScript and internally referred to a “JSEditor”, we follow the convention of other existing plugins and call the extension `MyJsEditorExtension`. The following extension class gets the `EditorRegistry`, the file type and the `DefaultTextEditorProvider` injected and creates the editor registration. As mentioned, this will have no visible effect in the example. However, if we would replace the Default Text Editor Provider with our own provider (`CustomEditorProvider`), we could extend or replace the editor used for our new file type.

```java  
org.eclipse.che.plugin.myextension.ide.MyJsEditorExtension
@Extension(title = "My JS Editor Extension")
public class MyJsEditorExtension {

	@Inject
	public MyJsEditorExtension(final EditorRegistry editorRegistry,
                           	final @Named("MyFileType") FileType myFile,
                           	final CustomEditorProvider editorProvider) {
     editorRegistry.registerDefaultEditor(myFile, editorProvider);
	}

}

```
So far, we have defined a new file type, which can be opened with the default text editor. Currently, the text editor provides no syntax highlighting and code completion, as it knows nothing about the format or grammar of our new language. The syntax highlighting of the default editor is actually provided by the embedded orion editor (referred to as JSEditor). Please refer to the section [syntax highlighting](code-editors#section-syntax-highlighting) to learn how to extend it and add syntax highlighting for the new file type.

Further, the new file type can only be created using the generic “New” action and enter the extension manually. If you want to define a custom action, visible in the “New” menu, please refer to the section New File Actions.

###JSON File Type (already supported by Che)
The [continuous JSON example](introduction-1#section-the-json-example), which is used throughout this tutorial uses the file type ".json". As Che already registeres a JSON file type out of the box, that means, the necessary registrations, described above for the "my" file type example are already existing in the Che core framework. For reference, the corresponding registrations can be found in the following classes and can be used as another example.

####File Type Definition
```java  
core/ide/che-core-ide-app/src/main/java/org.eclipse.che.ide.filetypes.FileTypeModule (alongside with other file types)

@Provides
@Singleton
@Named("JsonFileType")
protected FileType provideJsonFile(Resources resources) {
   return new FileType(resources.jsonFile(), "json");
}
```
####File Type Registration
```java  
core/ide/che-core-ide-app/src/main/java/org.eclipse.che.ide.core.StandardComponentInitializer

fileTypeRegistry.registerFileType(jsonFile);\
```
####Resources
```java  
core/ide/che-core-ide-app/src/main/java/org.eclipse.che.ide.Resources

@Source("defaulticons/json.svg")
SVGResource jsonFile();
```
Based on these existing registrations, Che will show the JSON file type as shown in the following screenshot. As JSON is a known format to the embedded Orion editor, it will also already provide syntax highlighting.


![image01.png]({{ base }}/assets/imgs/image01.png)
##Code Completion
This part of the tutorial describes how the code-completion of Che’s default code editor can be extended through new suggestions. This also enables you to add code-completion for completely new languages. The following diagram shows all components of a typical file type registration. The classes highlighted in dark grey are to be implemented for the extension.


![image06.png]({{ base }}/assets/imgs/image06.png)
First, we need to register a custom editor provider, `MyEditorProvider`, which plugs in our custom code completion. If you did not register a custom editor provider before, Che will use the `DefaultEditorProvider`, which we now replace. This is done in a class `JsEditorExtension` which contains all potential extensions for the JSEditor (see also [here](introduction-1#section-extension-classes)).

An editor provider is responsible for configuring a specific editor type. Therefore, it provides an `EditorConfiguration`, which is responsible for editor features such as code completion, quick assist or code formatting. To provide custom code completion, the `EditorConfiguration` needs to create a custom `CodeAssistProcessor`.

In the following example, we will describe how to provide a custom code completion to the existing JSON example editor. For simplicity, the code completion will just return a static list of keywords, however, the example can be extended to provide more sophisticated completion processing.

As a first step, we register a custom `JsonExampleEditorProvider`:
```java  
org.eclipse.che.plugin.jsonexample.ide.JsonExampleJsEditorExtension
@Extension(title = "JSON Example Editor")
public class JsonExampleJsEditorExtension {

@Inject
public JsonExampleJsEditorExtension(
final EditorRegistry editorRegistry,
final @Named("JsonFileType") FileType jsonFile,
final JsonExampleEditorProvider editorProvider) {
      editorRegistry.registerDefaultEditor(jsonFile, editorProvider);
   }
}

```
An Editor Provider needs to implement the interface `EditorProvider`. If you want to use the default editor configuration, you can inherit from `AbstractTextEditorProvider`, in this case, you just need to define an ID and  a description. Additionally, you can optionally create a custom `EditorPartPresenter` by implementing the method `getEditor`. If you do not implement `getEditor`, the default editor will be created by `AbstractTextEditorProvider`.
In this tutorial, we create an extension for the existing `DefaultTextEditor`. Therefore, we first retrieve the existing editor from the `DefaultEditorProvider` and initialize it with our new custom editor configuration `JsonExampleEditorConfiguration`, which will add the custom auto-completion to the editor.

```java  
org.eclipse.che.plugin.jsonexample.ide.editor.JsonExampleEditorProvider
/**
 * The JSON Example specific {@link EditorProvider}.
 */
public class JsonExampleEditorProvider extends AbstractTextEditorProvider {

	private JsonExampleEditorConfigurationFactory editorConfigurationFactory;

	/**
 	* Constructor.
 	*
 	* @param editorConfigurationFactory
 	*	the JSON Example Editor configuration factory
 	*/
	@Inject
	public JsonExampleEditorProvider(
         final JsonExampleEditorConfigurationFactory              			editorConfigurationFactory) {
    	  this.editorConfigurationFactory = editorConfigurationFactory;
	}

	@Override
	public String getId() {
     return "JsonExampleEditor";
	}

	@Override
	public String getDescription() {
    	return "JSON Example Editor";
	}

	@Override
    public TextEditor getEditor() {
    	TextEditor editor = super.getEditor();
    	TextEditorConfiguration configuration = this.editorConfigurationFactory.create(editor);
    	editor.initialize(configuration);
    	return editor;
    }
}
```
As we just want to adapt the code completion, the example implementation of the editor configuration inherits from the existing `DefaultTextEditorConfiguration`. The method `getContentAssistantProcessors` is expected to return a mapping from content types to `CodeAssistProcessors`. In our case, if this mapping has exactly one entry registering the custom `JsonExampleCodeAssistProcessor` for the default content type.

```java  
org.eclipse.che.plugin.jsonexample.ide.editor.JsonExampleEditorConfiguration
public class JsonExampleEditorConfiguration extends AutoSaveTextEditorConfiguration {

  private Map<String, CodeAssistProcessor> codeAssist;

  public JsonExampleEditorConfiguration() {
    codeAssist = new LinkedHashMap<>();
    codeAssist.put(DEFAULT_CONTENT_TYPE, new JsonExampleCodeAssistProcessor());
  }

  @Override
  public Map<String, CodeAssistProcessor> getContentAssistantProcessors() {
    return codeAssist;
  }
}\
```
A `CodeAssistProcessor` is responsible for calculating `CompletionProposals`. Therefore, it gets the editor, from which the completion was triggered, the current offset in this editor and a callback to be filled with completion proposals. In this example, we fill the list of proposals with three `SimpleCompletionProposals` (see below) containing static Strings (“firstName”, “lastName” and “age”). In a real completion use case, this simple and static example is to be replaced with a more advanced proposal calculation. If any exception occurs during the computation of the completion proposals, e.g. the server is not reachable, a corresponding message should be returned in `#getErrorMessage`.

```java  
public  class JsonExampleCodeAssistProcessor implements CodeAssistProcessor {

  @Override
  public void computeCompletionProposals(TextEditor editor,
      																	 int offset,
      																	 CodeAssistCallback callback) {

      List<CompletionProposal> proposals = new ArrayList<>();

      proposals.addAll(Arrays.asList(
         new SimpleCompletionProposal("firstName"),
         new SimpleCompletionProposal("lastName"),
         new SimpleCompletionProposal("age")
      ));

      callback.proposalComputed(proposals);
    }

    @Override
    public String getErrorMessage() {
       return null;
    }
}
```
A `CompletionProposal` represents a completion option to be displayed when the users trigger auto-completion in the editor. Therefore, it shows all necessary information for the user and allows to select the right proposal to be applied. The following example shows a code proposal based on a static String, which is retrieved as a parameter in the constructor. This String is used as the displayed name and, along with the defined icon, will be shown to the user in the proposal list. Finally, once the user has selected a proposal which should be applied, the `CompletionProposal` returns the `Completion` (using a callback) in the `#getCompletion` method.

```java  
org.eclipse.che.plugin.jsonexample.ide.editor.SimpleCompletionProposal
public class SimpleCompletionProposal implements CompletionProposal {

  private String proposal;

  public SimpleCompletionProposal(String proposal) {
     this.proposal = proposal;
  }

  @Override
  public Widget getAdditionalProposalInfo() {
     return null;
  }

  @Override
  public String getDisplayString() {
     return proposal;
  }

  @Override
  public Icon getIcon() {
     return new Icon("\ JsonExampleResources.INSTANCE.completion());
  }

  @Override
  public void getCompletion(CompletionCallback callback) {
     callback.onCompletion(new SimpleCompletion(proposal));
  }
}

```

![image13.png]({{ base }}/assets/imgs/image13.png)
A `Completion` is finally responsible for applying a proposal, once the user has selected one. Therefore, after accessing the Document it can apply any text change necessary. In the following example, we append the static String of the `Completion` at the current offset. The `#getSelection` method can optionally set a new selection in the editor after the proposal has been applied. This is done in absolute document coordinates. Returning `null` (as in the example) will not set any new selection.

```java  
org.eclipse.che.plugin.jsonexample.ide.editor.SimpleCompletion
public class SimpleCompletion implements Completion {

	private final String proposal;

	public SimpleCompletion(String proposal) {
     this.proposal = proposal;
	}

	@Override
	public void apply(Document document) {
    document.replace(
        document.getCursorOffset(),
        proposal.length(),
        proposal
     );
	}

	@Override
	public LinearRange getSelection(Document document) {
     return null;
	}
}\
```
In the example, we have shown, how to extend the code completion and used a static list of Strings. However, in a real world example, the calculation of the available proposals might, of course, be more complex. Furthermore, our example completion happens entirely on the client-site, without accessing the server or the workspace. If you need to access dependencies or other resources of a project, please see here to learn how to implement server site services to be used for more advanced code completion.


##Syntax Highlighting
Syntax highlighting allows you to mark characters and keywords in certain colors, based on a given grammar. To enable syntax highlighting in the browser IDE, Che embeds the existing [Orion Editor](https://wiki.eclipse.org/Orion). It already provides a wide range of supported grammars to be used. Please refer to the section “contentType parameter” within [this document](https://wiki.eclipse.org/Orion/How_Tos/Code_Edit) for a list of supported types.

If the orion editor already knows the language you want to support, you need to associate the file extension with the content type defined by orion. As an example, we could associate our a custom file type `.my` ([see here for its definition](code-editors#section-file-types)) with the existing content type `Json`, which is already supported by the Orion editor. Therefore, we add the following line to `org.eclipse.che.ide.jseditor.client.filetype.ExtensionFileTypeIdentifier`:
```java  
org.eclipse.che.ide.jseditor.client.filetype.ExtensionFileTypeIdentifier
	//...
    	this.mappings.put("my\ makeList("application/json"));
	//...

```
By adding this mapping, the embedded Orion editor will now provide the JSON syntax highlighting for our custom file type.


![image01.png]({{ base }}/assets/imgs/image01.png)
To adapt or extend the syntax highlighting of the orion editor, please have a look at the [Orion Documentation](https://wiki.eclipse.org/Orion/Documentation/Developer_Guide/Plugging_into_the_editor#orion.edit.highlighter).

###Add syntax highlighting for your own language
There are two options how you can add a syntax highlighting for your own language:
- Add the highlighting of content type, supported by Orion, but not by IDE.
Add extension with mime type to [File Extension Registry](https://github.com/eclipse/che/blob/master/ide/che-core-ide-api/src/main/java/org/eclipse/che/ide/api/editor/filetype/ExtensionFileTypeIdentifier.java). For example:

```java  
//...
this.mappings.put("ino\ makeList("text/x-c++src"));
...//
```
- You can configure arbitrary new contentTypes and corresponding highlight configuration.
Usage example:
```java  
@Inject
    protected void configureContentType(final OrionContentTypeRegistrant contentTypeRegistrant) {
        // register content type and configure orion
        final String contentTypeId = "text/x-testlang";

        OrionContentTypeOverlay contentType = OrionContentTypeOverlay.create();
        contentType.setId(contentTypeId);
        contentType.setName("Test Language");
        contentType.setExtension("testlang");
        contentType.setExtends("text/plain");

        // highlighting
        OrionHighlightingConfigurationOverlay config = OrionHighlightingConfigurationOverlay.create();
        config.setId("testlang.highlighting");
        config.setContentTypes(contentTypeId);
        config.setPatterns(
                "[\n" +
                        "  {include: \"orion.lib#string_doubleQuote\"},\n" +
                        "  {include: \"orion.lib#string_singleQuote\"},\n" +
                        "  {include: \"orion.lib#brace_open\"},\n" +
                        "  {include: \"orion.lib#brace_close\"},\n" +
                        "  {include: \"orion.lib#bracket_open\"},\n" +
                        "  {include: \"orion.lib#bracket_close\"},\n" +
                        "  {include: \"orion.lib#parenthesis_open\"},\n" +
                        "  {include: \"orion.lib#parenthesis_close\"},\n" +
                        "  {include: \"orion.lib#number_decimal\"},\n" +
                        "  {include: \"orion.lib#number_hex\"},\n" +
                        "  {\n" +
                        "    match: \"\\\\b(?:false|true)\\\\b\\
" +
                        "    name: \"keyword.json\"\n" +
                        "  }\n" +
                "]");

        contentTypeRegistrant.registerFileType(contentType, config);
    }
```
