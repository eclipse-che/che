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
package org.eclipse.che.ide.ui.list;

import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import java.util.ArrayList;
import java.util.List;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * @author Evgen Vidolob
 * @author Vitaliy Guliy
 */
public class CategoriesList extends Composite {
  /** Defines the attribute used to indicate selection. */
  private static final String SELECTED_ATTRIBUTE = "SELECTED";

  private final Resources resources;
  private final SelectionManager selectionManager;
  private List<CategoryNodeElement> categoryNodeElements;

  private FlowPanel rootPanel;
  private FlowPanel scrollPanel;
  private FlowPanel lockPanel;

  private boolean enabled = true;

  public CategoriesList(Resources resources) {
    this.resources = resources;
    this.categoryNodeElements = new ArrayList<>();

    rootPanel = new FlowPanel();
    rootPanel.setStyleName(resources.defaultCategoriesListCss().rootPanel());
    initWidget(rootPanel);

    scrollPanel = new FlowPanel();
    scrollPanel.setStyleName(resources.defaultCategoriesListCss().scrollPanel());
    rootPanel.add(scrollPanel);

    lockPanel = new FlowPanel();
    lockPanel.setStyleName(resources.defaultCategoriesListCss().lockPanel());
    lockPanel.setVisible(false);
    rootPanel.add(lockPanel);

    selectionManager = new SelectionManager();
  }

  /**
   * Refreshes list of items.
   *
   * <p>
   *
   * <p>This method tries to keep selection.
   *
   * @param categories the categories
   * @param renderChildren if is true - child node will be expanded, otherwise only root node.
   */
  public void render(List<Category<?>> categories, boolean renderChildren) {
    this.categoryNodeElements = new ArrayList<>();
    for (Category category : categories) {
      CategoryNodeElement categoryNodeElement =
          new CategoryNodeElement(category, renderChildren, selectionManager, resources);
      categoryNodeElements.add(categoryNodeElement);
      scrollPanel.add(categoryNodeElement);
    }
  }

  /**
   * Select object in the list.
   *
   * @param element
   * @return
   */
  public boolean selectElement(Object element) {
    if (categoryNodeElements == null || categoryNodeElements.isEmpty()) {
      return false;
    }

    for (CategoryNodeElement category : categoryNodeElements) {
      if (category.containsItem(element)) {
        category.selectItem(element);
        return true;
      }
    }

    return false;
  }

  /** Clears list of items. */
  public void clear() {
    scrollPanel.clear();
    categoryNodeElements = null;
  }

  /**
   * Enables ot disables this widget.
   *
   * @param enabled enabled state
   */
  public void setEnabled(boolean enabled) {
    if (enabled) {
      lockPanel.setVisible(false);
      scrollPanel.getElement().getStyle().clearProperty("opacity");
    } else {
      lockPanel.setVisible(true);
      scrollPanel.getElement().getStyle().setProperty("opacity", "0.5");
    }

    this.enabled = enabled;
  }

  class SelectionManager {

    private Element selectedItem;

    public void selectItem(Element item) {
      if (selectedItem != null) {
        selectedItem.removeAttribute(SELECTED_ATTRIBUTE);
      }
      selectedItem = item;
      selectedItem.setAttribute(SELECTED_ATTRIBUTE, SELECTED_ATTRIBUTE);
    }
  }

  /** Item style selectors for a categories list item. */
  public interface Css extends CssResource {

    String rootPanel();

    String scrollPanel();

    String lockPanel();

    String categoryItem();

    String category();

    String categoryLabel();

    String expandControl();

    String categoryHeader();

    String expandedImage();

    String itemContainer();

    String headerText();
  }

  public interface Resources extends ClientBundle {
    @Source({
      "CategoriesList.css",
      "org/eclipse/che/ide/ui/constants.css",
      "org/eclipse/che/ide/api/ui/style.css"
    })
    Css defaultCategoriesListCss();

    @Source("arrowExpansionIcon.svg")
    SVGResource arrowExpansionImage();
  }
}
