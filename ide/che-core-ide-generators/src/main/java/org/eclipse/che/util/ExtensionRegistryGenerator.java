/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.extension.ExtensionRegistry;
import org.eclipse.che.ide.api.extension.SDK;

/**
 * This class generates implementation for {@link
 * org.eclipse.che.ide.api.extension.ExtensionRegistry} that contains descriptive information about
 * extensions present in IDE Bundle. Generator processes all Types found by TypeOracle, looking for
 * {@link Extension} annotation. Each class annotated with Extension is processed to retrieve
 * dependency information. Dependencies are collected by reading constructor annotated with {@link
 * Inject}. All arguments retrieved and asked if annotated with {@link Extension} of {@link SDK}.
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
public class ExtensionRegistryGenerator extends Generator {

  /** {@inheritDoc} */
  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName)
      throws UnableToCompleteException {
    TypeOracle typeOracle = context.getTypeOracle();
    JClassType extensionManager = typeOracle.findType(typeName);
    if (extensionManager == null) {
      logger.log(TreeLogger.ERROR, "Can't find interface type '" + typeName + "'", null);
      throw new UnableToCompleteException();
    }
    if (extensionManager.isInterface() == null) {
      logger.log(
          TreeLogger.ERROR,
          extensionManager.getQualifiedSourceName() + " is not an interface",
          null);
      throw new UnableToCompleteException();
    }

    List<JClassType> extensions = new ArrayList<>();
    for (JClassType type : typeOracle.getTypes()) {
      if (type.isAnnotationPresent(Extension.class)) {
        extensions.add(type);
      }
    }

    String packageName = extensionManager.getPackage().getName();
    String className = extensionManager.getSimpleSourceName() + "Impl";

    generateClass(logger, context, packageName, className, extensions);

    return packageName + "." + className;
  }

  /**
   * Generate the content of the class
   *
   * @param logger
   * @param context
   * @param packageName
   * @param className
   * @param extensions
   * @throws UnableToCompleteException
   */
  private void generateClass(
      TreeLogger logger,
      GeneratorContext context,
      String packageName,
      String className,
      List<JClassType> extensions)
      throws UnableToCompleteException {
    PrintWriter pw = context.tryCreate(logger, packageName, className);
    if (pw == null) {
      return;
    }

    ClassSourceFileComposerFactory composerFactory =
        new ClassSourceFileComposerFactory(packageName, className);
    // generate imports
    generateImports(extensions, composerFactory);

    // interface
    composerFactory.addImplementedInterface(ExtensionRegistry.class.getCanonicalName());

    // get source writer
    SourceWriter sw = composerFactory.createSourceWriter(context, pw);
    // begin class definition
    // fields
    sw.println("private final Map<String, ExtensionDescription> extensions = new HashMap<>();");

    generateConstructor(className, extensions, sw);

    // methods
    generateGetExtensionsMethod(sw);
    // close it out
    sw.outdent();
    sw.println("}");

    context.commit(logger, pw);
  }

  /**
   * Inject imports
   *
   * @param extensions
   * @param composerFactory
   */
  private void generateImports(
      List<JClassType> extensions, ClassSourceFileComposerFactory composerFactory) {
    // imports
    composerFactory.addImport(GWT.class.getCanonicalName());
    composerFactory.addImport(Extension.class.getCanonicalName());
    composerFactory.addImport(ExtensionRegistry.class.getCanonicalName());
    composerFactory.addImport(Inject.class.getCanonicalName());
    composerFactory.addImport(Provider.class.getCanonicalName());
    composerFactory.addImport(List.class.getCanonicalName());
    composerFactory.addImport(ArrayList.class.getCanonicalName());
    composerFactory.addImport(Map.class.getCanonicalName());
    composerFactory.addImport(HashMap.class.getCanonicalName());
    // import for extensions
    for (JClassType jClassType : extensions) {
      composerFactory.addImport(jClassType.getQualifiedSourceName());
    }
  }

  /**
   * Generate constructor with dependencies added into field
   *
   * @param className
   * @param extensions
   * @param sw
   * @throws UnableToCompleteException
   */
  private void generateConstructor(String className, List<JClassType> extensions, SourceWriter sw)
      throws UnableToCompleteException {
    // constructor
    sw.indent();
    sw.print("public %s()", className);
    sw.println("{");
    sw.indent();
    {
      for (JClassType extension : extensions) {
        sw.println("{");
        sw.indent();
        /*
           Array<DependencyDescription> deps = Collections.<DependencyDescription> createArray();
        */
        generateDependenciesForExtension(sw, extension);

        Extension annotation = extension.getAnnotation(Extension.class);

        /*
           extensions.put("ide.ext.demo", new ExtensionDescription("ide.ext.demo", "1.0.0", "Demo extension", deps,
           demoExtProvider));
        */

        // the class's fqn
        String extensionId = extension.getQualifiedSourceName();

        sw.println(
            "extensions.put(\"%s\", new ExtensionDescription(\"%s\",\"%s\",\"%s\",\"%s\",deps));",
            escape(extensionId),
            escape(extensionId),
            escape(annotation.version()),
            escape(annotation.title()),
            escape(annotation.description()));
        sw.outdent();
        sw.println("}");
      }
    }
    sw.outdent();
    sw.println("}");
  }

  /**
   * Writes dependency gathering code, like:
   *
   * <p>Array<DependencyDescription> deps = Collections.<DependencyDescription> createArray();
   * deps.add(new DependencyDescription("ide.api.ui.menu", "")); deps.add(new
   * DependencyDescription("extension.demo", "1.0.0-alpha"));
   *
   * @param sw
   * @param extension
   * @throws UnableToCompleteException
   */
  private void generateDependenciesForExtension(SourceWriter sw, JClassType extension)
      throws UnableToCompleteException {
    // expected code
    /*
          Array<DependencyDescription> deps = Collections.<DependencyDescription> createArray();
          deps.add(new DependencyDescription("ide.api.ui.menu", ""));
    */
    if (extension.getConstructors().length == 0) {
      throw new UnableToCompleteException();
    }
    sw.println("List<DependencyDescription> deps = new ArrayList<>();");

    JConstructor jConstructor = extension.getConstructors()[0];
    JType[] parameterTypes = jConstructor.getParameterTypes();

    for (JType jType : parameterTypes) {
      JClassType argType = jType.isClassOrInterface();
      if (argType != null
          && (argType.isAnnotationPresent(SDK.class)
              || argType.isAnnotationPresent(Extension.class))) {
        String id = "";
        String version = "";
        if (argType.isAnnotationPresent(SDK.class)) {
          id = argType.getAnnotation(SDK.class).title();
        } else if (argType.isAnnotationPresent(Extension.class)) {
          id = argType.getQualifiedSourceName();
          version = argType.getAnnotation(Extension.class).version();
        }
        sw.println(
            "deps.add(new DependencyDescription(\"%s\", \"%s\"));", escape(id), escape(version));
      }
    }
  }

  /**
   * Generate the code for {@link ExtensionRegistry#getExtensionDescriptions()}
   *
   * @param sw
   */
  private void generateGetExtensionsMethod(SourceWriter sw) {
    /*
         @Override
         public StringMap<ExtensionDescription> getExtensionDescriptions()
         {
            return extensions;
         }
    */

    sw.println("@Override");
    sw.println("public Map<String, ExtensionDescription> getExtensionDescriptions()");

    sw.println("{");
    sw.indent();

    sw.println("return extensions;");

    sw.outdent();
    sw.println("}");
  }
}
