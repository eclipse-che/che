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
package org.eclipse.che.ide.api.debug;

/**
 * The type of a debug configuration.
 *
 * @author Artem Zatsarynnyi
 */
public interface DebugConfigurationType {

  /** Returns unique identifier for this debug configuration type. */
  String getId();

  /** Returns the display name of this debug configuration type. */
  String getDisplayName();

  /**
   * Returns the {@link DebugConfigurationPage} that allows to edit debug configuration of this
   * type.
   */
  DebugConfigurationPage<? extends DebugConfiguration> getConfigurationPage();
}
