/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.dialogs.input;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Validator for {@link InputDialog}.
 *
 * @author Artem Zatsarynnyi
 */
public interface InputValidator {
  /**
   * Validates the given {@code value}.
   *
   * <p>Returns {@link Violation} instance that may contains an error message or {@code null} if
   * there is no validation error. Note that the empty message returned by {@link
   * Violation#getMessage()} is treated that error state but with no message to display.
   *
   * @param value value to check for validity
   * @return {@link Violation} instance if {@code value} isn't valid or {@code null} otherwise
   */
  @Nullable
  Violation validate(String value);

  /** Describes a violation of validation constraint. */
  interface Violation {
    /** Returns an error message for violation of validation constraints. */
    @Nullable
    String getMessage();

    /**
     * Returns the corrected value for replacement. Note that not {@code null} value returned by
     * this method is treated that not error state and no message to display. In this case the wrong
     * value will be replaced with the result of this method.
     */
    @Nullable
    String getCorrectedValue();
  }
}
