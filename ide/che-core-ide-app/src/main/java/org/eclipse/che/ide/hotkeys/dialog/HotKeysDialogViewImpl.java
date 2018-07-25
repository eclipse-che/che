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
package org.eclipse.che.ide.hotkeys.dialog;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.editor.hotkeys.HotKeyItem;
import org.eclipse.che.ide.api.keybinding.Scheme;
import org.eclipse.che.ide.hotkeys.HotKeyResources;
import org.eclipse.che.ide.ui.list.CategoriesList;
import org.eclipse.che.ide.ui.list.Category;
import org.eclipse.che.ide.ui.list.CategoryRenderer;
import org.eclipse.che.ide.ui.listbox.CustomListBox;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Implementation {@link HotKeysDialogView}
 *
 * @author Alexander Andrienko
 * @author Artem Zatsarynnyi
 * @author @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 */
@Singleton
public class HotKeysDialogViewImpl extends Window implements HotKeysDialogView {

  interface KeyMapViewImplUiBinder extends UiBinder<Widget, HotKeysDialogViewImpl> {}

  private final HotKeyResources hotKeyResources;

  private final Category.CategoryEventDelegate<HotKeyItem> keyBindingsEventDelegate =
      (listItemBase, hotKeyItem) -> {};

  private final CategoryRenderer<HotKeyItem> keyBindingsRenderer =
      new CategoryRenderer<HotKeyItem>() {
        @Override
        public void renderElement(Element element, HotKeyItem hotKeyItem) {
          element.setInnerText(hotKeyItem.getActionDescription());
          element.addClassName(hotKeyResources.css().description());
          if (hotKeyItem.isGlobal()) {
            element.addClassName(hotKeyResources.css().isGlobal());
          }

          DivElement hotKeyElem = Document.get().createDivElement();
          hotKeyElem.setInnerText(hotKeyItem.getHotKey());
          hotKeyElem.addClassName(hotKeyResources.css().hotKey());
          hotKeyElem.addClassName(hotKeyResources.css().floatRight());

          element.appendChild(hotKeyElem);
        }

        @Override
        public SpanElement renderCategory(Category<HotKeyItem> category) {
          SpanElement spanElement = Document.get().createSpanElement();
          spanElement.setInnerText(category.getTitle());
          return spanElement;
        }
      };

  private CategoriesList list;
  private List<Category<?>> categoriesList;
  private ActionDelegate delegate;
  private String filteredValue;

  Button saveButton;

  Button closeButton;

  Button printButton;

  @UiField FlowPanel category;

  @UiField TextBox filterInput;

  @UiField CustomListBox selectionListBox;

  @UiField FlowPanel selectionPanel;

  @Inject
  public HotKeysDialogViewImpl(
      KeyMapViewImplUiBinder uiBinder,
      CoreLocalizationConstant locale,
      org.eclipse.che.ide.Resources res,
      HotKeyResources hotKeyResources) {
    hotKeyResources.css().ensureInjected();

    this.hotKeyResources = hotKeyResources;

    this.setTitle(locale.keyBindingsDialogTitle());
    this.setWidget(uiBinder.createAndBindUi(this));

    saveButton =
        addFooterButton(
            locale.save(), "keybindings-saveButton-btn", event -> delegate.onSaveClicked(), true);

    closeButton =
        addFooterButton(
            locale.close(), "keybindings-closeButton-btn", event -> delegate.onCloseClicked());

    printButton =
        addFooterButton(
            locale.print(), "keybindings-printButton-btn", event -> delegate.onPrintClicked());

    list = new CategoriesList(res);
    categoriesList = new ArrayList<>();
    category.add(list);
    filterInput.getElement().setAttribute("placeholder", "Search");
    selectionListBox.addChangeHandler(changeEvent -> delegate.onSchemeSelectionChanged());

    // Override DockLayoutPanel Overflow to correctly display ListBox
    selectionPanel.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void showDialog() {
    show();
  }

  @Override
  public void hideDialog() {
    hide();
  }

  @Override
  protected void onHide() {
    resetFilter();
  }

  @Override
  public void renderKeybindings() {
    list.clear();
    list.render(categoriesList, true);
  }

  @Override
  public void setData(Map<String, List<HotKeyItem>> data) {
    categoriesList.clear();
    for (Map.Entry<String, List<HotKeyItem>> elem : data.entrySet()) {
      categoriesList.add(
          new Category<>(
              elem.getKey(), keyBindingsRenderer, elem.getValue(), keyBindingsEventDelegate));
    }
  }

  @Override
  public String getSelectedScheme() {
    return selectionListBox.getValue();
  }

  @Override
  public void setSchemes(String select, List<Scheme> schemes) {
    selectionListBox.clear();
    for (Scheme s : schemes) {
      selectionListBox.addItem(s.getDescription(), s.getSchemeId());
      if (s.getSchemeId().equals(select)) {
        // TODO Might be a better way to select item
        selectionListBox.setSelectedIndex(selectionListBox.getItemCount() - 1);
      }
    }
  }

  @UiHandler("filterInput")
  public void onKeyUp(KeyUpEvent keyUpEvent) {
    String value = filterInput.getText();
    if (!filterInput.getText().equals(filteredValue)) {
      filteredValue = value;
      delegate.onFilterValueChanged(value);
    }
  }

  private void resetFilter() {
    filterInput.setText("");
    filterInput.setFocus(true);
  }
}
