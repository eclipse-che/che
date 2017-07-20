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
package org.eclipse.che.selenium.core.workspace;

import org.eclipse.che.selenium.core.user.TestUser;

import java.util.concurrent.ExecutionException;

/**
 * Represents workspace in a test environment.
 *
 * @author Anatolii Bazko
 */
public interface TestWorkspace {

    /**
     * Return the name of the workspace.
     */
    String getName() throws ExecutionException, InterruptedException;

    /**
     * Returns the id of the workspace.
     */
    String getId() throws ExecutionException, InterruptedException;

    /**
     * Returns owner of the workspace.
     */
    TestUser getOwner();

    /**
     * Waits until workspace is started.
     */
    void await() throws InterruptedException, ExecutionException;

    /**
     * Deletes workspace.
     */
    void delete();
}
