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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

/**
 * Generates server side dto's from lsp4j message classes and che extensions thereof. The classes in
 * question must have a zero-arg constructor and standard getters/setters for all fields.
 *
 * @author Thomas Mäder
 */
public class ServerDtoGenerator extends DtoGenerator {
  public ServerDtoGenerator() {
    super(new ServerJsonImpl());
  }

  public static void main(String[] args) throws IOException {
    new ServerDtoGenerator()
        .generate(new File(args[0]), "Dtos", args[1], args[2].split(","), new String[] {});
  }

  @Override
  protected void writeEnvClassAnnotations(PrintWriter out) {
    out.println("@SuppressWarnings(value= {\"serial\", \"deprecation\"})");
  }

  @Override
  protected void writeEnvImports(PrintWriter out) {
    out.println("import org.eclipse.che.dto.server.DtoFactoryVisitor;");
    out.println("import org.eclipse.che.dto.server.DtoFactory;");
    out.println("import org.eclipse.che.dto.server.DtoProvider;");
    out.println("import org.eclipse.che.dto.server.JsonSerializable;");
    out.println("import java.io.Writer;");
    out.println("import java.io.IOException;");
  }

  protected void writeEnvSpecificToJson(String indent, PrintWriter out) {
    out.println(indent + INDENT + "public void toJson(Writer w) {");
    out.println(indent + INDENT + INDENT + "try {");

    out.println(indent + INDENT + INDENT + INDENT + "w.write(toJson());");
    out.println(indent + INDENT + INDENT + "} catch (IOException e) {");
    out.println(indent + INDENT + INDENT + INDENT + "throw new RuntimeException(e);");
    out.println(indent + INDENT + INDENT + "}");
    out.println(indent + INDENT + "}");
    out.println();
  }

  @Override
  public void generate(
      File targetFolder,
      String targetName,
      String targetPackage,
      String[] sourcePackages,
      String[] classes)
      throws IOException {
    super.generate(targetFolder, targetName, targetPackage, sourcePackages, classes);
    // Create file in META-INF/services/
    File outServiceFile =
        new File(targetFolder, "META-INF/services/org.eclipse.che.dto.server.DtoFactoryVisitor");
    Files.createDirectories(outServiceFile.toPath().getParent());
    try (BufferedWriter serviceFileWriter = new BufferedWriter(new FileWriter(outServiceFile))) {
      serviceFileWriter.write(targetPackage + "." + targetName);
    }
  }
}
