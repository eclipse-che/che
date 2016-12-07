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
package org.eclipse.che.ide.api.parts;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;
import java.util.List;

/** PartStack View interface */
public interface PartStackView extends View<PartStackView.ActionDelegate> {

    enum TabPosition {
        BELOW, LEFT, RIGHT
    }

    /** Tab which can be clicked and closed */
    interface TabItem extends ClickHandler, DoubleClickHandler {

        @NotNull
        IsWidget getView();

        @NotNull
        String getTitle();

        /**
         * Returns part tab icon
         *
         * @return part tab icon
         */
        Widget getIcon();

        /**
         * Updates part tab button.
         *
         * @param part part
         */
        void update(@NotNull PartPresenter part);

        /**
         * Selects part button.
         */
        void select();

        /**
         * Removes selection for the button.
         */
        void unSelect();

        /**
         * Determines position of the tab.
         *
         * @param tabPosition
         *         orientation of the Tab (e.g. LEFT or RIGHT)
         */
        void setTabPosition(@NotNull TabPosition tabPosition);

    }

    /** Add Tab */
    void addTab(@NotNull TabItem tabItem, @NotNull PartPresenter presenter);

    /** Remove Tab */
    void removeTab(@NotNull PartPresenter presenter);

    void selectTab(@NotNull PartPresenter partPresenter);

    /** Set new Tabs positions */
    void setTabPositions(List<PartPresenter> partPositions);

    /** Set PartStack focused */
    void setFocus(boolean focused);

    void setMaximized(boolean maximized);

    /** Update Tab */
    void updateTabItem(@NotNull PartPresenter partPresenter);

    /** Handles Focus Request Event. It is generated, when user clicks a stack anywhere */
    interface ActionDelegate {
        /** PartStack is being clicked and requests Focus */
        void onRequestFocus();
    }

}
