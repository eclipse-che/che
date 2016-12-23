---
tags: [ "eclipse" , "che" ]
title: Java+Maven in Che
excerpt: "Che was written in Java. Experience the rich Intellisense and Java tools in this tutorial."
layout: tutorials
permalink: /:categories/maven/
---
{% include base.html %}
# 1. Start Che  
Use your SaaS account for the following, or if you have [installed Che](https://eclipse-che.readme.io/v5.0/docs/che-getting-started), open a terminal and use the Che startup script:
```shell  
# Launch Che
che start\
```
When you execute this command, you'll see the URL for the user dashboard.

The Che dashboard will open. It is where you manage your projects and workspaces. If you do not have any projects in Che, you'll be asked to create a new project.  If you already have projects in Che, click on `New Project` button in the menu bar.
# 2. Create Console Java Project  
From the Dashboard page click "Create Workspace".

### Select Source
![ScreenShot2016-09-30at5.56.22PM.png]({{ base }}/assets/imgs/ScreenShot2016-09-30at5.56.22PM.png)
This informs Che where the source code for your project is going to come from. It is possible to start a new blank, template, sample project or import one from another location. Choosing the first option will present you with a set of samples that are preconfigured. If you already have a project at a valid URL, choose the second option. Che gives you choices on how to source the project from Git, GitHub, ZIP, etc..

We will create a project from a provided template.

###Select Stack
![ScreenShot2016-09-30at5.52.21PM.png]({{ base }}/assets/imgs/ScreenShot2016-09-30at5.52.21PM.png)
Your project will be inserted into a workspace, which has a provided Docker runtime. Stacks are the recipes or images used to define the runtime environment for the workspace where your project will be placed. There are three ways to choose a stack:
1. *Ready-To-Go Stacks.* Environments that have a large variety of tools already installed optimized for projects of a particular type. For this example, we will select the Java stack which will create a container with Ubuntu git, java-jdk, maven, tomcat installed.
2. *Stack Library.* Offers finer grained stacks that can be used to create specific technology layers needed for a project. Ubuntu and Debian stacks, for example, are minimal stacks with only an operating system and Che tools installed.
3. *Custom Stack.* You can provide your own custom stack. You'll have the ability to upload a recipe (dockerfile) or directly edit it from there.

Choose the `Ready-To-Go` category and select the `JAVA` stack.

### Configure Workspace
![ScreenShot2016-09-30at5.55.07PM.png]({{ base }}/assets/imgs/ScreenShot2016-09-30at5.55.07PM.png)
Provide a name to your workspace and configure its RAM. RAM will be the memory limit applied to the machines running your workspace environment. For this tutorial, create a new workspace with name `tutorial-java` and set its RAM to 1GB.

###Select Template (Code Sample)
![ScreenShot2016-09-30at5.55.58PM.png]({{ base }}/assets/imgs/ScreenShot2016-09-30at5.55.58PM.png)
A template is a set of code, configuration, and commands that can be imported to operate within Che. There are two types of templates:
1. **Ready-to-run project samples**. These samples have a compilable source tree and embedded commands. The list of templates available are filtered based upon the stack chosen.
2. **Wizard-driven project configuration**. This creates a blank project and then opens the IDE's project configuration wizard to let you scaffold a new project based upon a set of configurable parameters. This option is only available if there an appropriate project type available for the stack chosen.

Choose `Ready-to-run project samples` and select `console-java-simple`, those options should be preselected by default.

### Project Metadata
![ScreenShot2016-09-30at5.56.22PM.png]({{ base }}/assets/imgs/ScreenShot2016-09-30at5.56.22PM.png)
You can set a name and description of your project. The name is what will appear as the root node in the project explorer in the IDE.  Keep the default values.

### Create the Project

Hit the button **Create** at the bottom of the flow. The project construction will start.

The project construction process goes through a number of steps including creating a new workspace, downloading an image to use for the workspace environment, instantiating that environment, installing Che services into the workspace, and then creating your project.
# 3. Develop in the IDE  
The project created is a Maven project. Once you open it in the IDE, you'll see the dependencies updated from the `pom.xml` file.
![ide-update-dep.png]({{ base }}/assets/imgs/ide-update-dep.png)
### Project Explorer
![project-tree.png]({{ base }}/assets/imgs/project-tree.png)
On the left side of the IDE, a panel is displaying the project explorer which allow you to browse the sources of your project. You can use your mouse to expand/collapse the folders and packages, but you are also able to navigate in the project explorer using your keyboard. Use:
- `up arrow` and `down arrow` to navigate in the tree,
- `left arrow` and `right arrow` to expand/collapse folders and packages,
- `enter` to open a file.

### Editor Overview
Open the file `HelloWorld.java` in the package `org.eclipse.che.examples`. The file is displayed with syntax coloration.
![editor-simple.png]({{ base }}/assets/imgs/editor-simple.png)
The editor is structured in a common way:
- at the top: the list of all opened files,
- on the left: line number, breakpoints and error marks,
- on the right: the file's minimap and the cursor position bar to navigate in the file,
- at the bottom: file information (cursor exact position, encoding and file's type).

If you have error in your files, the editor will display error and warning marks:
![editor-errors.png]({{ base }}/assets/imgs/editor-errors.png)
You can use your keyboard to navigate in the file content, but also between files. You can get the complete list of all keyboard shortcut by going into the menu `Assistant` > `Key Bindings` and scrolling to the category `Editor`
![keybindings.png]({{ base }}/assets/imgs/keybindings.png)
### Java Intellisense
There is an Assistant menu that includes language specific capabilities. You can perform auto-complete by hitting `ctrl-space`.

### Jump to Definition
While you are editing your Java code, you may want a documentation lookup for a particular symbol (class, attribute or method). Get quick documentation by `Assistant` > `Quick Documentation` or `^j`.
![quick-documentation.png]({{ base }}/assets/imgs/quick-documentation.png)
If you need more information about the symbol, you can also navigate to its definition with `Assistant` > `Open Declaration` or `F4`.  The `String` class will open in a new editor.
![open-declaration.png]({{ base }}/assets/imgs/open-declaration.png)
### Search
Che editor provides various ways to search your projects and workspace.

#### Search with Editor
Use the editor search to find and replace in a particular file via `CTRL+f` keyboard shortcut. You can also use regular expressions.
![find-editor.png]({{ base }}/assets/imgs/find-editor.png)

#### Find Usages
This will find all references of a particular class, method, field or attribute and search for its usage in your various project's files. Do this with `Assistant` > `Find Usages`. A new panel will open and list all references for `String` into your project. If you select one of the occurrence and double-click on it, the editor will open the file to the position of the found reference.

![find-usages.png]({{ base }}/assets/imgs/find-usages.png)

### Refactoring
Che provides the ability to refactor your source code.

#### Rename
Put cursor on method, variable or field that you want to rename, and hit Shift + F6. If this hotkey is pressed once, the selected keyword will be highlighted which means it's ready for refactoring. You can type a new name and press Enter.

If you press Shift + F6 twice, an advanced Rename mode is called out:
![rename.png]({{ base }}/assets/imgs/rename.png)
Preview button will open a side by side comparison window that will show changes that you are about to apply.

#### Move
Choose any Java class you want to move and hit F6. It will call a Move item menu. Choose destination for your class and click OK.

![move-item.png]({{ base }}/assets/imgs/move-item.png)
It's also possible to preview changes. Choose destination for your class and click Preview. It will show all Java classes and non-Java files (optional), that the replaced class is referenced in.
![preview.png]({{ base }}/assets/imgs/preview.png)
###Manage Maven Modules
Maven Plugin provides the ability to manage Maven modules in multi-module projects entirely through `pom.xml`.

Open any Java multi-module project and create a new folder with a simple Maven project in it. It will be seen as folder in the project tree first. Open your parent project POM and add your newly imported module there:
`<module>new-module</module>`

 As a result, it will be automatically configured as a Maven module in your project tree. Maven plugin watches changes in `pom.xml` and automatically imports changes (dependencies, configuration etc).

###Dependency Management
If you make changes to dependencies in POM, they will be automatically updated. You can also manually reimport the project: right click on your Maven project, choose `Maven > Reimport`.

If you have errors in your POM or add some nonexistent dependency to your POM, the following error will be displayed in the editor:
![nonexistent-dependency.png]({{ base }}/assets/imgs/nonexistent-dependency.png)
###Configure Classpath
It's possible to view project dependencies at `Project > Configure Classpath`. Dependencies in the classpath will be categorized as follows:
- JRE_CONTAINER - Java 1.8 jars;
- MAVEN2_CLASSPATH_CONFIGURATION - project dependencies.
![configure-classpath.png]({{ base }}/assets/imgs/configure-classpath.png)
###Generate Effective POM
There is also a possibility to display POM that results from the application of interpolation, inheritance and active profiles. Just open your Maven project and go to `Assistant > Generate Effective Pom`.
