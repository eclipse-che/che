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
package org.eclipse.che.ide.ui.list;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.UIObject;
import java.util.HashMap;
import org.eclipse.che.ide.util.AnimationController;

/**
 * Overlay type for the base element for a Category Node in the list. Nodes that have children, but
 * that have never been expanded (nodes render lazily on expansion), have an empty DIV element.
 *
 * <p>
 *
 * <pre>
 *
 * <li class="treeNode">
 *   <div class="treeNodeBody">
 *     <span class="treeNodeLabel"></span><div class="expandControl"></div>
 *   </div>
 *   <ul class="childrenContainer">
 *   </ul>
 * </li>
 *
 * </pre>
 *
 * @author Evgen Vidolob
 */
public class CategoryNodeElement extends FlowPanel {
  private final Category category;
  private CategoriesList.SelectionManager selectionManager;
  private final FocusPanel container;
  private final AnimationController animator;
  private CategoriesList.Resources resources;
  private boolean expanded;
  private final DivElement expandControl;
  private HashMap<Object, Element> elementsMap;
  private Element selectedElement;

  @SuppressWarnings("unchecked")
  CategoryNodeElement(
      final Category category,
      boolean renderChildren,
      CategoriesList.SelectionManager selectionManager,
      CategoriesList.Resources resources) {
    this.category = category;
    this.selectionManager = selectionManager;
    CategoryRenderer renderer = category.getRenderer();
    this.resources = resources;
    setStyleName(resources.defaultCategoriesListCss().category());
    FlowPanel header = new FlowPanel();
    header.sinkEvents(Event.ONCLICK);
    header.addDomHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            if (category.getData().isEmpty()) {
              return;
            }
            expandOrCollapse();
          }
        },
        ClickEvent.getType());
    header.setStyleName(resources.defaultCategoriesListCss().categoryHeader());
    SpanElement label = Document.get().createSpanElement();
    label.setClassName(resources.defaultCategoriesListCss().categoryLabel());

    label.appendChild(renderer.renderCategory(category));

    header.getElement().appendChild(label);
    header.ensureDebugId("categoryHeader-" + category.getTitle());

    expandControl = Document.get().createDivElement();
    expandControl.appendChild(resources.arrowExpansionImage().getSvg().getElement());
    expandControl.setClassName(resources.defaultCategoriesListCss().expandControl());
    header.getElement().appendChild(expandControl);
    container = new FocusPanel();
    container.setTabIndex(1);
    container.setStyleName(resources.defaultCategoriesListCss().itemContainer());
    container.sinkEvents(Event.ONCLICK);
    container.addDomHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            selectElement(Element.as(event.getNativeEvent().getEventTarget()));
          }
        },
        ClickEvent.getType());
    container.sinkEvents(Event.ONKEYDOWN);
    container.addHandler(
        new KeyDownHandler() {
          @Override
          public void onKeyDown(KeyDownEvent keyDownEvent) {

            if (selectedElement == null) {
              return;
            }

            Element element = null;

            if (keyDownEvent.isDownArrow()) {
              element = selectedElement.getNextSiblingElement();
              if (element == null) {
                return;
              }
            }

            if (keyDownEvent.isUpArrow()) {
              element = selectedElement.getPreviousSiblingElement();
              if (element.getClassName().equals("")) {
                return;
              }
            }

            if (keyDownEvent.isUpArrow() || keyDownEvent.isDownArrow()) {
              keyDownEvent.preventDefault();
              element.scrollIntoView();
              selectElement(element);
            }
          }
        },
        KeyDownEvent.getType());
    add(header);
    add(container);
    animator = new AnimationController.Builder().setCollapse(false).setFade(false).build();
    expanded = true;
    renderChildren();
    if (renderChildren) {
      expandControl.addClassName(resources.defaultCategoriesListCss().expandedImage());
    } else {
      expandOrCollapse();
    }
  }

  @SuppressWarnings("unchecked")
  private void selectElement(Element eventTarget) {
    selectedElement = eventTarget;
    selectionManager.selectItem(eventTarget);
    category
        .getEventDelegate()
        .onListItemClicked(eventTarget, ListItem.cast(eventTarget).getData());
  }

  private void expandOrCollapse() {
    if (!expanded) {
      expanded = true;
      if (container.getElement().getChildCount() == 0) {
        renderChildren();
      }
      animator.show((elemental.dom.Element) container.getElement());
      expandControl.addClassName(resources.defaultCategoriesListCss().expandedImage());
    } else {
      animator.hide((elemental.dom.Element) container.getElement());
      expandControl.removeClassName(resources.defaultCategoriesListCss().expandedImage());
      expanded = false;
    }
  }

  @SuppressWarnings("unchecked")
  private void renderChildren() {
    elementsMap = new HashMap<>();
    CategoryRenderer categoryRenderer = category.getRenderer();
    for (Object o : category.getData()) {
      ListItem<?> element =
          ListItem.create(categoryRenderer, resources.defaultCategoriesListCss(), o);
      categoryRenderer.renderElement(element, o);
      elementsMap.put(o, element);
      if (element.getId().isEmpty()) {
        UIObject.ensureDebugId(element, "projectWizard-" + element.getInnerText());
      }
      container.getElement().appendChild(element);
    }
    if (elementsMap.isEmpty()) {
      expandControl.getStyle().setVisibility(Style.Visibility.HIDDEN);
    } else {
      expandControl.getStyle().setVisibility(Style.Visibility.VISIBLE);
    }
  }

  /**
   * Checks whether the category contains the pointed item.
   *
   * @param item item to find
   * @return boolean <code>true</code> if contains
   */
  public boolean containsItem(Object item) {
    if (elementsMap == null || elementsMap.isEmpty()) {
      return false;
    }
    return elementsMap.containsKey(item);
  }

  /**
   * Selects the item in the category list.
   *
   * @param item
   */
  public void selectItem(Object item) {
    if (elementsMap == null || elementsMap.isEmpty()) {
      return;
    }

    if (elementsMap.containsKey(item)) {
      selectElement(elementsMap.get(item));
    }
  }

  /** A javascript overlay object which ties a list item's DOM element to its associated data. */
  static final class ListItem<M> extends Element {
    /**
     * Creates a new ListItem overlay object by creating a div element, assigning it the listItem
     * css class, and associating it to its data.
     */
    public static <M> ListItem<M> create(
        CategoryRenderer<M> factory, CategoriesList.Css css, M data) {
      Element element = factory.createElement();
      element.addClassName(css.categoryItem());

      ListItem<M> item = ListItem.cast(element);
      item.setData(data);
      return item;
    }

    /**
     * Casts an element to its ListItem representation. This is an unchecked cast so we extract it
     * into this static factory method so we don't have to suppress warnings all over the place.
     */
    @SuppressWarnings("unchecked")
    public static <M> ListItem<M> cast(Element element) {
      return (ListItem<M>) element;
    }

    protected ListItem() {
      // Unused constructor
    }

    public final native M getData() /*-{
            return this.__data;
        }-*/;

    public final native void setData(M data) /*-{
            this.__data = data;
        }-*/;
  }
}
