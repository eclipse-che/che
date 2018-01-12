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
package org.eclipse.che.api.languageserver.generator;

import com.google.common.base.Predicate;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * DtoGenerator generates che-style DTO's for lsp4j protocol classes. Subclasses specialize this
 * class to generate client-side and server-side implementations.
 *
 * @author Thomas Mäder
 */
public abstract class DtoGenerator {

  /** Standard indent size to use. */
  public static final String INDENT = "    ";

  protected final JsonImpl json;

  public DtoGenerator(JsonImpl json) {
    this.json = json;
  }

  /**
   * Return the simple class name to use for the dto class for the given class.
   *
   * @param clazz
   * @return
   */
  public static String dtoName(Class<? extends Object> clazz) {
    return clazz.getSimpleName() + "Dto";
  }

  static boolean isSetter(Class<?> receiverClass, Method m) {
    Method getter = getterFor(receiverClass, m);
    return m.getName().startsWith("set")
        && m.getName().length() > 3
        && Character.isUpperCase(m.getName().charAt(3))
        && getter != null
        && getter.getReturnType() == m.getParameterTypes()[0];
  }

  private static Method getterFor(Class<?> receiverClass, Method m) {
    if (m.getParameterTypes().length != 1) {
      return null;
    }
    if (boolean.class == m.getParameterTypes()[0] || Boolean.class == m.getParameterTypes()[0]) {
      String root = m.getName().substring(3);
      try {
        return receiverClass.getMethod("is" + root, new Class<?>[] {});
      } catch (NoSuchMethodException e) {
        try {
          return receiverClass.getMethod("get" + root, new Class<?>[] {});
        } catch (NoSuchMethodException e1) {
          StringBuilder b = new StringBuilder(root);
          b.setCharAt(0, Character.toLowerCase(root.charAt(0)));
          try {
            return receiverClass.getMethod(b.toString(), new Class<?>[] {});
          } catch (NoSuchMethodException e3) {
            return null;
          }
        }
      }
    }
    try {
      return receiverClass.getMethod(m.getName().replaceFirst("set", "get"), new Class<?>[] {});
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  /**
   * Generate dto classes.
   *
   * @param targetFolder the base folder to generate code to. This is the package folder root.
   * @param targetName the simple class name to use for the generated class
   * @param targetPackage the package name to use for the generated class
   * @param sourcePackages the source packages to use. THe packages must be on the class path at
   *     execution time.
   * @throws IOException
   */
  public void generate(
      File targetFolder,
      String targetName,
      String targetPackage,
      String[] sourcePackages,
      String[] classNames)
      throws IOException {
    File targetFile =
        new File(
            targetFolder,
            targetPackage.replace('.', File.separatorChar)
                + File.separatorChar
                + targetName
                + ".java");
    targetFile.getParentFile().mkdirs();
    Set<URL> urls = new HashSet<>();
    for (String pkg : sourcePackages) {
      urls.addAll(ClasspathHelper.forPackage(pkg));
    }
    Reflections reflection =
        new Reflections(
            new ConfigurationBuilder()
                .setUrls(urls)
                .setScanners(new SubTypesScanner(false))
                .filterInputsBy(
                    new Predicate<String>() {

                      @Override
                      public boolean apply(String input) {
                        for (String pkg : sourcePackages) {
                          if (input.startsWith(pkg)) {
                            return input.indexOf('.', pkg.length() + 1) == input.lastIndexOf('.');
                          }
                        }
                        return false;
                      }
                    }));
    Set<Class<?>> allTypes = reflection.getSubTypesOf(Object.class);
    for (String className : classNames) {
      try {
        allTypes.add(Class.forName(className));
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }

    allTypes = allTypes.stream().filter((cls) -> !cls.isInterface()).collect(Collectors.toSet());

    try (PrintWriter out = new PrintWriter(targetFile, "utf-8")) {

      out.print("package ");
      out.print(targetPackage);
      out.println(";");

      writeEnvImports(out);

      json.writeImports(out);

      out.println("import java.util.Map.Entry;");
      out.println("import java.util.ArrayList;");
      out.println("import java.util.HashMap;");
      out.println("import org.eclipse.lsp4j.jsonrpc.messages.Either;");
      out.println("import org.eclipse.che.api.languageserver.util.EitherUtil;");
      out.println("import org.eclipse.che.api.languageserver.util.JsonUtil;");
      out.println("import org.eclipse.che.api.languageserver.shared.util.JsonDecision;");

      for (Class<? extends Object> clazz : allTypes) {
        out.print("import ");
        out.print(clazz.getCanonicalName());
        out.println(";");
      }

      out.println();
      writeEnvClassAnnotations(out);

      out.println(String.format("public class %1$s implements DtoFactoryVisitor {", targetName));

      for (Class<? extends Object> clazz : allTypes) {
        writeDTOClass(INDENT, out, clazz, allTypes);
        out.println();
        writeDTOProvider(INDENT, out, clazz);
        out.println();
      }

      out.println(INDENT + "public void accept(DtoFactory dtoFactory) {");
      for (Class<? extends Object> clazz : allTypes) {
        out.println(
            INDENT
                + INDENT
                + String.format(
                    "dtoFactory.registerProvider(%1$s.class, new %2$s());",
                    clazz.getSimpleName(), dtoProviderName(clazz)));
      }
      out.println(INDENT + "}");

      ToDtoGenerator.generateMakeDto(INDENT, out, allTypes);

      out.println("}");
      out.flush();
    }
  }

  /**
   * Write environment-specific target class annotations.
   *
   * @param out
   */
  protected abstract void writeEnvClassAnnotations(PrintWriter out);

  /**
   * Write environment-specific imports.
   *
   * @param out
   */
  protected abstract void writeEnvImports(PrintWriter out);

  private void writeDTOClass(
      String indent,
      PrintWriter out,
      Class<? extends Object> clazz,
      Set<Class<? extends Object>> classes) {

    out.println(
        indent
            + String.format(
                "public static class %1$s extends %2$s implements JsonSerializable {",
                dtoName(clazz), clazz.getSimpleName()));

    out.println();

    out.println(indent + INDENT + String.format("public %1$s() {", dtoName(clazz)));
    out.println(indent + INDENT + "}");
    out.println();

    writeCopyConstructor(indent + INDENT, out, clazz, classes);
    out.println();

    writeToJson(indent + INDENT, out, clazz);
    out.println();

    writeEnvSpecificToJson(indent, out);

    out.println(indent + INDENT + "public String toJson() {");
    out.println(indent + INDENT + INDENT + "return toJsonElement().toString();");
    out.println(indent + INDENT + "}");
    out.println();
    out.println(
        String.format(
            indent + INDENT + "public static %1$s fromJson(String json) {", dtoName(clazz)));
    out.println(indent + INDENT + INDENT + "if (json == null) {");
    out.println(indent + INDENT + INDENT + INDENT + "return null;");
    out.println(indent + INDENT + INDENT + "}");

    out.println(
        indent + INDENT + INDENT + String.format("return fromJson(%1$s);", json.parse("json")));
    out.println(indent + INDENT + "}");

    out.println(
        String.format(
            indent + INDENT + "public static %1$s fromJson(%2$s jsonVal) {",
            dtoName(clazz),
            json.element()));

    out.println(indent + INDENT + INDENT + "if (jsonVal == null) {");
    out.println(indent + INDENT + INDENT + INDENT + "return null;");
    out.println(indent + INDENT + INDENT + "}");
    out.println(
        indent
            + INDENT
            + INDENT
            + String.format("%1$s json= %2$s;", json.object(), json.objectValue("jsonVal")));

    out.println(
        indent + INDENT + INDENT + String.format("%1$s result= new %1$s();", dtoName(clazz)));
    FromJsonGenerator fromJsonGenerator = new FromJsonGenerator(json);
    for (Method m : clazz.getMethods()) {
      if (isSetter(clazz, m)) {
        fromJsonGenerator.generateFromJson(indent + INDENT + INDENT, out, m, "result", "json");
      }
    }
    out.println(indent + INDENT + INDENT + "return result;");

    out.println(indent + INDENT + "}");

    out.println(indent + "}");
  }

  protected void writeEnvSpecificToJson(String indent, PrintWriter out) {}

  private void writeCopyConstructor(
      String indent,
      PrintWriter out,
      Class<? extends Object> clazz,
      Set<Class<? extends Object>> classes) {
    out.println(indent + String.format("public %1$s(%2$s o) {", dtoName(clazz), clazz.getName()));
    ToDtoGenerator toJsonGenerator = new ToDtoGenerator(classes);

    for (Method m : clazz.getMethods()) {
      if (isSetter(clazz, m)) {
        toJsonGenerator.generateToDto(indent + INDENT, out, clazz, m, "o");
      }
    }

    out.println(indent + "}");
  }

  private void writeToJson(String indent, PrintWriter out, Class<? extends Object> clazz) {
    out.println(indent + String.format("public %1$s toJsonElement() {", json.element()));
    out.println(indent + INDENT + String.format("%1$s result = new %1$s();", json.object()));

    ToJsonGenerator toJsonGenerator = new ToJsonGenerator(json);

    for (Method m : clazz.getMethods()) {
      if (isSetter(clazz, m)) {
        toJsonGenerator.generateToJson(indent + INDENT, out, clazz, m);
      }
    }

    out.println(indent + INDENT + "return result;\n");
    out.println(indent + "}");
  }

  private void writeDTOProvider(String indent, PrintWriter out, Class<? extends Object> clazz) {
    String source =
        indent
            + "public static class %1$s implements DtoProvider<%2$s> {\n"
            + indent
            + "	public Class<? extends %2$s> getImplClass() {\n"
            + indent
            + "	  	return %2$s.class;\n"
            + indent
            + "	}\n"
            + indent
            + "\n"
            + indent
            + "	public %2$s newInstance() {\n"
            + indent
            + "    	return new %2$s();\n"
            + indent
            + "	}\n"
            + indent
            + "\n"
            + indent
            + "	public %2$s fromJson(String json) {\n"
            + indent
            + "		return %2$s.fromJson(json);\n"
            + indent
            + "	}\n"
            + indent
            + INDENT
            + "public %2$s fromJson(%3$s json) {\n"
            + indent
            + "		return %2$s.fromJson(json);\n"
            + indent
            + "	}\n"
            + indent
            + INDENT
            + "public %2$s clone(%2$s dto) {\n"
            + indent
            + INDENT
            + INDENT
            + "return %2$s.fromJson(dto.toJson());\n"
            + indent
            + "	}\n"
            + indent
            + "}";

    out.println(String.format(source, dtoProviderName(clazz), dtoName(clazz), json.element()));
  }

  private String dtoProviderName(Class<? extends Object> clazz) {
    return clazz.getSimpleName() + "DtoProvider";
  }
}
