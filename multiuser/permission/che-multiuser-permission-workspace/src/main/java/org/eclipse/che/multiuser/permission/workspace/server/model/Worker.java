/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
