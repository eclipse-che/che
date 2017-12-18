// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.dto.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.dto.server.DtoFactoryVisitor;

/**
 * Base template for the generated output file that contains all the DTOs.
 *
 * <p>Note that we generate client and server DTOs in separate runs of the generator.
 *
 * <p>The directionality of the DTOs only affects whether or not we expose methods to construct an
 * instance of the DTO. We need both client and server versions of all DTOs (irrespective of
 * direction).
 */
public class DtoTemplate {
  public static class MalformedDtoInterfaceException extends RuntimeException {
    public MalformedDtoInterfaceException(String msg) {
      super(msg);
    }
  }

  // We keep a whitelist of allowed non-DTO generic types.
  static final Set<Class<?>> jreWhitelist =
      new HashSet<>(
          Arrays.asList(
              new Class<?>[] {
                String.class, Integer.class, Double.class, Float.class, Boolean.class
              }));

  private final List<DtoImpl> dtoInterfaces = new ArrayList<>();

  // contains mapping for already implemented DTO interfaces
  private final Map<Class<?>, Set<Class<?>>> implementedDtoInterfaces = new HashMap<>();

  private final String packageName;

  private final String className;

  private final String implType;

  /**
   * Walks the super interface hierarchy to determine if a Class implements some target interface
   * transitively.
   */
  static boolean implementsInterface(Class<?> i, Class<?> target) {
    if (i.equals(target)) {
      return true;
    }

    boolean rtn = false;
    Class<?>[] superInterfaces = i.getInterfaces();
    for (Class<?> superInterface : superInterfaces) {
      rtn = rtn || implementsInterface(superInterface, target);
    }
    return rtn;
  }

  /**
   * Constructor.
   *
   * @param packageName The name of the package for the outer DTO class.
   * @param className The name of the outer DTO class.
   * @param implType DTO impls type, "client" or "server".
   */
  DtoTemplate(String packageName, String className, String implType) {
    this.packageName = packageName;
    this.className = className;
    this.implType = implType;
  }

  public String getImplType() {
    return implType;
  }

  public void addImplementation(Class<?> dtoInterface, Class<?> impl) {
    Set<Class<?>> classes = implementedDtoInterfaces.get(dtoInterface);
    if (classes == null) {
      implementedDtoInterfaces.put(dtoInterface, classes = new LinkedHashSet<>());
    }
    classes.add(impl);
  }

  /**
   * Some DTO interfaces may be already implemented in dependencies of current project. Try to reuse
   * them. If this method returns <code>null</code> it means interface is not implemented yet.
   */
  public Class<?> getDtoImplementation(Class<?> dtoInterface) {
    Set<Class<?>> classes = implementedDtoInterfaces.get(dtoInterface);
    if (classes == null || classes.isEmpty()) {
      return null;
    }
    for (Class<?> impl : classes) {
      if (impl.getSimpleName().equals(dtoInterface.getSimpleName() + "Impl")) {
        return impl;
      }
    }
    return null;
  }

  /**
   * Adds an interface to the DtoTemplate for code generation.
   *
   * @param i interface to add
   */
  public void addInterface(Class<?> i) {
    getDtoInterfaces().add(createDtoImplTemplate(i));
  }

  /** @return the dtoInterfaces */
  public List<DtoImpl> getDtoInterfaces() {
    return dtoInterfaces;
  }

  /**
   * Returns the source code for a class that contains all the DTO impls for any interfaces that
   * were added via the {@link #addInterface(Class)} method.
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    emitPreamble(builder);
    emitDtos(builder);
    emitPostamble(builder);
    return builder.toString();
  }

  /**
   * Tests whether or not a given class is a part of our dto jar, and thus will eventually have a
   * generated Impl that is serializable (thus allowing it to be a generic type).
   */
  boolean isDtoInterface(Class<?> potentialDto) {
    for (DtoImpl dto : getDtoInterfaces()) {
      if (dto.getDtoInterface().equals(potentialDto)) {
        return true;
      }
    }
    return false;
  }

  private DtoImpl createDtoImplTemplate(Class<?> i) {
    if ("server".equals(implType)) {
      return new DtoImplServerTemplate(this, i);
    } else if ("client".equals(implType)) {
      return new DtoImplClientTemplate(this, i);
    }
    throw new IllegalStateException(
        "Unsupported DTO implementation type, must be 'client' or 'server'");
  }

  private void emitDtos(StringBuilder builder) {
    for (DtoImpl dto : getDtoInterfaces()) {
      builder.append(dto.serialize());
    }
  }

  private void emitPostamble(StringBuilder builder) {
    builder.append("\n}");
  }

  private void emitPreamble(StringBuilder builder) {
    builder.append(
        "/*******************************************************************************\n");
    builder.append(" * Copyright (c) 2012-2016 Red Hat, Inc.\n");
    builder.append(" * All rights reserved. This program and the accompanying materials\n");
    builder.append(" * are made available under the terms of the Eclipse Public License v1.0\n");
    builder.append(" * which accompanies this distribution, and is available at\n");
    builder.append(" * http://www.eclipse.org/legal/epl-v10.html\n");
    builder.append(" *\n");
    builder.append(" * Contributors:\n");
    builder.append(" * Red Hat, Inc. - initial API and implementation\n");
    builder.append(
        " *******************************************************************************/\n\n\n");
    builder.append("// GENERATED SOURCE. DO NOT EDIT.\npackage ");
    builder.append(packageName);
    builder.append(";\n\n");
    if ("server".equals(implType)) {
      builder.append("import org.eclipse.che.dto.server.JsonSerializable;\n");
      builder.append("\n");
      builder.append("import com.google.gson.Gson;\n");
      builder.append("import com.google.gson.GsonBuilder;\n");
      builder.append("import com.google.gson.JsonArray;\n");
      builder.append("import com.google.gson.JsonElement;\n");
      builder.append("import com.google.gson.JsonNull;\n");
      builder.append("import com.google.gson.JsonObject;\n");
      builder.append("import com.google.gson.JsonParser;\n");
      builder.append("import com.google.gson.JsonPrimitive;\n");
      builder.append("\n");
      builder.append("import java.util.List;\n");
      builder.append("import java.util.Map;\n");
    }
    if ("client".equals(implType)) {
      builder.append("import org.eclipse.che.ide.dto.ClientDtoFactoryVisitor;\n");
      builder.append("import org.eclipse.che.ide.dto.DtoFactoryVisitor;\n");
      builder.append("import org.eclipse.che.ide.dto.JsonSerializable;\n");
      builder.append("import com.google.gwt.json.client.*;\n");
      builder.append("import com.google.inject.Singleton;\n");
    }
    builder.append("\n\n@SuppressWarnings({\"unchecked\", \"cast\", \"MissingOverride\"})\n");
    if ("client".equals(implType)) {
      builder.append("@Singleton\n");
      builder.append("@ClientDtoFactoryVisitor\n");
    }
    // Note that we always use fully qualified path names when referencing Types
    // so we need not add any import statements for anything.
    builder.append("public class ");
    builder.append(className);
    if ("server".equals(implType)) {
      builder.append(" implements ").append(DtoFactoryVisitor.class.getCanonicalName());
    }
    if ("client".equals(implType)) {
      builder.append(" implements ").append("DtoFactoryVisitor");
    }
    builder.append(" {\n\n");
    if ("server".equals(implType)) {
      builder.append(
          "  private static final Gson gson = org.eclipse.che.dto.server.DtoFactory.getInstance().getGson();\n\n");
      builder.append(
          "  @Override\n"
              + "  public void accept(org.eclipse.che.dto.server.DtoFactory dtoFactory) {\n");
      for (DtoImpl dto : getDtoInterfaces()) {
        String dtoInterface = dto.getDtoInterface().getCanonicalName();
        builder
            .append("    dtoFactory.registerProvider(")
            .append(dtoInterface)
            .append(".class")
            .append(", ")
            .append("new org.eclipse.che.dto.server.DtoProvider<")
            .append(dtoInterface)
            .append(">() {\n");
        builder
            .append("        public Class<? extends ")
            .append(dtoInterface)
            .append("> getImplClass() {\n")
            .append("            return ")
            .append(dto.getImplClassName())
            .append(".class;\n");
        builder.append("        }\n\n");
        builder
            .append("        public ")
            .append(dtoInterface)
            .append(" newInstance() {\n")
            .append("            return ")
            .append(dto.getImplClassName())
            .append(".make();\n");
        builder.append("        }\n\n");
        builder
            .append("        public ")
            .append(dtoInterface)
            .append(" fromJson(String json) {\n")
            .append("            return ")
            .append(dto.getImplClassName())
            .append(".fromJsonString(json);\n");
        builder.append("        }\n\n");
        builder
            .append("        public ")
            .append(dtoInterface)
            .append(" fromJson(com.google.gson.JsonElement json) {\n")
            .append("            return ")
            .append(dto.getImplClassName())
            .append(".fromJsonElement(json);\n");
        builder.append("        }\n\n");
        builder
            .append("        public ")
            .append(dtoInterface)
            .append(" clone(")
            .append(dtoInterface)
            .append(" origin) {\n")
            .append("            return new ")
            .append(dto.getImplClassName())
            .append("(origin);\n");
        builder.append("        }\n");
        builder.append("    });\n");
      }
      builder.append("  }\n\n");
    }
    if ("client".equals(implType)) {
      builder
          .append("  @Override\n")
          .append("  public void accept(org.eclipse.che.ide.dto.DtoFactory dtoFactory) {\n");
      for (DtoImpl dto : getDtoInterfaces()) {
        String dtoInterface = dto.getDtoInterface().getCanonicalName();
        builder
            .append("    dtoFactory.registerProvider(")
            .append(dtoInterface)
            .append(".class")
            .append(", ")
            .append("new org.eclipse.che.ide.dto.DtoProvider<")
            .append(dtoInterface)
            .append(">() {\n");
        builder
            .append("        public Class<? extends ")
            .append(dtoInterface)
            .append("> getImplClass() {\n")
            .append("            return ")
            .append(dto.getImplClassName())
            .append(".class;\n");
        builder.append("        }\n\n");
        builder
            .append("        public ")
            .append(dtoInterface)
            .append(" newInstance() {\n")
            .append("            return ")
            .append(dto.getImplClassName())
            .append(".make();\n");
        builder.append("        }\n\n");
        builder
            .append("        public ")
            .append(dtoInterface)
            .append(" fromJson(String json) {\n")
            .append("            return ")
            .append(dto.getImplClassName())
            .append(".fromJsonString(json);\n");
        builder.append("        }\n");
        builder.append("    });\n");
      }
      builder.append("  }\n\n");
    }
  }
}
