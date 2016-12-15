---
tags: [ "eclipse" , "che" ]
title: Dependency Injection
excerpt: ""
layout: docs
permalink: /:categories/dependency-injection-basics/
---
In this section, we briefly introduce the usage of dependency injection in Che, on the client and on the server side. If you are already familiar with Guice and Gin, you might want to skip this part.

Che uses dependency injection to wire the different components, in order to create objects as well as register and retrieve extensions. Therefore, dependency injection is technically the core mechanism of communicating with the framework and connecting custom extensions. This includes accessing framework services and objects (e.g. a file type or a [file type registry](code-editors)) and providing custom objects to the framework (e.g. a [custom wizard](project-types)).

Che uses the existing dependency injection framework [Guice](https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=0ahUKEwi69oT_sO3MAhXKOxQKHYMIB58QFggcMAA&url=https%3A%2F%2Fgithub.com%2Fgoogle%2Fguice&usg=AFQjCNHss97LwiVZ_GVp7HlDZgZYvWIbyQ&bvm=bv.122448493,d.bGg) on the server-side and the GWT version of Guice, [Gin](https://github.com/google-code-export/google-gin), on the client-side.

In general, there are two use cases for dependency injection: consuming objects and providing objects.


![image07.png]({{ base }}/assets/imgs/image07.png)
Please note that when extending Che, DI consumer and provider can be either in your custom extension or within the Che framework. As an example, if you want to provide a new wizard to be used in the IDE, you will create an object provider, which provides the wizard. Che implements an object consumer, which picks up the wizard class and uses it. In turn, if you want to access a service provided by Che, your extension will be the object consumer.

The main goal of using dependency injection is to decouple object provider and object consumer. Both parties just need to know about the object they consume/provide. The object is identified by its type (typically a Java interface) and optionally an additional key.
In the following, we first describe how to consume objects (in Guice and Gin) and subsequently, how to provide objects.


##Consuming Objects

Required objects can be injected in any class that is instantiated by the framework. If a custom component requires objects, e.g. a service, it can be injected as a parameter. This can be done in the constructor or in methods of a class. If the parameter is required for a class to operate, we recommend using the constructor for injection. To get parameters injected in a method or constructor, it is marked with the annotation `@Inject` (see code example below). By adding the annotation, all parameters of a constructor/method will be resolved by the framework and passed in through the initialization of the class.

The dependency injection framework needs to know how to identify the correct object to be used as a parameter. There are two essential ways of specifying the parameters as a consumer
  * First, if you just specify a parameter of a certain type (in the example `SomeService`), the framework will search for an object of that type. This will only work, if there is exactly one object of this type in the context, which is typically true for services.

  * Second, if there can be several objects of the required type and you want a specific object out of those, you can additionally specify a key using the annotation `@Named`.

In the following example, for the second parameter, the framework will look for an object which is of type `MyClass` has been explicitly registered with the key `MyID`. Please [see the following](#section-providing-objects) section how to provide objects to be consumed that way.
```java  
public class MyClass {

  private MyOtherClass myOtherClass;

	@Inject
	public MyClass(final SomeService someService,
                           	final @Named("MyID") MyClass myClass) {
    someService.someMethod(myClass);
		this.other = new MyOtherClass(myClass);
	}

  // do somehting with myOtherClass;
}
```
Please note, that dependency injection is only available for objects which are instantiated by the dependency injection framework. In the example above, the class `MyOtherClass` is instantiated using plain Java, therefore it is not possible to use `@Inject` in its constructor.

##Providing Objects

Implementing an object provider serves two purposes when writing an extension:
  * First, you can consume the objects that you provide from within other custom classes.

  * Second, the provided classes can be consumed by the Che framework.

As an example, if you provide a wizard class, it will be integrated by the Che IDE. Therefore, dependency injection is a core extension mechanism of Che.

To provide custom objects, you implement a module class. It is responsible to create the objects to be injected as well as register them using the type and optionally a key. Depending on the general structure of your extension, you could add as many modules as you like, however, most extensions use only one module for the client (Gin) and one for the server part (Guice).

The following code example shows a simple Guice module. All Guice modules inherit from AbstractModule and are marked with the annotation `@DynaModule`, which registers the module itself to be executed by Guice. The mandatory method `#configure` is responsible for the registration of objects. We will not go into detail about all the different options of Guice/Gin, but focus on relevant use cases in Che. In the following code example, we register a custom object (`CustomObject`), which implements an existing Che type (`ExistingCheType`). The Che type defines an extension point for Che, e.g. a wizard.
```java  
public class CustomObject implements ExistingCheType {
  // ...
}\
```
Now, we register our custom object using the type and therefore make it available for the Che framework. To register and to retrieve the object, the type `ExistingCheType` is used as an identifier. In the example, there can be an arbitrary number of objects implementing `ExistingCheType`, so Che will retrieve a set of objects. To register the object, we create a new `Set Binder` for the type `ExistingCheType`. Then, we add a binding and register the custom object. The `CustomObject` will be instantiated by the framework using dependency injection. Therefore, the `@Inject` annotation can be used in the constructor of `CustomObject`.

```java  
import org.eclipse.che.inject.DynaModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

@DynaModule
public class MyGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ExistingCheType.class)
                   .addBinding()
                   .to(CustomObject.class);
    }
}\
```
Gin modules inherit from `AbstractGinModule` and use the `@ExtensionGinModule` annotation. Gin has a different [binding mechanism than Guice](https://code.google.com/p/google-gin/wiki/GinTutorial), however, for the typical use case, the code would look the same:
```java  
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

@ExtensionGinModule
public class MyGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        GinMultibinder.newSetBinder(binder(), ExistingCheType.class)
                      .addBinding()
                      .to(CustomObject.class);
    }
}\
```
As an alternative to the registration above, objects can also be registered using methods marked with the `@Provides` annotation. The following example provides a simple object, which only needs to be instantiated once (`@Singleton`). In this example, the registration additionally contains a key specified by the `@Named` annotation. Please note that in this case, the `CustomObject` is created manually, so no dependency injection can be used within it. The following method is placed in your custom Gin/Guice module.

```java  
@Provides
@Singleton
@Named("MyID")
protected FileType provideMyClass() {
    	return new MyClass();
}
```
The examples of dependency injection cover all basic use cases to understand the following extension tutorial. If you want to learn more about the different types of Guice bindings, please refer [to this page](https://github.com/google/guice/wiki/Bindings).


##Extension Classes

Besides the extensibility using dependency injections, many custom extensions need to call some Che services or registries on start-up. Therefore, most extensions contain a central class called `Extension`. To register those classes, Che provides the custom annotation `@Extension`, which also allows to define a title for the extension. A common example for a class which gets instantiated by Che and which requires parameters is the `Extension` class.

Extension classes will automatically be picked-up by Che on start-up and all methods will be executed using dependency injection. In the following example, the extension class connects `SomeParameter` to `SomeService`.


```java  
@Extension(title = "My Extension")
public class MyExtension {

	@Inject
	private void myInitialization(
        	final SomeService someService,
        	final SomeParameter someParameter) {
    		someService.doSth(someParameter);
	}
}
```
