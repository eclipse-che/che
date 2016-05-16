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
package org.eclipse.che;


/**
 * //
 *
 * @author Vitalii Parfonov
 */

public class WorkspaceIdProvider   {

    public static final String CHE_WORKSPACE_ID = "CHE_WORKSPACE_ID";

    public static String getWorkspaceId() {
        return System.getenv(CHE_WORKSPACE_ID) == null ? "" : System.getenv(CHE_WORKSPACE_ID);
    }
}
