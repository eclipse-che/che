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
package org.eclipse.che.ide.ext.java.client.settings.property;

import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.COMPARING_IDENTICAL_VALUES;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.COMPILER_UNUSED_IMPORT;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.COMPILER_UNUSED_LOCAL;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.DEAD_CODE;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.FIELD_HIDES_ANOTHER_VARIABLE;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.METHOD_WITH_CONSTRUCTOR_NAME;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.MISSING_DEFAULT_CASE;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.MISSING_OVERRIDE_ANNOTATION;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.MISSING_SERIAL_VERSION_UID;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.NO_EFFECT_ASSIGNMENT;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.NULL_POINTER_ACCESS;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.POTENTIAL_NULL_POINTER_ACCESS;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.REDUNDANT_NULL_CHECK;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.TYPE_PARAMETER_HIDE_ANOTHER_TYPE;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.UNCHECKED_TYPE_OPERATION;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.UNNECESSARY_ELSE_STATEMENT;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.UNUSED_PRIVATE_MEMBER;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.USAGE_OF_RAW_TYPE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions;

/**
 * The class contains compiler properties ids which match to properties' names.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
class PropertyNameManager {

  private final Map<ErrorWarningsOptions, String> names;

  @Inject
  public PropertyNameManager(JavaLocalizationConstant locale) {
    names = new HashMap<>();

    names.put(COMPILER_UNUSED_LOCAL, locale.propertyUnusedLocal());
    names.put(COMPILER_UNUSED_IMPORT, locale.propertyUnusedImport());
    names.put(DEAD_CODE, locale.propertyDeadCode());
    names.put(METHOD_WITH_CONSTRUCTOR_NAME, locale.propertyWithConstructorName());
    names.put(UNNECESSARY_ELSE_STATEMENT, locale.propertyUnnecessaryElse());
    names.put(COMPARING_IDENTICAL_VALUES, locale.comparingIdenticalValues());
    names.put(NO_EFFECT_ASSIGNMENT, locale.noEffectAssignment());
    names.put(MISSING_SERIAL_VERSION_UID, locale.missingSerialVersionUid());
    names.put(TYPE_PARAMETER_HIDE_ANOTHER_TYPE, locale.typeParameterHideAnotherType());
    names.put(FIELD_HIDES_ANOTHER_VARIABLE, locale.fieldHidesAnotherField());
    names.put(MISSING_DEFAULT_CASE, locale.missingSwitchDefaultCase());
    names.put(UNUSED_PRIVATE_MEMBER, locale.unusedPrivateMember());
    names.put(UNCHECKED_TYPE_OPERATION, locale.uncheckedTypeOperation());
    names.put(USAGE_OF_RAW_TYPE, locale.usageRawType());
    names.put(MISSING_OVERRIDE_ANNOTATION, locale.missingOverrideAnnotation());
    names.put(NULL_POINTER_ACCESS, locale.nullPointerAccess());
    names.put(POTENTIAL_NULL_POINTER_ACCESS, locale.potentialNullPointerAccess());
    names.put(REDUNDANT_NULL_CHECK, locale.redundantNullCheck());
  }

  /**
   * Returns property name using special id. Method can throw {@link IllegalArgumentException} if
   * name not found.
   *
   * @param propertyId id for which name will be returned
   * @return name of property
   */
  @NotNull
  public String getName(@NotNull ErrorWarningsOptions propertyId) {
    String name = names.get(propertyId);

    if (name == null) {
      throw new IllegalArgumentException(getClass() + "property name is not found...");
    }

    return name;
  }
}
