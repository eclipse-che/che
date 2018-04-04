/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.projectimport.wizard.mainpage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.ui.list.CategoriesList;
import org.eclipse.che.ide.ui.list.Category;
import org.eclipse.che.ide.ui.list.CategoryRenderer;

/**
 * UI implementation for {@link MainPageView}.
 *
 * @author Ann Shumilova
 */
public class MainPageViewImpl implements MainPageView {

  private static MainPageViewImplUiBinder uiBinder = GWT.create(MainPageViewImplUiBinder.class);
  private final DockLayoutPanel rootElement;
  private final CategoryRenderer<ProjectImporterDescriptor> projectImporterRenderer =
      new CategoryRenderer<ProjectImporterDescriptor>() {
        @Override
        public void renderElement(Element element, ProjectImporterDescriptor data) {
          element.setInnerText(data.getId().toUpperCase());
        }

        @Override
        public SpanElement renderCategory(Category<ProjectImporterDescriptor> category) {
          SpanElement textElement = Document.get().createSpanElement();
          textElement.setClassName(resources.defaultCategoriesListCss().headerText());
          textElement.setInnerText(category.getTitle());
          return textElement;
        }
      };

  @UiField Style style;
  @UiField SimplePanel importerPanel;
  @UiField SimplePanel categoriesPanel;
  @UiField HTMLPanel descriptionArea;

  @UiField(provided = true)
  Resources resources;

  private CategoriesList list;
  private ActionDelegate delegate;
  private final Category.CategoryEventDelegate<ProjectImporterDescriptor> projectImporterDelegate =
      new Category.CategoryEventDelegate<ProjectImporterDescriptor>() {
        @Override
        public void onListItemClicked(Element listItemBase, ProjectImporterDescriptor itemData) {
          delegate.projectImporterSelected(itemData);
        }
      };

  @Inject
  public MainPageViewImpl(Resources resources) {
    this.resources = resources;
    rootElement = uiBinder.createAndBindUi(this);
  }

  @Override
  public void reset() {
    categoriesPanel.clear();
    list = new CategoriesList(resources);
    categoriesPanel.add(list);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return rootElement;
  }

  /** {@inheritDoc} */
  @Override
  public void setImporters(Map<String, Set<ProjectImporterDescriptor>> categories) {
    List<Category<?>> categoriesList = new ArrayList<>();
    for (Entry<String, Set<ProjectImporterDescriptor>> entry : categories.entrySet()) {
      categoriesList.add(
          new Category<>(
              entry.getKey(), projectImporterRenderer, entry.getValue(), projectImporterDelegate));
    }

    list.render(categoriesList, true);
  }

  @Override
  public AcceptsOneWidget getImporterPanel() {
    return importerPanel;
  }

  /** {@inheritDoc} */
  @Override
  public void selectImporter(ProjectImporterDescriptor importer) {
    list.selectElement(importer);
  }

  @Override
  public void setImporterDescription(@NotNull String text) {
    descriptionArea.getElement().setInnerText(text);
  }

  interface MainPageViewImplUiBinder extends UiBinder<DockLayoutPanel, MainPageViewImpl> {}

  interface Style extends CssResource {
    String mainPanel();

    String leftPart();

    String rightPart();

    String categories();
  }
}
