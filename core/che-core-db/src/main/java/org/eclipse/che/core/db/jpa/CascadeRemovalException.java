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

import javax.persistence.RollbackException;

/**
 * Throws when any exception during cascade remove occurs.
 *
 * <p>Note that in case of throwing this type of exception,
 * cascade removal transaction will be rolled back.
 *
 * @author Anton Korneta
 */
public class CascadeRemovalException extends RollbackException {

    public CascadeRemovalException(Throwable cause) {
        super(cause);
    }
}
