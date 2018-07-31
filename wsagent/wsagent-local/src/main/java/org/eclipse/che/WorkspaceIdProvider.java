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
package org.eclipse.che;

/**
 * Class provide workspace ID which linked to current developer machine it will take from
 * environment variable "CHE_WORKSPACE_ID" if this variable not set return empty String but in real
 * life should never be
 *
 * @author Vitalii Parfonov
 */
public class WorkspaceIdProvider {

  public static final String CHE_WORKSPACE_ID = "CHE_WORKSPACE_ID";

  public static String getWorkspaceId() {
    return System.getenv(CHE_WORKSPACE_ID) == null ? "" : System.getenv(CHE_WORKSPACE_ID);
  }
}
