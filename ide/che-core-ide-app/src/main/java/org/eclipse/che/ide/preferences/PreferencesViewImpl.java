/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.preferences;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.html.TableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.ide.ui.list.CategoriesList;
import org.eclipse.che.ide.ui.list.Category;
import org.eclipse.che.ide.ui.list.CategoryRenderer;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.loging.Log;

/**
 * PreferenceViewImpl is the view of preferences. The view shows preference pages to the end user.
 * It has an area at the bottom containing OK, Apply and Close buttons, on the left hand side of
 * page is list of available preferences, on the right hand side of page is current preference page.
 *
 * @author Andrey Plotnikov
 */
@Singleton
public class PreferencesViewImpl extends Window implements PreferencesView {
  interface PreferenceViewImplUiBinder extends UiBinder<Widget, PreferencesViewImpl> {}

  Button btnClose;
  Button btnSave;
  Button btnRefresh;

  @UiField SimplePanel preferences;
  @UiField SimplePanel contentPanel;

  @UiField(provided = true)
  org.eclipse.che.ide.Resources resources;

  private CoreLocalizationConstant locale;
  private ActionDelegate delegate;
  private CategoriesList list;

  private final Category.CategoryEventDelegate<PreferencePagePresenter> preferencesPageDelegate =
      new Category.CategoryEventDelegate<PreferencePagePresenter>() {
        @Override
        public void onListItemClicked(
            com.google.gwt.dom.client.Element listItemBase, PreferencePagePresenter itemData) {
          delegate.onPreferenceSelected(itemData);
        }
      };

  private final CategoryRenderer<PreferencePagePresenter> preferencesPageRenderer =
      new CategoryRenderer<PreferencePagePresenter>() {
        @Override
        public void renderElement(
            com.google.gwt.dom.client.Element element, PreferencePagePresenter preference) {
          element.setInnerText(preference.getTitle());
        }

        @Override
        public com.google.gwt.dom.client.SpanElement renderCategory(
            Category<PreferencePagePresenter> category) {
          SpanElement spanElement = Document.get().createSpanElement();
          spanElement.setClassName(resources.defaultCategoriesListCss().headerText());
          spanElement.setInnerText(category.getTitle());
          return spanElement;
        }
      };

  /**
   * Create view.
   *
   * @param resources
   */
  @Inject
  protected PreferencesViewImpl(
      org.eclipse.che.ide.Resources resources,
      PreferenceViewImplUiBinder uiBinder,
      CoreLocalizationConstant locale) {
    this.resources = resources;
    this.locale = locale;

    Widget widget = uiBinder.createAndBindUi(this);

    this.setTitle("Preferences");
    this.setWidget(widget);

    // create list of preferences
    TableElement tableElement = Elements.createTableElement();
    tableElement.setAttribute("style", "width: 100%");
    list = new CategoriesList(resources);
    preferences.add(list);
    createButtons();
  }

  private void createButtons() {
    btnSave =
        addFooterButton(
            locale.save(),
            "window-preferences-storeChanges",
            event -> delegate.onSaveClicked(),
            true);
    btnRefresh =
        addFooterButton(
            locale.refresh(), "window-preferences-refresh", event -> delegate.onRefreshClicked());
    btnClose =
        addFooterButton(
            locale.close(),
            "window-preferences-close",
            event -> {
              delegate.onCloseClicked();
              Log.info(getClass(), "close clicked");
            });
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    hide();
  }

  /** {@inheritDoc} */
  @Override
  public AcceptsOneWidget getContentPanel() {
    return contentPanel;
  }

  /** {@inheritDoc} */
  @Override
  public void enableSaveButton(boolean enabled) {
    btnSave.setEnabled(enabled);
  }

  /** {@inheritDoc} */
  @Override
  public void setPreferences(Map<String, Set<PreferencePagePresenter>> preferences) {
    List<Category<?>> categoriesList = new ArrayList<>();
    for (Entry<String, Set<PreferencePagePresenter>> entry : preferences.entrySet()) {
      categoriesList.add(
          new Category<>(
              entry.getKey(), preferencesPageRenderer, entry.getValue(), preferencesPageDelegate));
    }

    list.render(categoriesList, true);
  }

  /** {@inheritDoc} */
  @Override
  public void selectPreference(PreferencePagePresenter preference) {
    list.selectElement(preference);
  }

  @Override
  public void showDialog() {
    show(btnClose);
  }
}
