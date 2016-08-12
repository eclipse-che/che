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

import java.util.HashMap;
import java.util.Map;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
public class SubPanelPresenter implements SubPanel, SubPanelView.ActionDelegate {

    private final SubPanelView                 view;
    private final SubPanelFactory              subPanelFactory;
    private final Map<IsWidget, CloseListener> closeListeners;

    private FocusListener focusListener;

    @Inject
    public SubPanelPresenter(SubPanelView view,
                             SubPanelFactory subPanelFactory,
                             @Assisted @Nullable SubPanel parentPanel) {
        this.view = view;
        this.subPanelFactory = subPanelFactory;

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

    @Override
    public void onSplitHorizontallyClicked() {
        SubPanel subPanel = subPanelFactory.newPanel(this);
        subPanel.setFocusListener(focusListener);
        view.splitHorizontally(subPanel.getView());
    }

    @Override
    public void onSplitVerticallyClicked() {
        SubPanel subPanel = subPanelFactory.newPanel(this);
        subPanel.setFocusListener(focusListener);
        view.splitVertically(subPanel.getView());
    }

    @Override
    public void onClosePaneClicked() {
        view.removeCentralPanel();
    }

    @Override
    public void addWidget(WidgetToShow widget, CloseListener closeListener) {
        // TODO: just activate the widget if it's already exists on the panel

        closeListeners.put(widget.getWidget(), closeListener);

        view.addWidget(widget);
    }

    @Override
    public void activateWidget(WidgetToShow widget) {
        view.activateWidget(widget);
    }

    @Override
    public void removeWidget(WidgetToShow widget) {
        view.removeWidget(widget);
    }
}
