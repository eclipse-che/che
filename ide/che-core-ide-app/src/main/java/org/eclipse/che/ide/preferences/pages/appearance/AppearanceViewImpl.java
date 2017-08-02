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
package org.eclipse.che.ide.preferences.pages.appearance;

import org.eclipse.che.ide.api.theme.Theme;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;

import org.eclipse.che.ide.ui.listbox.CustomListBox;

import com.google.gwt.user.client.ui.Widget;

import java.util.List;

/**
 * @author Evgen Vidolob
 */
public class AppearanceViewImpl implements AppearanceView {

    private static AppearanceViewImplUiBinder ourUiBinder = GWT.create(AppearanceViewImplUiBinder.class);
    private final FlowPanel rootElement;
    @UiField
    CustomListBox themeBox;
    private ActionDelegate delegate;

    public AppearanceViewImpl() {
        rootElement = ourUiBinder.createAndBindUi(this);

    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

    @Override
    public void setThemes(List<Theme> themes, String currentThemeId) {
        themeBox.clear();

        for (Theme t : themes) {
            themeBox.addItem(t.getDescription(), t.getId());
            if (t.getId().equals(currentThemeId)) {
                themeBox.setSelectedIndex(themes.indexOf(t));
            }
        }
    }

    @UiHandler("themeBox")
    void handleSelectionChanged(ChangeEvent event) {
        themeBox.getSelectedIndex();
        delegate.themeSelected(themeBox.getValue(themeBox.getSelectedIndex()));
    }

    interface AppearanceViewImplUiBinder
            extends UiBinder<FlowPanel, AppearanceViewImpl> {
    }
}