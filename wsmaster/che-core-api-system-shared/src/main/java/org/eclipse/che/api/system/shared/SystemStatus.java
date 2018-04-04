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
package org.eclipse.che.api.system.shared;

/**
 * Defines system status.
 *
 * @author Yevhenii Voevodin
 */
public enum SystemStatus {

  /** The system is running, which means that it wasn't stopped via system API. */
  RUNNING,

  /** The system stops corresponding services and will be eventually {@link #READY_TO_SHUTDOWN}. */
  PREPARING_TO_SHUTDOWN,

  /** All the necessary services are stopped, system is ready to be shut down. */
  READY_TO_SHUTDOWN
}
