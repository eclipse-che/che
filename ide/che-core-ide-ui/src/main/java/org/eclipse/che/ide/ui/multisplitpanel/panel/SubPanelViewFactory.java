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

import org.eclipse.che.ide.ui.multisplitpanel.actions.ClosePaneAction;
import org.eclipse.che.ide.ui.multisplitpanel.actions.RemoveAllWidgetsInPaneAction;
import org.eclipse.che.ide.ui.multisplitpanel.actions.SplitHorizontallyAction;
import org.eclipse.che.ide.ui.multisplitpanel.actions.SplitVerticallyAction;

/**
 * Factory for the {@link SubPanelView} instances.
 *
 * @author Artem Zatsarynnyi
 */
public interface SubPanelViewFactory {

  SubPanelView createView(
      ClosePaneAction closePaneAction,
      RemoveAllWidgetsInPaneAction removeAllWidgetsInPaneAction,
      SplitHorizontallyAction splitHorizontallyAction,
      SplitVerticallyAction splitVerticallyAction);
}
