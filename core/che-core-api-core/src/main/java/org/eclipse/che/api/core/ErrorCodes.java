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
package org.eclipse.che.api.core;

/**
 * Error codes that are used in exceptions.
 * Defined error codes MUST BE in range <b>15000-32999</b> inclusive.
 *
 * @author Igor Vinokur
 */
public class ErrorCodes {
    private ErrorCodes() {
    }

    public static final int NO_COMMITTER_NAME_OR_EMAIL_DEFINED = 15216;
    public static final int UNABLE_GET_PRIVATE_SSH_KEY         = 32068;
    public static final int UNAUTHORIZED_GIT_OPERATION         = 32080;
    public static final int UNAUTHORIZED_SVN_OPERATION         = 32090;
    public static final int MERGE_CONFLICT                     = 32062;
    public static final int FAILED_CHECKOUT                    = 32063;
    public static final int FAILED_CHECKOUT_WITH_START_POINT   = 32064;
    public static final int INIT_COMMIT_WAS_NOT_PERFORMED      = 32082;
}
