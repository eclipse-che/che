/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 ******************************************************************************/
package org.eclipse.che.ide.api.multisplitpanel;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * A panel that represents a tabbed set of pages, each of which contains another
 * widget. Its child widgets are shown as the user selects the various tabs
 * associated with them.
 * <p>
 * A panel may be split on two sub-panels vertically or horizontally.
 * Each sub-panel may be closed.
 *
 * @author Artem Zatsarynnyi
 */
public interface SubPanel {

    /**
     * Add the given {@code widget} to this panel.
     *
     * @param widget
     *         widget to add
     * @param closeListener
     *         listener to be notified when tab with the specified {@code widget} is closed
     */
    void addWidget(WidgetToShow widget, CloseListener closeListener);

    /** Show (activate) the {@code widget} if it exists on this panel. */
    void activateWidget(WidgetToShow widget);

    /**
     * Remove the given {@code widget} from this panel.
     *
     * @param widget
     *         widget to remove
     */
    void removeWidget(WidgetToShow widget);

    /** Returns the panel's view. */
    IsWidget getView();

    /**
     * Set the listener to be notified when some widget on
     * this panel or on any child sub-panel gains the focus.
     */
    void setFocusListener(FocusListener listener);
}
