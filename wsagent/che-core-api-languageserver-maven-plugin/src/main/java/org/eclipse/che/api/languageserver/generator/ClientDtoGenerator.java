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
package org.eclipse.che.api.languageserver.generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Generates client side dto's from lsp4j message classes and che extensions thereof. The classes in
 * question must have a zero-arg constructor and standard getters/setters for all fields.
 *
 * @author Thomas Mäder
 */
public class ClientDtoGenerator extends DtoGenerator {
  public ClientDtoGenerator() {
    super(new ClientJsonImpl());
  }

  public static void main(String[] args) throws IOException {
    new ClientDtoGenerator()
        .generate(new File(args[0]), "Dtos", args[1], args[2].split(","), new String[] {});
  }

  @Override
  protected void writeEnvClassAnnotations(PrintWriter out) {
    out.println("@Singleton");
    out.println("@ClientDtoFactoryVisitor");
    out.println("@SuppressWarnings(\"deprecation\")");
  }

  @Override
  protected void writeEnvImports(PrintWriter out) {
    out.println("import org.eclipse.che.ide.dto.ClientDtoFactoryVisitor;");
    out.println("import org.eclipse.che.ide.dto.DtoFactoryVisitor;");
    out.println("import org.eclipse.che.ide.dto.DtoFactory;");
    out.println("import org.eclipse.che.ide.dto.DtoProvider;");
    out.println("import org.eclipse.che.api.languageserver.util.JsonSerializable;");

    out.println("import com.google.inject.Singleton;");
  }
}
