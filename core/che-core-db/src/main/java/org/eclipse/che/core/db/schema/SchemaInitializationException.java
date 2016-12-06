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
package org.eclipse.che.core.db.schema;

/**
 * Thrown when any schema initialization/migration problem occurs.
 *
 * @author Yevhenii Voevodin
 */
public class SchemaInitializationException extends Exception {

    public SchemaInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemaInitializationException(String message) {
        super(message);
    }
}
