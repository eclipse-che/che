/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.util;

import static org.eclipse.che.util.IgnoreUnExistedResourcesReflectionConfigurationBuilder.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.eclipse.che.ide.dto.ClientDtoFactoryVisitor;
import org.reflections.Reflections;

/**
 * Generates {DtoFactoryVisitorRegistry} class source.
 *
 * @author Artem Zatsarynnyi
 */
public class DtoFactoryVisitorRegistryGenerator {

  /**
   * Path of the output class, it definitely should already exits. To ensure proper config. File
   * content will be overridden.
   */
  protected static final String REGISTRY_PATH =
      "org/eclipse/che/ide/client/DtoFactoryVisitorRegistry.java";
  /** Map containing <FullFQN, ClassName> */
  protected static final Map<String, String> dtoFactoryVisitors = new HashMap<>();

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
    System.out.println("Searching for DTO");
    System.out.println(
        " ------------------------------------------------------------------------ ");

    // find all DtoFactoryVisitors
    findDtoFactoryVisitors();
    generateExtensionManager(rootFolder);
  }

  /**
   * Find all the Java classes that have proper @ClientDtoFactoryVisitor annotation.
   *
   * @throws java.io.IOException
   */
  @SuppressWarnings("unchecked")
  private static void findDtoFactoryVisitors() throws IOException {
    Reflections reflection = new Reflections(getConfigurationBuilder());
    Set<Class<?>> classes = reflection.getTypesAnnotatedWith(ClientDtoFactoryVisitor.class);
    int i = 0;
    for (Class clazz : classes) {
      dtoFactoryVisitors.put(clazz.getCanonicalName(), "provider_" + i++);
      System.out.println(
          String.format("New DtoFactoryVisitor found: %s", clazz.getCanonicalName()));
    }
    System.out.println(String.format("Found: %d DtoFactoryVisitor(s)", dtoFactoryVisitors.size()));
  }

  /**
   * Generate to source of the class.
   *
   * @param rootFolder
   */
  public static void generateExtensionManager(File rootFolder) throws IOException {
    File outFile = new File(rootFolder, REGISTRY_PATH);

    StringBuilder builder = new StringBuilder();
    builder.append("package org.eclipse.che.ide.client;\n\n");
    generateImports(builder);
    generateClass(builder);

    // flush content
    FileUtils.writeStringToFile(outFile, builder.toString());
  }

  /**
   * Generate imports.
   *
   * @param builder
   */
  public static void generateImports(StringBuilder builder) {
    builder.append("import com.google.inject.Inject;\n");
    builder.append("import com.google.inject.Provider;\n");
    builder.append("import com.google.inject.Singleton;\n");

    builder.append("import java.util.HashMap;\n");
    builder.append("import java.util.Map;\n");
  }

  /**
   * Generate class declarations.
   *
   * @param builder
   */
  public static void generateClass(StringBuilder builder) {
    // generate class header
    builder.append("/**\n");
    builder.append(
        " * THIS CLASS WILL BE OVERRIDDEN BY MAVEN BUILD. DON'T EDIT CLASS, IT WILL HAVE NO EFFECT.\n");
    builder.append(" */\n");
    builder.append("@Singleton\n");
    builder.append("@SuppressWarnings(\"rawtypes\")\n");
    builder.append("public class DtoFactoryVisitorRegistry\n");
    builder.append("{\n");
    builder.append("\n");

    // field
    builder
        .append(GeneratorUtils.TAB)
        .append(
            "/** Contains the map with all the DtoFactoryVisitor Providers <FullClassFQN, Provider>. */\n");
    builder
        .append(GeneratorUtils.TAB)
        .append("protected final Map<String, Provider> providers = new HashMap<>();\n\n");

    // generate constructor
    builder
        .append(GeneratorUtils.TAB)
        .append("/** Constructor that accepts all found DtoFactoryVisitor Providers. */\n");
    builder.append(GeneratorUtils.TAB).append("@Inject\n");
    builder.append(GeneratorUtils.TAB).append("public DtoFactoryVisitorRegistry(\n");

    // paste args here
    Iterator<Entry<String, String>> entryIterator = dtoFactoryVisitors.entrySet().iterator();
    while (entryIterator.hasNext()) {
      // <FullFQN, ClassName>
      Entry<String, String> entry = entryIterator.next();
      String hasComma = entryIterator.hasNext() ? "," : "";
      // add constructor argument like:
      // fullFQN classNameToLowerCase,
      String classFQN = String.format("Provider<%s>", entry.getKey());
      String variableName = entry.getValue().toLowerCase();
      builder
          .append(GeneratorUtils.TAB2)
          .append(classFQN)
          .append(" ")
          .append(variableName)
          .append(hasComma)
          .append("\n");
    }

    builder.append(GeneratorUtils.TAB).append(")\n");
    builder.append(GeneratorUtils.TAB).append("{\n");

    // paste add here
    for (Entry<String, String> entries : dtoFactoryVisitors.entrySet()) {
      String fullFqn = entries.getKey();
      String variableName = entries.getValue().toLowerCase();

      String putStatement =
          String.format("this.providers.put(\"%s\", %s);%n", fullFqn, variableName);
      builder.append(GeneratorUtils.TAB2).append(putStatement);
    }

    // close constructor
    builder.append(GeneratorUtils.TAB).append("}\n\n");

    // generate getter
    builder
        .append(GeneratorUtils.TAB)
        .append(
            "/** Returns the map with all the DtoFactoryVisitor Providers <FullClassFQN, Provider>. */\n");
    builder
        .append(GeneratorUtils.TAB)
        .append("public Map<String, Provider> getDtoFactoryVisitors()\n");
    builder.append(GeneratorUtils.TAB).append("{\n");
    builder.append(GeneratorUtils.TAB2).append("return providers;\n");
    builder.append(GeneratorUtils.TAB).append("}\n");

    // close class
    builder.append("}\n");
  }
}
