/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

import com.google.common.annotations.VisibleForTesting;
import java.util.regex.Pattern;
import org.eclipse.che.api.core.ValidationException;

public final class NamespaceNameValidator {

  static final int METADATA_NAME_MAX_LENGTH = 63;
  private static final String METADATA_NAME_REGEX = "[a-z0-9]([-a-z0-9]*[a-z0-9])?";
  private static final Pattern METADATA_NAME_PATTERN = Pattern.compile(METADATA_NAME_REGEX);

  private NamespaceNameValidator() {
    throw new AssertionError();
  }

  /**
   * Checks whether the provided name is a valid namespace name and throws an exception detailing
   * the validation result if it is not.
   *
   * @throws ValidationException if the provided name is not a valid namespace name
   */
  public static void validate(String namespaceName) throws ValidationException {
    ValidationResult res = validateInternal(namespaceName);
    if (!res.isOk()) {
      throw res.toException(namespaceName);
    }
  }

  /**
   * Similar to {@link #validate(String)} but doesn't throw an exception. The caller cannot
   * distinguish why the name is not valid.
   *
   * @return true if the namespace is valid, false otherwise
   */
  public static boolean isValid(String name) {
    return validateInternal(name).isOk();
  }

  @VisibleForTesting
  static ValidationResult validateInternal(String namespaceName) {
    if (isNullOrEmpty(namespaceName)) {
      return ValidationResult.NULL_OR_EMPTY;
    }

    if (namespaceName.length() > METADATA_NAME_MAX_LENGTH) {
      return ValidationResult.TOO_LONG;
    }

    if (!METADATA_NAME_PATTERN.matcher(namespaceName).matches()) {
      return ValidationResult.INVALID;
    }

    return ValidationResult.OK;
  }

  @VisibleForTesting
  enum ValidationResult {
    OK,
    NULL_OR_EMPTY,
    TOO_LONG,
    INVALID;

    boolean isOk() {
      return this == OK;
    }

    ValidationException toException(String namespaceName) {
      switch (this) {
        case OK:
          throw new IllegalStateException(
              "Tried to convert a successful validation into" + " an exception. This is a bug.");
        case NULL_OR_EMPTY:
          return new ValidationException("Namespace name cannot be undefined.");
        case TOO_LONG:
          return new ValidationException(
              "The specified namespace "
                  + namespaceName
                  + " is invalid: must be no more than 63 characters");
        case INVALID:
          return new ValidationException(
              "The specified namespace "
                  + namespaceName
                  + " is invalid: a DNS-1123 label must consist of lower case alphanumeric"
                  + " characters or '-', and must start and end with an"
                  + " alphanumeric character (e.g. 'my-name', or '123-abc', regex used for"
                  + " validation is '"
                  + METADATA_NAME_REGEX
                  + "')");
        default:
          throw new IllegalStateException(
              format(
                  "Could not convert namespace validation failure '%s' to a validation exception."
                      + " This is a bug.",
                  this));
      }
    }
  }
}
