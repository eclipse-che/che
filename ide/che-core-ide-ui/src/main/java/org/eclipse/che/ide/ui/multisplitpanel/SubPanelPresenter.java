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
package org.eclipse.che.ide.ui.multisplitpanel;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.multisplitpanel.CloseListener;
import org.eclipse.che.ide.api.multisplitpanel.FocusListener;
import org.eclipse.che.ide.api.multisplitpanel.SubPanel;
import org.eclipse.che.ide.api.multisplitpanel.SubPanelFactory;
import org.eclipse.che.ide.api.multisplitpanel.WidgetToShow;
import org.eclipse.che.ide.api.parts.PartStackUIResources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
public class SubPanelPresenter implements SubPanel, SubPanelView.ActionDelegate {

    private final SubPanelView                 view;
    private final SubPanelFactory              subPanelFactory;
    private final List<WidgetToShow>           widgets;
    private final Map<IsWidget, CloseListener> closeListeners;

    private FocusListener focusListener;

    @Inject
    public SubPanelPresenter(SubPanelFactory subPanelFactory,
                             SubPanelViewFactory subPanelViewFactory,
                             PartStackUIResources resources,
                             @Assisted @Nullable SubPanel parentPanel) {
        this.subPanelFactory = subPanelFactory;

        widgets = new ArrayList<>();

        this.view = subPanelViewFactory.createView(new ClosePaneAction(resources, this),
                                                   new CloseAllTabsInPaneAction(resources, this),
                                                   new SplitHorizontallyAction(resources, this),
                                                   new SplitVerticallyAction(resources, this));

        closeListeners = new HashMap<>();

        view.setDelegate(this);

        if (parentPanel != null) {
            view.setParentPanel((SubPanelView)parentPanel.getView());
        }
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    public void splitHorizontally() {
        final SubPanel subPanel = subPanelFactory.newPanel(this);
        subPanel.setFocusListener(focusListener);
        view.splitHorizontally(subPanel.getView());
    }

    @Override
    public void splitVertically() {
        final SubPanel subPanel = subPanelFactory.newPanel(this);
        subPanel.setFocusListener(focusListener);
        view.splitVertically(subPanel.getView());
    }

    @Override
    public void addWidget(WidgetToShow widget, @Nullable CloseListener closeListener) {
        // TODO: just activate the widget if it's already exists on the panel

        widgets.add(widget);

        if (closeListener != null) {
            closeListeners.put(widget.getWidget(), closeListener);
        }

        view.addWidget(widget);
    }

    @Override
    public void activateWidget(WidgetToShow widget) {
        view.activateWidget(widget);
    }

    @Override
    public List<WidgetToShow> getAllWidgets() {
        return new ArrayList<>(widgets);
    }

    @Override
    public void removeWidget(WidgetToShow widget) {
        view.removeWidget(widget);
        widgets.remove(widget);
    }

    @Override
    public void closePane() {
        view.removeCentralPanel();
    }

    @Override
    public void setFocusListener(FocusListener listener) {
        focusListener = listener;
    }

    @Override
    public void onWidgetFocused(IsWidget widget) {
        focusListener.focusGained(this, widget);
    }

    @Override
    public void onWidgetRemoved(IsWidget widget) {
        CloseListener listener = closeListeners.get(widget);
        if (listener != null) {
            listener.tabClosed();
        }
    }
}
