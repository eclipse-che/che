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
package org.eclipse.che.ide.processes.actions;

/** Factory to create menu for adding new tab */
public interface AddTabMenuFactory {

  /**
   * Creates new menu for adding new tab in processes panel.
   *
   * @return menu
   */
  AddTabMenu newAddTabMenu();
}
