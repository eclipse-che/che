/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.preferences.pages.general;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.ide.api.theme.Theme;
import org.eclipse.che.ide.preferences.PreferencesLocalizationConstants;
import org.eclipse.che.ide.ui.listbox.CustomListBox;

/** @author Evgen Vidolob */
public class IdeGeneralPreferencesViewImpl implements IdeGeneralPreferencesView {

  private static IdeGeneralPreferencesUiBinder ourUiBinder =
      GWT.create(IdeGeneralPreferencesUiBinder.class);
  private final FlowPanel rootElement;
  @UiField CustomListBox themeBox;
  @UiField CheckBox askBeforeClosingTab;

  @UiField(provided = true)
  PreferencesLocalizationConstants localizationConstants;

  private ActionDelegate delegate;

  @Inject
  public IdeGeneralPreferencesViewImpl(PreferencesLocalizationConstants localizationConstants) {
    this.localizationConstants = localizationConstants;

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

  @Override
  public boolean isAskBeforeClosingTab() {
    return askBeforeClosingTab.getValue();
  }

  @Override
  public void setAskBeforeClosingTab(boolean askBeforeClosingTab) {
    this.askBeforeClosingTab.setValue(askBeforeClosingTab);
  }

  @UiHandler("themeBox")
  void handleSelectionChanged(ChangeEvent event) {
    themeBox.getSelectedIndex();
    delegate.themeSelected(themeBox.getValue(themeBox.getSelectedIndex()));
  }

  @UiHandler("askBeforeClosingTab")
  void onAskBeforeClosingTabChanged(final ValueChangeEvent<Boolean> event) {
    delegate.onAskBeforeClosingTabChanged(event.getValue());
  }

  interface IdeGeneralPreferencesUiBinder
      extends UiBinder<FlowPanel, IdeGeneralPreferencesViewImpl> {}
}
