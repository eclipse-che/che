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

import org.eclipse.che.ide.api.multisplitpanel.FocusListener;
import org.eclipse.che.ide.api.multisplitpanel.SubPanel;
import org.eclipse.che.ide.api.multisplitpanel.SubPanelFactory;
import org.eclipse.che.ide.api.multisplitpanel.WidgetToShow;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
public class SubPanelPresenter implements SubPanelView.ActionDelegate, SubPanel {

    private final SubPanelView    view;
    private final SubPanelFactory subPanelFactory;
    private final FocusListener   focusListener;

    @Inject
    public SubPanelPresenter(SubPanelView view,
                             SubPanelFactory subPanelFactory,
                             @Assisted FocusListener focusListener,
                             @Assisted SubPanel parentPanel) {
        this.view = view;
        this.subPanelFactory = subPanelFactory;
        this.focusListener = focusListener;

        view.setDelegate(this);
        if (parentPanel != null) {
            view.setParent((SubPanelView)parentPanel.getView());
        }
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    public void onFocused() {
        focusListener.focusGained(this);
    }

    @Override
    public void onSplitHorizontallyClicked() {
        SubPanel subPanel = subPanelFactory.newPanel(focusListener, this);
        view.splitHorizontally(subPanel.getView());
    }

    @Override
    public void onSplitVerticallyClicked() {
        SubPanel subPanel = subPanelFactory.newPanel(focusListener, this);
        view.splitVertically(subPanel.getView());
    }

    @Override
    public void onClosePaneClicked() {
        view.removeCentralPanel();
    }

    @Override
    public void addWidget(WidgetToShow widget) {
        view.addWidget(widget.getWidget());
    }

    @Override
    public void activateWidget(WidgetToShow widget) {
        view.activateWidget(widget.getWidget());
    }

    @Override
    public void removeWidget(WidgetToShow widget) {
        view.removeWidget(widget.getWidget());
    }
}
