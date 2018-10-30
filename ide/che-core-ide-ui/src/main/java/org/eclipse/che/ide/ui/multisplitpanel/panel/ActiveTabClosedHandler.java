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
package org.eclipse.che.ide.ui.multisplitpanel.panel;

import com.google.gwt.event.shared.EventHandler;
import org.eclipse.che.ide.ui.multisplitpanel.tab.Tab;

/** Handler to process case when active {@link Tab} of {@link SubPanelView} is closed */
public interface ActiveTabClosedHandler extends EventHandler {

  /**
   * Called when a active tab is closed.
   *
   * @param panel panel which contains closed tab
   * @param tabToActivate nearby tab to activate
   */
  void onActiveTabClosed(SubPanelView panel, Tab tabToActivate);
}
