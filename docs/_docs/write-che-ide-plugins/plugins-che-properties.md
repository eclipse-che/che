---
tags: [ "eclipse" , "che" ]
title: Properties
excerpt: ""
layout: docs
permalink: /:categories/che-properties/
---
# Referencing Properties in Extensions  
You can reference properties in Che extensions that you author. Configuration parameters may be injected with a constructor or directly in the fields. The parameter of the field must be annotated with `@javax.inject.Named`. The value of the annotation is the name of the property. For example, if the configuration property is: `data_file:/home/user/storage` then in your extension code:
```java  
public class MyClass {
  ...
  @Inject
  public MyClass(@Named("data_file") File storage) {
      ...
  }
}
```
or
```java  
public class MyClass {
  @Inject
  @Named("data_file")
  private File storage;
  ...
}
```
All system properties and environment variables may be injected into your extensions with prefixes `sys.` and `env.`, respectively. So, for example, environment variable `HOME` name must be `env.HOME`. Here is an example of how to inject the value of system property `java.io.tmpdir` and value of environment variable `HOME`.
```java  
public class MyClass {
  @Inject
  public MyClass(@Named("sys.java.io.tmpdir") File tmp, @Named("env.HOME") File home) {
      ...
  }
}
```
Any value can be converted into a `java.lang.String` Java type. You can also directly convert properties to the following Java types:
  * `boolean`
  * `byte`
  * `short`
  * `int`
  * `long`
  * `float`
  * `double`
  * `java.net.URI`
  * `java.net.URL`
  * `java.io.File`
  * `String[]` (value is a comma separated string)
# Workspace Extension Properties  
Each workspace is a separate runtime, and has at least one development agent that runs as a miniaturized Che server within the workspace. That agent has its own properties that can be configured as well. If you are authoring custom workspace extensions that are deployed within Che's agent in the workspace, you can customize.
