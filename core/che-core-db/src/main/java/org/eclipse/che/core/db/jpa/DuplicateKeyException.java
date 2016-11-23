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
package org.eclipse.che.core.db.jpa;

import org.eclipse.che.core.db.DBErrorCode;

/**
 * Thrown when data couldn't be updated/stored due to unique constrain violation.
 *
 * @author Yevhenii Voevodin
 * @see DBErrorCode#DUPLICATE_KEY
 */
public class DuplicateKeyException extends DetailedRollbackException {

    public DuplicateKeyException(String message, Throwable cause) {
        super(message, cause, DBErrorCode.DUPLICATE_KEY);
    }
}
