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
    if (stack == null) {
      throw new BadRequestException("Required non-null stack");
    }
    if (stack.getName() == null || stack.getName().isEmpty()) {
      throw new BadRequestException("Required non-null and non-empty stack name");
    }
    if (stack.getScope() == null
        || !stack.getScope().equals("general") && !stack.getScope().equals("advanced")) {
      throw new BadRequestException("Required non-null scope value: 'general' or 'advanced'");
    }
    if (stack.getWorkspaceConfig() == null) {
      throw new BadRequestException("Workspace config required");
    }
    try {
      wsValidator.validateConfig(stack.getWorkspaceConfig());
    } catch (ValidationException x) {
      throw new BadRequestException(x.getMessage());
    }
  }
}
