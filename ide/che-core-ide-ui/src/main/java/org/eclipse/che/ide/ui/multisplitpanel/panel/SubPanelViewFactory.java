/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
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

    SubPanelView createView(ClosePaneAction closePaneAction,
                            RemoveAllWidgetsInPaneAction removeAllWidgetsInPaneAction,
                            SplitHorizontallyAction splitHorizontallyAction,
                            SplitVerticallyAction splitVerticallyAction);
}
