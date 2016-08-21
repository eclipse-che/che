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

import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;

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

    /** Returns the panel's view. */
    IsWidget getView();

    void splitHorizontally();

    void splitVertically();

    /**
     * Add the given {@code widget} to this panel.
     *
     * @param widget
     *         widget to add
     * @param closingListener
     *         listener to be notified when tab with the specified {@code widget} is closed
     */
    void addWidget(WidgetToShow widget, @Nullable ClosingListener closingListener);

    /** Show (activate) the {@code widget} if it exists on this panel. */
    void activateWidget(WidgetToShow widget);

    List<WidgetToShow> getAllWidgets();

    /**
     * Remove the given {@code widget} from this panel.
     *
     * @param widget
     *         widget to remove
     */
    void removeWidget(WidgetToShow widget);

    void closePane();

    /**
     * Set the listener to be notified when some widget on
     * this panel or on any child sub-panel gains the focus.
     */
    void setFocusListener(FocusListener listener);

}
