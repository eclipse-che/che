/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.server.model;

import java.util.List;

/**
 * Describes relations between user and workspace
 *
 * @author Sergii Leschenko
 */
public interface Worker {
  /** Returns user id */
  String getUserId();

  /** Returns workspace id */
  String getWorkspaceId();

  /** Returns list of workspace actions which can be performed by current user */
  List<String> getActions();
}
