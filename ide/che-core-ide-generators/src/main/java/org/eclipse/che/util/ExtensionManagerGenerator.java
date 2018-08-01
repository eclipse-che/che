/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.eclipse.che.ide.api.extension.Extension;
import org.reflections.Reflections;

/**
 * Generates {ExtensionManager} class source
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
public class ExtensionManagerGenerator {

  /** Annotation to look for. */
  protected static final String EXT_ANNOTATION = "@Extension";

  /** Reg Exp that matches the "@Extension ( ... )" */
  protected static final Pattern EXT_PATTERN =
      Pattern.compile(".*@Extension\\s*\\(.*\\).*", Pattern.DOTALL);

  /**
   * Path of the output class, it definitely should already exits. To ensure proper config. File
   * content will be overridden.
   */
  protected static final String EXT_MANAGER_PATH =
      "/org/eclipse/che/ide/client/ExtensionManager.java";

  /** Map containing <FullFQN, ClassName> */
  protected static final Map<String, String> EXTENSIONS_FQN = new HashMap<String, String>();

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
        String.format("Searching for Extensions in %s", rootFolder.getAbsolutePath()));
    System.out.println(
        " ------------------------------------------------------------------------ ");
    // find all Extension FQNs
    findExtensions();
    generateExtensionManager(rootFolder);
  }

  /**
   * Generate to source of the Class
   *
   * @param rootFolder
   */
  public static void generateExtensionManager(File rootFolder) throws IOException {
    File extManager = new File(rootFolder, EXT_MANAGER_PATH);
    StringBuilder builder = new StringBuilder();
    builder.append("package org.eclipse.che.ide.client;\n\n");
    generateImports(builder);
    generateClass(builder);
    // flush content
    FileUtils.writeStringToFile(extManager, builder.toString());
  }

  /**
   * Generate Class declarations
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
    builder.append("public class ExtensionManager\n");
    builder.append("{\n");
    builder.append("\n");

    // field
    builder
        .append(GeneratorUtils.TAB)
        .append(
            "/** Contains the map will all the Extension Providers <FullClassFQN, Provider>. */\n");
    builder
        .append(GeneratorUtils.TAB)
        .append("protected final Map<String, Provider> extensions = new HashMap<>();\n\n");

    // generate constructor
    builder
        .append(GeneratorUtils.TAB)
        .append("/** Constructor that accepts all the Extension found in IDE package */\n");
    builder.append(GeneratorUtils.TAB).append("@Inject\n");
    builder.append(GeneratorUtils.TAB).append("public ExtensionManager(\n");

    // paste args here
    Iterator<Entry<String, String>> entryIterator = EXTENSIONS_FQN.entrySet().iterator();
    while (entryIterator.hasNext()) {
      // <FullFQN, ClassName>
      Entry<String, String> extensionEntry = entryIterator.next();
      String hasComma = entryIterator.hasNext() ? "," : "";
      // add constructor argument like:
      // fullFQN classNameToLowerCase,
      String classFQN = String.format("Provider<%s>", extensionEntry.getKey());
      String variableName = extensionEntry.getValue().toLowerCase();
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
    for (Entry<String, String> extension : EXTENSIONS_FQN.entrySet()) {
      String fullFqn = extension.getKey();
      String variableName = extension.getValue().toLowerCase();

      String putStatement =
          String.format("this.extensions.put(\"%s\",%s);%n", fullFqn, variableName);
      builder.append(GeneratorUtils.TAB2).append(putStatement);
    }

    // close constructor
    builder.append(GeneratorUtils.TAB).append("}\n\n");

    // generate getter
    builder
        .append(GeneratorUtils.TAB)
        .append(
            "/** Returns  the map will all the Extension Providers <FullClassFQN, Provider>. */\n");
    builder.append(GeneratorUtils.TAB).append("public Map<String, Provider> getExtensions()\n");
    builder.append(GeneratorUtils.TAB).append("{\n");
    builder.append(GeneratorUtils.TAB2).append("return extensions;\n");
    builder.append(GeneratorUtils.TAB).append("}\n");

    // close class
    builder.append("}\n");
  }

  /**
   * Generate imports
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
   * Find all the Java Classes that have proper @Extension declaration
   *
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public static void findExtensions() throws IOException {
    Reflections reflection = new Reflections(getConfigurationBuilder());
    Set<Class<?>> classes = reflection.getTypesAnnotatedWith(Extension.class);
    for (Class clazz : classes) {
      EXTENSIONS_FQN.put(clazz.getCanonicalName(), clazz.getSimpleName());
      System.out.println(String.format("New Extension Found: %s", clazz.getCanonicalName()));
    }
    System.out.println(String.format("Found: %d extensions", EXTENSIONS_FQN.size()));
  }
}
