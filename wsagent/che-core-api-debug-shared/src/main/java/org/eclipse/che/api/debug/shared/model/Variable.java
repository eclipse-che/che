/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.debug.shared.model;

import java.util.List;

/**
 * @author Anatoliy Bazko
 */
public interface Variable {
    /**
     * The variable name.
     */
    String getName();

    boolean isExistInformation();

    /**
     * The variable value.
     */
    String getValue();

    /**
     * The variable type. E.g.: String, int etc.
     */
    String getType();

    /**
     * The path to the variable.
     */
    VariablePath getVariablePath();

    /**
     * Indicates if variable is primitive.
     */
    boolean isPrimitive();

    /**
     * The nested variables.
     */
    List<? extends Variable> getVariables();
}
