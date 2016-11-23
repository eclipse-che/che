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
 * Throws during inserts/updates entity that restricted by referential integrity
 * and given insert/update refers to non-existing entity.
 *
 * @author Anton Korneta
 * @see DBErrorCode#INTEGRITY_CONSTRAINT_VIOLATION
 */
public class IntegrityConstraintViolationException extends DetailedRollbackException {

    public IntegrityConstraintViolationException(String message, Throwable cause) {
        super(message, cause, DBErrorCode.INTEGRITY_CONSTRAINT_VIOLATION);
    }
}
