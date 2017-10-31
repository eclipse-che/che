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
 * Describes possible bootstrapper statuses.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public enum BootstrapperStatus {

  /** Bootstrapping is in progress. */
  STARTING,

  /** Bootstrapping done, everything is started ok. */
  DONE,

  /** Bootstrapping failed (when any installer fails or any error occurs). */
  FAILED
}
