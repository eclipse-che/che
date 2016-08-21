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
import org.eclipse.che.ide.api.multisplitpanel.CloseCallback;
import org.eclipse.che.ide.api.multisplitpanel.ClosingListener;
import org.eclipse.che.ide.api.multisplitpanel.FocusListener;
import org.eclipse.che.ide.api.multisplitpanel.SubPanel;
import org.eclipse.che.ide.api.multisplitpanel.SubPanelFactory;
import org.eclipse.che.ide.api.multisplitpanel.WidgetToShow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
public class SubPanelPresenter implements SubPanel, SubPanelView.ActionDelegate {

    private final SubPanelView                   view;
    private final SubPanelFactory                subPanelFactory;
    private final List<WidgetToShow>             widgets;
    private final Map<IsWidget, ClosingListener> closeListeners;

    private FocusListener focusListener;

    @Inject
    public SubPanelPresenter(SubPanelFactory subPanelFactory,
                             SubPanelViewFactory subPanelViewFactory,
                             @Assisted @Nullable SubPanel parentPanel) {
        this.subPanelFactory = subPanelFactory;

        widgets = new ArrayList<>();

        this.view = subPanelViewFactory.createView(new ClosePaneAction(this),
                                                   new CloseAllTabsInPaneAction(this),
                                                   new SplitHorizontallyAction(this),
                                                   new SplitVerticallyAction(this));

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
    public void addWidget(WidgetToShow widget, @Nullable ClosingListener closingListener) {
        // TODO: just activate the widget if it's already exists on the panel

        widgets.add(widget);

        if (closingListener != null) {
            closeListeners.put(widget.getWidget(), closingListener);
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
    public void onWidgetRemoving(IsWidget widget, CloseCallback closeCallback) {
        final ClosingListener listener = closeListeners.remove(widget);
        if (listener != null) {
            listener.onTabClosing(closeCallback);
        }
    }
}
