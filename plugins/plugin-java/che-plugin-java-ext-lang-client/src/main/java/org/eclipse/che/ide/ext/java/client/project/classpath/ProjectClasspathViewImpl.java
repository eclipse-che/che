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
package org.eclipse.che.ide.ext.java.client.project.classpath;

import static org.eclipse.che.ide.util.dom.DomUtils.isWidgetOrChildFocused;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.events.KeyboardEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.ClasspathPagePresenter;
import org.eclipse.che.ide.ui.list.CategoriesList;
import org.eclipse.che.ide.ui.list.Category;
import org.eclipse.che.ide.ui.list.CategoryRenderer;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link ProjectClasspathView}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ProjectClasspathViewImpl extends Window implements ProjectClasspathView {

  private static final PropertiesViewImplUiBinder UI_BINDER =
      GWT.create(PropertiesViewImplUiBinder.class);

  private final JavaLocalizationConstant localization;
  private final ProjectClasspathResources commandResources;

  private Button doneButton;

  private ActionDelegate delegate;
  private CategoriesList list;

  @UiField SimplePanel categoriesPanel;
  @UiField SimplePanel contentPanel;

  @Inject
  protected ProjectClasspathViewImpl(
      org.eclipse.che.ide.Resources resources,
      JavaLocalizationConstant localization,
      ProjectClasspathResources commandResources) {
    this.localization = localization;
    this.commandResources = commandResources;

    commandResources.getCss().ensureInjected();

    Widget widget = UI_BINDER.createAndBindUi(this);
    widget.getElement().setId("classpathManagerView");
    widget.getElement().getStyle().setPadding(0, Style.Unit.PX);
    setWidget(widget);
    setTitle(localization.projectClasspathTitle());

    list = new CategoriesList(resources);
    list.addDomHandler(
        event -> {
          switch (event.getNativeKeyCode()) {
            case KeyboardEvent.KeyCode.INSERT:
              break;
            case KeyboardEvent.KeyCode.DELETE:
              break;
          }
        },
        KeyDownEvent.getType());
    categoriesPanel.add(list);

    contentPanel.clear();

    createButtons();
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void showDialog() {
    show(doneButton);
  }

  @Override
  public void close() {
    this.hide();
  }

  @Override
  public AcceptsOneWidget getConfigurationsContainer() {
    return contentPanel;
  }

  @Override
  public void setDoneButtonInFocus() {
    doneButton.setFocus(true);
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    delegate.onEnterClicked();
  }

  @Override
  protected void onHide() {
    delegate.onCloseClicked();
  }

  @Override
  public boolean isDoneButtonInFocus() {
    return isWidgetOrChildFocused(doneButton);
  }

  @Override
  public void setPages(Map<String, Set<ClasspathPagePresenter>> properties) {
    List<Category<?>> categoriesList = new ArrayList<>();
    for (Map.Entry<String, Set<ClasspathPagePresenter>> entry : properties.entrySet()) {
      categoriesList.add(
          new Category<>(
              entry.getKey(),
              projectPropertiesRenderer,
              entry.getValue(),
              projectPropertiesDelegate));
    }

    list.render(categoriesList, true);
  }

  @Override
  public void selectPage(ClasspathPagePresenter property) {
    list.selectElement(property);
  }

  private void createButtons() {

    doneButton =
        addFooterButton(
            localization.buttonDone(),
            "window-edit-configurations-close",
            event -> delegate.onDoneClicked(),
            true);
  }

  private final CategoryRenderer<ClasspathPagePresenter> projectPropertiesRenderer =
      new CategoryRenderer<ClasspathPagePresenter>() {
        @Override
        public void renderElement(Element element, ClasspathPagePresenter data) {
          element.setInnerText(data.getTitle());
        }

        @Override
        public SpanElement renderCategory(Category<ClasspathPagePresenter> category) {
          SpanElement spanElement = Document.get().createSpanElement();
          spanElement.setClassName(commandResources.getCss().categoryHeader());
          spanElement.setInnerText(category.getTitle());
          return spanElement;
        }
      };

  private final Category.CategoryEventDelegate<ClasspathPagePresenter> projectPropertiesDelegate =
      new Category.CategoryEventDelegate<ClasspathPagePresenter>() {
        @Override
        public void onListItemClicked(Element listItemBase, ClasspathPagePresenter itemData) {
          delegate.onConfigurationSelected(itemData);
        }
      };

  interface PropertiesViewImplUiBinder extends UiBinder<Widget, ProjectClasspathViewImpl> {}
}
