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

import static org.eclipse.che.util.IgnoreUnExistedResourcesReflectionConfigurationBuilder.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.reflections.Reflections;

/**
 * This class looks for all the Gin Modules annotated with ExtensionGinModule annotation and adds
 * all them to IDEInjector class.
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
public class IDEInjectorGenerator {

  /** Set containing all the FQNs of GinModules */
  public static final Set<String> EXTENSIONS_FQN = new HashSet<>();
  /** Annotation to look for */
  protected static final String GIN_MODULE_ANNOTATION = "@ExtensionGinModule";

  /**
   * Path of the output class, it definitely should already exits. To ensure proper config. File
   * content will be overridden.
   */
  protected static final String IDE_INJECTOR_PATH =
      "org/eclipse/che/ide/client/inject/IDEInjector.java";

  /**
   * Entry point. --rootDir is the optional parameter.
   *
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    File rootFolder = GeneratorUtils.getRootFolder(args);
    System.out.println(
        " ------------------------------------------------------------------------ ");
    System.out.println(
        String.format("Searching for GinModules in %s", rootFolder.getAbsolutePath()));
    System.out.println(
        " ------------------------------------------------------------------------ ");
    // find all Extension FQNs
    findGinModules(rootFolder);
    generateExtensionManager(rootFolder);
  }

  /**
   * Generate to source of the Class
   *
   * @param rootFolder
   */
  public static void generateExtensionManager(File rootFolder) throws IOException {
    File extManager = new File(rootFolder, IDE_INJECTOR_PATH);
    StringBuilder builder = new StringBuilder();
    // declare package name
    builder.append("package org.eclipse.che.ide.client.inject;\n\n");

    // declare imports
    builder.append("import org.eclipse.che.ide.bootstrap.IdeBootstrap;\n");
    builder.append("import com.google.gwt.inject.client.GinModules;\n");
    builder.append("import com.google.gwt.inject.client.Ginjector;\n");
    builder.append("\n");

    // declare class Javadoc
    builder.append("/**\n");
    builder.append(
        " * THIS CLASS WILL BE OVERRIDDEN BY MAVEN BUILD. DON'T EDIT CLASS, IT WILL HAVE NO EFFECT.\n");
    builder.append(" * \n");
    builder.append(" * Interface for GIN Injector, that provides access to the top level\n");
    builder.append(" * application components. Implementation of Injector is generated\n");
    builder.append(" * on compile time.\n");
    builder.append(" */\n");
    builder.append("@GinModules({\n");
    // generate the list of modules includes
    generateListOfModules(builder);
    // close GinModules declaration
    builder.append("})\n");

    // declare class definition
    builder.append("public interface IDEInjector extends Ginjector\n");
    builder.append("{\n");
    builder.append("\n");
    // define method
    builder.append(GeneratorUtils.TAB).append("/**\n");
    builder.append(GeneratorUtils.TAB).append(" * @return the instance of IdeBootstrap\n");
    builder.append(GeneratorUtils.TAB).append(" */\n");
    builder.append(GeneratorUtils.TAB).append("IdeBootstrap getIdeBootstrap();\n");
    // close class definition
    builder.append("\n");
    builder.append("}\n");

    // flush content
    FileUtils.writeStringToFile(extManager, builder.toString());
  }

  /**
   * Generate codeblock with all the GinModules
   *
   * @param builder
   */
  public static void generateListOfModules(StringBuilder builder) {
    // Generate the list of GinModules declarations
    Iterator<String> entryIterator = EXTENSIONS_FQN.iterator();
    while (entryIterator.hasNext()) {
      // <FullFQN, ClassName>
      String ginModuleFQN = entryIterator.next();
      String hasComma = entryIterator.hasNext() ? "," : "";
      // add ModuleDeclaration
      builder
          .append(GeneratorUtils.TAB)
          .append(ginModuleFQN)
          .append(".class ")
          .append(hasComma)
          .append("\n");
    }
  }

  /**
   * Find all the Java files that have ExtensionGinModule annotation
   *
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public static void findGinModules(File rootFolder) throws IOException {
    Reflections reflection = new Reflections(getConfigurationBuilder());

    Set<Class<?>> classes = reflection.getTypesAnnotatedWith(ExtensionGinModule.class);
    for (Class clazz : classes) {
      EXTENSIONS_FQN.add(clazz.getCanonicalName());
      System.out.println(String.format("New Gin Module Found: %s", clazz.getCanonicalName()));
    }
    System.out.println(String.format("Found: %d Gin Modules", EXTENSIONS_FQN.size()));
  }
}
