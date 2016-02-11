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
package org.eclipse.che.api.vfs.shared;

/**
 * Provide set of exit codes of Virtual Files System operation. Such codes can be used as a supplement to the HTTP status of the
 * client to help define more precisely the type of error.
 *
 * @author <a href="mailto:aparfonov@exoplatform.com">Andrey Parfonov</a>
 */
@Deprecated
public final class ExitCodes {
    public static final int SUCCESS = 0;

    /** If operation fails cause to any constraints. */
    public static final int CONSTRAINT = 100;

    /** If any parameter of request is not acceptable. */
    public static final int INVALID_ARGUMENT = 101;

    /** Name conflict. */
    public static final int ITEM_EXISTS = 102;

    /** Item with specified path or ID does not exist. */
    public static final int ITEM_NOT_FOUND = 103;

    /** Lock conflict. */
    public static final int LOCK_CONFLICT = 104;

    /** Requested action is not supported. */
    public static final int UNSUPPORTED = 105;

    /** Performed action is not allowed for caller. */
    public static final int NOT_PERMITTED = 106;

    public static final int INTERNAL_ERROR = 200;

    private ExitCodes() {
    }
}
