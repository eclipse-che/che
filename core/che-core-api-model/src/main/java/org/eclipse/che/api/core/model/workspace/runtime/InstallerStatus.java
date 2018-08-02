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
package org.eclipse.che.api.core.model.workspace.runtime;

/**
 * Describes possible installer statuses.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public enum InstallerStatus {

  /** Installer is started by bootstrapper. */
  STARTING,

  /** Installer successfully started, defined server is running. */
  RUNNING,

  /** Installer successfully started, no servers was defined. */
  DONE,

  /** Installer failed. */
  FAILED
}
