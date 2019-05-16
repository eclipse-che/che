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
package org.eclipse.che.api.workspace.server.stack;

import static com.google.common.base.Strings.isNullOrEmpty;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.WorkspaceValidator;
import org.eclipse.che.api.workspace.shared.stack.Stack;

/**
 * Validator for {@link Stack} objects
 *
 * @author Mihail Kuznyetsov
 */
@Singleton
public class StackValidator {

  @Inject private WorkspaceValidator wsValidator;

  /**
   * Validate stack object
   *
   * @param stack stack to validate
   * @throws BadRequestException if stack is not valid
   */
  public void check(Stack stack) throws BadRequestException, ServerException, NotFoundException {
    checkArgument(stack != null, "Required non-null stack");

    checkArgument(!isNullOrEmpty(stack.getName()), "Required non-null and non-empty stack name");

    checkArgument(
        stack.getScope() != null
            && (stack.getScope().equals("general") || stack.getScope().equals("advanced")),
        "Required non-null scope value: 'general' or 'advanced'");

    checkArgument(
        stack.getWorkspaceConfig() != null ^ stack.getDevfile() != null,
        "Required non-null workspace configuration or devfile but not both");

    if (stack.getWorkspaceConfig() != null) {
      try {
        wsValidator.validateConfig(stack.getWorkspaceConfig());
      } catch (ValidationException x) {
        throw new BadRequestException(x.getMessage());
      }
    }
  }

  /**
   * Checks the specified expression.
   *
   * @param expression the expression to check
   * @param errorMessage error message that should be used if expression is false
   * @throws BadRequestException when the expression is false
   */
  private void checkArgument(boolean expression, String errorMessage) throws BadRequestException {
    if (!expression) {
      throw new BadRequestException(String.valueOf(errorMessage));
    }
  }
}
