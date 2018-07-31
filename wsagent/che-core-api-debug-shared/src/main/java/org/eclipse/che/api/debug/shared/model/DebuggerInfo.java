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
package org.eclipse.che.api.debug.shared.model;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Summary of debugger information.
 *
 * @author Anatoliy Bazko
 */
public interface DebuggerInfo {
  /** The host where debugger is connected to. */
  @Nullable
  String getHost();

  /** The port where debugger is connected to. */
  int getPort();

  /** The debugger name. */
  String getName();

  /** The debugger version. */
  String getVersion();

  /** The pid where debugger is connected to. */
  int getPid();

  /** The binary file used by debugger. */
  @Nullable
  String getFile();
}
