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
package org.eclipse.che.ide.ext.java.client.settings.compiler;

import javax.validation.constraints.NotNull;

/**
 * The interface contains constants' ids to setup compiler.
 *
 * @author Dmitry Shnurenko
 */
public enum ErrorWarningsOptions {
  COMPILER_UNUSED_LOCAL("org.eclipse.jdt.core.compiler.problem.unusedLocal"),

  COMPILER_UNUSED_IMPORT("org.eclipse.jdt.core.compiler.problem.unusedImport"),

  DEAD_CODE("org.eclipse.jdt.core.compiler.problem.deadCode"),

  METHOD_WITH_CONSTRUCTOR_NAME("org.eclipse.jdt.core.compiler.problem.methodWithConstructorName"),

  UNNECESSARY_ELSE_STATEMENT("org.eclipse.jdt.core.compiler.problem.unnecessaryElse"),

  COMPARING_IDENTICAL_VALUES("org.eclipse.jdt.core.compiler.problem.comparingIdentical"),

  NO_EFFECT_ASSIGNMENT("org.eclipse.jdt.core.compiler.problem.noEffectAssignment"),

  MISSING_SERIAL_VERSION_UID("org.eclipse.jdt.core.compiler.problem.missingSerialVersion"),

  TYPE_PARAMETER_HIDE_ANOTHER_TYPE("org.eclipse.jdt.core.compiler.problem.typeParameterHiding"),

  FIELD_HIDES_ANOTHER_VARIABLE("org.eclipse.jdt.core.compiler.problem.fieldHiding"),

  MISSING_DEFAULT_CASE("org.eclipse.jdt.core.compiler.problem.missingDefaultCase"),

  UNUSED_PRIVATE_MEMBER("org.eclipse.jdt.core.compiler.problem.unusedPrivateMember"),

  UNCHECKED_TYPE_OPERATION("org.eclipse.jdt.core.compiler.problem.uncheckedTypeOperation"),

  USAGE_OF_RAW_TYPE("org.eclipse.jdt.core.compiler.problem.rawTypeReference"),

  MISSING_OVERRIDE_ANNOTATION("org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotation"),

  NULL_POINTER_ACCESS("org.eclipse.jdt.core.compiler.problem.nullReference"),

  POTENTIAL_NULL_POINTER_ACCESS("org.eclipse.jdt.core.compiler.problem.potentialNullReference"),

  REDUNDANT_NULL_CHECK("org.eclipse.jdt.core.compiler.problem.redundantNullCheck");

  private final String value;

  ErrorWarningsOptions(@NotNull String value) {
    this.value = value;
  }

  /** Returns value which associated with enum */
  @NotNull
  @Override
  public String toString() {
    return value;
  }
}
