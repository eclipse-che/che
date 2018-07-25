/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.workspace;

import java.util.concurrent.ExecutionException;
import org.eclipse.che.selenium.core.user.TestUser;

/**
 * Represents workspace in a test environment.
 *
 * @author Anatolii Bazko
 */
public interface TestWorkspace {

  /** Return the name of the workspace. */
  String getName() throws ExecutionException, InterruptedException;

  /** Returns the id of the workspace. */
  String getId() throws ExecutionException, InterruptedException;

  /** Returns owner of the workspace. */
  TestUser getOwner();

  /** Waits until workspace is started. */
  void await() throws InterruptedException, ExecutionException;

  /** Deletes workspace. */
  void delete();
}
