/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
