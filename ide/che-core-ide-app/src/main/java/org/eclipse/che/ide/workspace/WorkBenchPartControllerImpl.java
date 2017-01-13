/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.workspace;

import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Implementation of WorkBenchPartController, used with SplitLayoutPanel as container
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
public class WorkBenchPartControllerImpl implements WorkBenchPartController {

    public static final int DURATION = 200;

    private final SplitLayoutPanel splitLayoutPanel;
    private final SimplePanel      widget;

    @Inject
    public WorkBenchPartControllerImpl(@Assisted SplitLayoutPanel splitLayoutPanel,
                                       @Assisted SimplePanel widget) {
        this.splitLayoutPanel = splitLayoutPanel;
        this.widget = widget;

        splitLayoutPanel.setWidgetToggleDisplayAllowed(widget, true);
        splitLayoutPanel.setWidgetHidden(widget, true);
        splitLayoutPanel.forceLayout();
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return splitLayoutPanel.getWidgetSize(widget);
    }

    /** {@inheritDoc} */
    @Override
    public void setSize(double size) {
        splitLayoutPanel.setWidgetSize(widget, size);
        splitLayoutPanel.animate(DURATION);
    }

    @Override
    public void maximize() {
        DockLayoutPanel.Direction direction = splitLayoutPanel.getWidgetDirection(widget);

        if (DockLayoutPanel.Direction.NORTH == direction || DockLayoutPanel.Direction.SOUTH == direction) {
            int maxHeight = splitLayoutPanel.getOffsetHeight() - splitLayoutPanel.getSplitterSize();
            splitLayoutPanel.setWidgetSize(widget, maxHeight);
            splitLayoutPanel.animate(DURATION);

        } else if (DockLayoutPanel.Direction.WEST == direction || DockLayoutPanel.Direction.EAST == direction) {
            int maxWidth = splitLayoutPanel.getOffsetWidth() - splitLayoutPanel.getSplitterSize();
            splitLayoutPanel.setWidgetSize(widget, maxWidth);
            splitLayoutPanel.animate(DURATION);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setHidden(boolean hidden) {
        if (!hidden) {
            splitLayoutPanel.setWidgetHidden(widget, false);
        }

        splitLayoutPanel.setWidgetSize(widget, hidden ? 0 : getSize());
        splitLayoutPanel.animate(DURATION);

    }

    @Override
    public boolean isHidden() {
        return splitLayoutPanel.getWidgetSize(widget) == 0;
    }

    @Override
    public void setMinSize(int minSize) {
        splitLayoutPanel.setWidgetMinSize(widget, minSize);
    }

}
