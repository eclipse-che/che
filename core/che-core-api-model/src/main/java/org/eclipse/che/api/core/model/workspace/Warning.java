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
package org.eclipse.che.api.core.model.workspace;

/**
 * Describes a warning, a couple of message and a code
 * which may indicate some context specific non-critical violation.
 *
 * @author Yevhenii Voevodin
 */
public interface Warning {

    /**
     * Returns the code of the warning.
     */
    int getCode();

    /**
     * Returns the message explaining the warning.
     */
    String getMessage();
}
