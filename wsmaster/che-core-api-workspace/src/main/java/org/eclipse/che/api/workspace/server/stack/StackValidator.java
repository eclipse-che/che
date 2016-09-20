/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.stack;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.WorkspaceValidator;
import org.eclipse.che.api.workspace.shared.stack.Stack;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * Validator for {@link Stack} objects
 *
 * @author Mihail Kuznyetsov
 */
@Singleton
public class StackValidator {

    @Inject
    private WorkspaceValidator wsValidator;

    /**
     * Validate stack object
     *
     * @param stack
     *          stack to validate
     * @throws BadRequestException if stack is not valid
     */
    public void check(Stack stack) throws BadRequestException, ServerException {
        if (stack == null) {
            throw new BadRequestException("Required non-null stack");
        }
        if (stack.getName() == null || stack.getName().isEmpty()) {
            throw new BadRequestException("Required non-null and non-empty stack name");
        }
        if (stack.getScope() == null || !stack.getScope().equals("general") && !stack.getScope().equals("advanced")) {
            throw new BadRequestException("Required non-null scope value: 'general' or 'advanced'");
        }
        if (stack.getSource() == null && stack.getWorkspaceConfig() == null) {
            throw new BadRequestException("Stack source required. You must specify either 'workspaceConfig' or 'stackSource'");
        }
        if (stack.getWorkspaceConfig() == null) {
            throw new BadRequestException("Workspace config required");
        }
        wsValidator.validateConfig(stack.getWorkspaceConfig());
    }
}
