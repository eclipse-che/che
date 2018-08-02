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
package org.eclipse.che.plugin.debugger.ide.configuration;

import static org.eclipse.che.ide.util.dom.DomUtils.isWidgetOrChildFocused;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import elemental.events.KeyboardEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ui.list.CategoriesList;
import org.eclipse.che.ide.ui.list.Category;
import org.eclipse.che.ide.ui.list.CategoryRenderer;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * The implementation of {@link EditDebugConfigurationsView}.
 *
 * @author Artem Zatsarynnyi
 */
public class EditDebugConfigurationsViewImpl extends Window implements EditDebugConfigurationsView {

  private static final EditDebugConfigurationsViewImplUiBinder UI_BINDER =
      GWT.create(EditDebugConfigurationsViewImplUiBinder.class);

  private final EditConfigurationsResources editConfigurationsResources;
  private final IconRegistry iconRegistry;
  private final CoreLocalizationConstant coreLocale;
  private final Label hintLabel;

  private final Category.CategoryEventDelegate<DebugConfiguration> categoryEventDelegate;
  private final CategoryRenderer<DebugConfiguration> categoryRenderer;

  @UiField(provided = true)
  DebuggerLocalizationConstant locale;

  @UiField SimplePanel categoriesPanel;
  @UiField TextBox filterInputField;
  @UiField TextBox configurationName;
  @UiField SimplePanel contentPanel;
  @UiField FlowPanel namePanel;
  @UiField FlowPanel overFooter;

  private Button cancelButton;
  private Button saveButton;
  private Button closeButton;
  private Button debugButton;
  private ActionDelegate delegate;
  private CategoriesList list;
  private Map<DebugConfigurationType, List<DebugConfiguration>> categories;
  private DebugConfiguration selectedConfiguration;
  private DebugConfigurationType selectedType;
  private String filterTextValue;

  @Inject
  protected EditDebugConfigurationsViewImpl(
      org.eclipse.che.ide.Resources resources,
      final EditConfigurationsResources editConfigurationsResources,
      DebuggerLocalizationConstant locale,
      CoreLocalizationConstant coreLocale,
      IconRegistry iconRegistry) {
    this.editConfigurationsResources = editConfigurationsResources;
    this.locale = locale;
    this.coreLocale = coreLocale;
    this.iconRegistry = iconRegistry;

    categories = new HashMap<>();

    editConfigurationsResources.getCss().ensureInjected();
    Widget widget = UI_BINDER.createAndBindUi(this);
    widget.getElement().setId("editDebugConfigurationsView");
    widget.getElement().getStyle().setPadding(0, Style.Unit.PX);
    setWidget(widget);
    setTitle(locale.editConfigurationsViewTitle());

    hintLabel = new Label(locale.editConfigurationsViewHint());
    hintLabel.addStyleName(editConfigurationsResources.getCss().hintLabel());

    filterInputField
        .getElement()
        .setAttribute("placeholder", locale.editConfigurationsViewPlaceholder());
    filterInputField
        .getElement()
        .addClassName(editConfigurationsResources.getCss().filterPlaceholder());

    list = new CategoriesList(resources);
    list.addDomHandler(
        event -> {
          switch (event.getNativeKeyCode()) {
            case KeyboardEvent.KeyCode.INSERT:
              delegate.onAddClicked();
              resetFilter();
              break;
            case KeyboardEvent.KeyCode.DELETE:
              delegate.onRemoveClicked(selectedConfiguration);
              break;
          }
        },
        KeyDownEvent.getType());
    categoriesPanel.add(list);

    categoryEventDelegate =
        (listItemBase, itemData) -> {
          selectedType = itemData.getType();
          setSelectedConfiguration(itemData);
        };

    categoryRenderer =
        new CategoryRenderer<DebugConfiguration>() {
          @Override
          public void renderElement(Element element, DebugConfiguration data) {
            UIObject.ensureDebugId(element, "debug-configuration-type-" + data.getType().getId());
            element.addClassName(editConfigurationsResources.getCss().categorySubElementHeader());
            element.setInnerText(data.getName().trim().isEmpty() ? "<none>" : data.getName());
            element.appendChild(renderSubElementButtons());
          }

          @Override
          public SpanElement renderCategory(Category<DebugConfiguration> category) {
            return renderCategoryHeader(category.getTitle());
          }
        };

    namePanel.setVisible(false);
    contentPanel.clear();

    createButtons();
    resetFilter();
  }

  private SpanElement renderSubElementButtons() {
    final SpanElement categorySubElement = Document.get().createSpanElement();
    categorySubElement.setClassName(editConfigurationsResources.getCss().buttonArea());

    final SpanElement removeConfigurationButtonElement = Document.get().createSpanElement();
    categorySubElement.appendChild(removeConfigurationButtonElement);
    removeConfigurationButtonElement.appendChild(
        this.editConfigurationsResources.removeConfigurationButton().getSvg().getElement());
    Event.sinkEvents(removeConfigurationButtonElement, Event.ONCLICK);
    Event.setEventListener(
        removeConfigurationButtonElement,
        event -> {
          if (Event.ONCLICK == event.getTypeInt()) {
            event.stopPropagation();
            setSelectedConfiguration(selectedConfiguration);
            delegate.onRemoveClicked(selectedConfiguration);
          }
        });

    final SpanElement duplicateConfigurationButton = Document.get().createSpanElement();
    categorySubElement.appendChild(duplicateConfigurationButton);
    duplicateConfigurationButton.appendChild(
        this.editConfigurationsResources.duplicateConfigurationButton().getSvg().getElement());
    Event.sinkEvents(duplicateConfigurationButton, Event.ONCLICK);
    Event.setEventListener(
        duplicateConfigurationButton,
        event -> {
          if (Event.ONCLICK == event.getTypeInt()) {
            event.stopPropagation();
            delegate.onDuplicateClicked();
          }
        });

    return categorySubElement;
  }

  private SpanElement renderCategoryHeader(final String categoryTitle) {
    SpanElement categoryHeaderElement = Document.get().createSpanElement();
    categoryHeaderElement.setClassName(editConfigurationsResources.getCss().categoryHeader());

    SpanElement iconElement = Document.get().createSpanElement();
    categoryHeaderElement.appendChild(iconElement);

    SpanElement textElement = Document.get().createSpanElement();
    categoryHeaderElement.appendChild(textElement);
    DebugConfigurationType currentDebugConfigurationType = getTypeById(categoryTitle);
    textElement.setInnerText(
        currentDebugConfigurationType != null
            ? currentDebugConfigurationType.getDisplayName()
            : categoryTitle);

    SpanElement buttonElement = Document.get().createSpanElement();
    buttonElement.appendChild(
        editConfigurationsResources.addConfigurationButton().getSvg().getElement());
    categoryHeaderElement.appendChild(buttonElement);

    Event.sinkEvents(buttonElement, Event.ONCLICK);
    Event.setEventListener(
        buttonElement,
        event -> {
          if (Event.ONCLICK == event.getTypeInt()) {
            event.stopPropagation();
            namePanel.setVisible(true);
            selectedType = getTypeById(categoryTitle);
            delegate.onAddClicked();
            resetFilter();
          }
        });

    Icon icon = iconRegistry.getIconIfExist(categoryTitle + ".debug.configuration.type.icon");
    if (icon != null) {
      final SVGImage iconSVG = icon.getSVGImage();
      if (iconSVG != null) {
        iconElement.appendChild(iconSVG.getElement());
        return categoryHeaderElement;
      }
    }

    return categoryHeaderElement;
  }

  private DebugConfigurationType getTypeById(String typeId) {
    for (DebugConfigurationType type : categories.keySet()) {
      if (type.getId().equals(typeId)) {
        return type;
      }
    }
    return null;
  }

  private void resetFilter() {
    filterInputField.setText(""); // reset filter
    filterTextValue = "";
  }

  private void renderCategoriesList(
      Map<DebugConfigurationType, List<DebugConfiguration>> categories) {
    if (categories == null) {
      return;
    }

    final List<Category<?>> categoriesList = new ArrayList<>();

    for (DebugConfigurationType type : categories.keySet()) {
      List<DebugConfiguration> configurations = new ArrayList<>();
      if (filterTextValue.isEmpty()) {
        configurations = categories.get(type);
      } else { // filtering List
        for (final DebugConfiguration configuration : categories.get(type)) {
          if (configuration.getName().contains(filterTextValue)) {
            configurations.add(configuration);
          }
        }
      }
      Category<DebugConfiguration> category =
          new Category<>(type.getId(), categoryRenderer, configurations, categoryEventDelegate);
      categoriesList.add(category);
    }

    list.clear();
    list.render(categoriesList, true);
    if (selectedConfiguration != null) {
      list.selectElement(selectedConfiguration);
      if (filterTextValue.isEmpty()) {
        selectText(configurationName.getElement());
      }
    } else {
      contentPanel.clear();
      contentPanel.add(hintLabel);
      namePanel.setVisible(false);
    }
  }

  @Override
  public void selectNextItem() {
    final DebugConfiguration nextItem;
    final List<DebugConfiguration> configurations = categories.get(selectedConfiguration.getType());

    int selectPosition = configurations.indexOf(selectedConfiguration);
    if (configurations.size() < 2 || selectPosition == -1) {
      nextItem = null;
    } else {
      if (selectPosition > 0) {
        selectPosition--;
      } else {
        selectPosition++;
      }
      nextItem = configurations.get(selectPosition);
    }
    list.selectElement(nextItem);
    selectedConfiguration = nextItem;
  }

  @Override
  public void setData(Map<DebugConfigurationType, List<DebugConfiguration>> categories) {
    this.categories = categories;
    renderCategoriesList(categories);
  }

  private void createButtons() {
    saveButton =
        addFooterButton(
            coreLocale.save(),
            "window-edit-debug-configurations-save",
            event -> delegate.onSaveClicked(),
            true);
    cancelButton =
        addFooterButton(
            coreLocale.cancel(),
            "window-edit-debug-configurations-cancel",
            event -> delegate.onCancelClicked());
    debugButton =
        addFooterButton(
            coreLocale.debug(),
            "window-edit-debug-configurations-debug",
            event -> delegate.onDebugClicked());
    closeButton =
        addFooterButton(
            coreLocale.close(),
            "window-edit-debug-configurations-close",
            event -> delegate.onCloseClicked());

    closeButton.addDomHandler(
        blurEvent -> {
          // set default focus
          selectText(filterInputField.getElement());
        },
        BlurEvent.getType());
  }

  private native void selectText(Element inputElement) /*-{
        inputElement.focus();
        inputElement.setSelectionRange(0, inputElement.value.length);
    }-*/;

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void showDialog() {
    show();
  }

  @Override
  protected void onShow() {
    configurationName.setText("");
  }

  @Override
  public void close() {
    this.hide();
  }

  @Override
  public AcceptsOneWidget getDebugConfigurationPageContainer() {
    return contentPanel;
  }

  @Override
  public void clearDebugConfigurationPageContainer() {
    contentPanel.clear();
  }

  @Override
  public String getConfigurationName() {
    return configurationName.getText().trim();
  }

  @Override
  public void setConfigurationName(String name) {
    configurationName.setText(name);
  }

  @Override
  public void setCancelButtonState(boolean enabled) {
    cancelButton.setEnabled(enabled);
  }

  @Override
  public void setSaveButtonState(boolean enabled) {
    saveButton.setEnabled(enabled);
  }

  @Override
  public void setDebugButtonState(boolean enabled) {
    debugButton.setEnabled(enabled);
  }

  @Override
  public void setFilterState(boolean enabled) {
    filterInputField.setEnabled(enabled);
  }

  @Nullable
  @Override
  public DebugConfigurationType getSelectedConfigurationType() {
    return selectedType;
  }

  @Nullable
  @Override
  public DebugConfiguration getSelectedConfiguration() {
    return selectedConfiguration;
  }

  @Override
  public void setSelectedConfiguration(DebugConfiguration selectConfiguration) {
    this.selectedConfiguration = selectConfiguration;
    if (selectConfiguration != null) {
      namePanel.setVisible(true);
      delegate.onConfigurationSelected(selectConfiguration);
    }
  }

  @Override
  public void focusCloseButton() {
    closeButton.setFocus(true);
  }

  @UiHandler("configurationName")
  public void onNameKeyUp(KeyUpEvent event) {
    delegate.onNameChanged();
  }

  @UiHandler("filterInputField")
  public void onFilterKeyUp(KeyUpEvent event) {
    if (!filterTextValue.equals(filterInputField.getText())) {
      filterTextValue = filterInputField.getText();
      renderCategoriesList(categories);
    }
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    delegate.onEnterPressed();
  }

  @Override
  protected void onHide() {
    setSelectedConfiguration(selectedConfiguration);
  }

  @Override
  public boolean isCancelButtonFocused() {
    return isWidgetOrChildFocused(cancelButton);
  }

  @Override
  public boolean isCloseButtonFocused() {
    return isWidgetOrChildFocused(closeButton);
  }

  interface EditDebugConfigurationsViewImplUiBinder
      extends UiBinder<Widget, EditDebugConfigurationsViewImpl> {}
}
