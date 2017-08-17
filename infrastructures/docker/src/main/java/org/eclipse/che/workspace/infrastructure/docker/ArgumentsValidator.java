/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.ValidationException;

import static java.lang.String.format;

/**
 * @author Alexander Garagatyi
 */
public class ArgumentsValidator {
    /**
     * Checks that object reference is not null, throws {@link ValidationException} otherwise.
     *
     * <p>Exception uses error message built from error message template and error message parameters.
     */
    public static void checkNotNull(Object object, String errorMessageTemplate, Object... errorMessageParams)
            throws ValidationException {
        if (object == null) {
            throw new ValidationException(format(errorMessageTemplate, errorMessageParams));
        }
    }

    public static void checkArgument(boolean expression, String error) throws ValidationException {
        if (!expression) {
            throw new ValidationException(error);
        }
    }

    public static void checkArgument(boolean expression, String errorMessageTemplate, Object... errorMessageParams)
            throws ValidationException {
        if (!expression) {
            throw new ValidationException(format(errorMessageTemplate, errorMessageParams));
        }
    }

    private ArgumentsValidator() {}
}
