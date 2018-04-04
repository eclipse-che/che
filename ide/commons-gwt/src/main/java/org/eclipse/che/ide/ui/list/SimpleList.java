// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.ui.list;

import com.google.gwt.dom.client.Node;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.MouseEvent;
import elemental.js.dom.JsElement;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.mvp.UiComponent;
import org.eclipse.che.ide.ui.ElementView;
import org.eclipse.che.ide.util.CssUtils;
import org.eclipse.che.ide.util.dom.DomUtils;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.loging.Log;

/** A simple list widget for displaying flat collections of things. */
// TODO: When we hit a place where a component wants to ditch all of
// the default simple list styles, figure out a way to make that easy.
public class SimpleList<M> extends UiComponent<SimpleList.View> implements IsWidget {

  /** Create using the default CSS. */
  public static <M> SimpleList<M> create(
      View view,
      Resources res,
      ListItemRenderer<M> itemRenderer,
      ListEventDelegate<M> eventDelegate) {
    return new SimpleList<M>(
        view, view, view, res.defaultSimpleListCss(), itemRenderer, eventDelegate);
  }

  /** Create with custom CSS. */
  public static <M> SimpleList<M> create(
      View view, Css css, ListItemRenderer<M> itemRenderer, ListEventDelegate<M> eventDelegate) {
    return new SimpleList<M>(view, view, view, css, itemRenderer, eventDelegate);
  }

  /**
   * Create and configure control instance.
   *
   * <p>
   *
   * <p>Use this method when either only part of control is scrollable ({@code view != container})
   * or elements are stored in table ({@code container != itemHolder}) or both.
   *
   * @param view element that receives control decorations (shadow, etc.)
   * @param container element whose content is scrolled
   * @param itemHolder element to add items to
   */
  public static <M> SimpleList<M> create(
      View view,
      Element container,
      Element itemHolder,
      Css css,
      ListItemRenderer<M> itemRenderer,
      ListEventDelegate<M> eventDelegate) {
    return new SimpleList<M>(view, container, itemHolder, css, itemRenderer, eventDelegate);
  }

  /**
   * Called each time we render an item in the list. Provides an opportunity for
   * implementors/clients to customize the DOM structure of each list item.
   */
  public abstract static class ListItemRenderer<M> {
    public abstract void render(Element listItemBase, M itemData);

    /**
     * A factory method for the outermost element used by a list item.
     *
     * <p>The default implementation returns a div element.
     */
    public Element createElement() {
      return Elements.createDivElement();
    }
  }

  /** Receives events fired on items in the list. */
  public interface ListEventDelegate<M> {
    default void onListItemClicked(Element listItemBase, M itemData) {}

    default void onListItemDoubleClicked(Element listItemBase, M itemData) {}

    default void onListItemContextMenu(int clientX, int clientY, M itemData) {}
  }

  /** Item style selectors for a simple list item. */
  public interface Css extends CssResource {
    int menuListBorderPx();

    String listItem();

    String listBase();

    String listContainer();
  }

  public interface Resources extends ClientBundle {
    @Source({
      "SimpleList.css",
      "org/eclipse/che/ide/ui/constants.css",
      "org/eclipse/che/ide/api/ui/style.css"
    })
    Css defaultSimpleListCss();
  }

  /** Overlay type representing the base element of the SimpleList. */
  public static class View extends ElementView<Void> {
    protected View() {}
  }

  /** A javascript overlay object which ties a list item's DOM element to its associated data. */
  static final class ListItem<M> extends JsElement {
    /**
     * Creates a new ListItem overlay object by creating a div element, assigning it the listItem
     * css class, and associating it to its data.
     */
    public static <M> ListItem<M> create(ListItemRenderer<M> factory, Css css, M data) {
      Element element = factory.createElement();
      Elements.addClassName(css.listItem(), element);

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

    /**
     * Reuses this elements container by clearing it's contents and updating its data. This allows
     * it to be sent back to the renderer.
     *
     * @param className The css class to set to the container. All other css classes are cleared.
     */
    public final native void reuseContainerElement(M data, String className) /*-{
            this.className = className;
            this.innerHTML = "";
            this.__data = data;
        }-*/;
  }

  /**
   * A model which maintains the internal state of the list including selection and DOM elements.
   */
  public class Model<M> implements HasSelection<M> {

    private static final int NO_SELECTION = -1;

    /** Defines the attribute used to indicate selection. */
    private static final String SELECTED_ATTRIBUTE = "SELECTED";

    private final ListEventDelegate<M> delegate;

    private final List<ListItem<M>> listItems = new ArrayList<>();

    private int selectedIndex;

    /** Creates a new model for use by SimpleList. The provided delegate should not be null. */
    public Model(ListEventDelegate<M> delegate) {
      this.delegate = delegate;
      // set the initially selected item
      selectedIndex = 0;
    }

    @Override
    public int getSelectedIndex() {
      return selectedIndex;
    }

    @Override
    public M getSelectedItem() {
      ListItem<M> selectedListItem = getSelectedListItem();
      return selectedListItem != null ? selectedListItem.getData() : null;
    }

    /** Returns the currently selected list element or null. */
    private ListItem<M> getSelectedListItem() {
      if (selectedIndex >= 0 && selectedIndex < listItems.size()) {
        return listItems.get(selectedIndex);
      }
      return null;
    }

    @Override
    public boolean selectNext() {
      return setSelectedItem(selectedIndex + 1);
    }

    @Override
    public boolean selectPrevious() {
      return setSelectedItem(selectedIndex - 1);
    }

    @Override
    public boolean selectNextPage() {
      return setSelectedItem(Math.min(selectedIndex + getPageSize(), size() - 1));
    }

    @Override
    public boolean selectPreviousPage() {
      return setSelectedItem(Math.max(0, selectedIndex - getPageSize()));
    }

    private int getPageSize() {
      int indexAboveViewport = findIndexOfFirstNotInViewport(selectedIndex, false);
      int indexBelowViewport = findIndexOfFirstNotInViewport(selectedIndex, true);

      // A minimum size of 1
      return Math.max(1, indexBelowViewport - indexAboveViewport - 1);
    }

    /**
     * Returns the index of the first item that is not fully in the viewport. If all items are, it
     * will return the last item in the given direction.
     */
    private int findIndexOfFirstNotInViewport(int beginIndex, boolean forward) {
      final int deltaIndex = forward ? 1 : -1;
      int i = beginIndex;
      for (; i >= 0 && i < size(); i += deltaIndex) {
        if (!DomUtils.isFullyInScrollViewport(container, listItems.get(i))) {
          return i;
        }
      }

      return i + -deltaIndex;
    }

    @Override
    public void handleClick() {
      ListItem<M> item = getSelectedListItem();
      if (item != null) {
        delegate.onListItemClicked(item, item.getData());
      }
    }

    @Override
    public void clearSelection() {
      maybeRemoveSelectionFromElement();
      selectedIndex = NO_SELECTION;
    }

    @Override
    public int size() {
      return listItems.size();
    }

    @Override
    public boolean setSelectedItem(int index) {
      if (index >= 0 && index < listItems.size()) {
        maybeRemoveSelectionFromElement();
        selectedIndex = index;
        getSelectedListItem().setAttribute(SELECTED_ATTRIBUTE, SELECTED_ATTRIBUTE);
        ensureSelectedIsVisible();
        return true;
      }

      return false;
    }

    @Override
    public boolean setSelectedItem(M item) {
      int index = -1;
      for (int i = 0; i < listItems.size(); i++) {
        if (listItems.get(i).getData().equals(item)) {
          index = i;
          break;
        }
      }
      return setSelectedItem(index);
    }

    private void ensureSelectedIsVisible() {
      DomUtils.ensureScrolledTo(container, model.getSelectedListItem());
    }

    /** Removes selection from the currently selected element if it exists. */
    private void maybeRemoveSelectionFromElement() {
      ListItem<M> element = getSelectedListItem();
      if (element != null) {
        element.removeAttribute(SELECTED_ATTRIBUTE);
      }
    }
  }

  private final ListEventDelegate<M> eventDelegate;

  private final ListItemRenderer<M> itemRenderer;

  private final Css css;

  private final Model<M> model;

  private final Element container;

  private final Element itemHolder;

  private HTML widget;

  SimpleList(
      View view,
      Element container,
      Element itemHolder,
      Css css,
      ListItemRenderer<M> itemRenderer,
      ListEventDelegate<M> eventDelegate) {
    super(view);

    this.css = css;
    this.model = new Model<M>(eventDelegate);
    this.itemRenderer = itemRenderer;
    this.eventDelegate = eventDelegate;
    this.itemHolder = itemHolder;
    this.container = container;

    Elements.addClassName(css.listBase(), view);
    Elements.addClassName(css.listContainer(), container);
    attachEventHandlers();
  }

  /** Returns the current number of items in the simple list. */
  public int size() {
    return model.listItems.size();
  }

  public void render(List<M> items) {
    M selectedItem = model.getSelectedItem();
    model.clearSelection();

    itemHolder.setInnerHTML("");
    model.listItems.clear();

    for (int i = 0; i < items.size(); i++) {
      ListItem<M> elem = ListItem.create(itemRenderer, css, items.get(i));
      CssUtils.setUserSelect(elem, false);
      model.listItems.add(elem);
      itemRenderer.render(elem, elem.getData());

      itemHolder.appendChild(elem);
    }

    model.setSelectedItem(selectedItem);
  }

  public HasSelection<M> getSelectionModel() {
    return model;
  }

  /** @return true if the list or any one of its element's currently have focus. */
  public boolean hasFocus() {
    return DomUtils.isElementOrChildFocused(container);
  }

  private void attachEventHandlers() {
    getView()
        .addEventListener(
            Event.CLICK,
            new EventListener() {
              @Override
              public void handleEvent(Event evt) {
                Element listItemElem =
                    CssUtils.getAncestorOrSelfWithClassName(
                        (Element) evt.getTarget(), css.listItem());

                if (listItemElem == null) {
                  Log.warn(
                      SimpleList.class,
                      "Unable to find an ancestor that was a list item for a click on: ",
                      evt.getTarget());
                  return;
                }

                ListItem<M> listItem = ListItem.cast(listItemElem);
                eventDelegate.onListItemClicked(listItem, listItem.getData());
              }
            },
            false);

    getView()
        .addEventListener(
            Event.DBLCLICK,
            new EventListener() {
              @Override
              public void handleEvent(Event evt) {
                Element listItemElem =
                    CssUtils.getAncestorOrSelfWithClassName(
                        (Element) evt.getTarget(), css.listItem());

                if (listItemElem == null) {
                  Log.warn(
                      SimpleList.class,
                      "Unable to find an ancestor that was a list item for a click on: ",
                      evt.getTarget());
                  return;
                }

                ListItem<M> listItem = ListItem.cast(listItemElem);
                eventDelegate.onListItemDoubleClicked(listItem, listItem.getData());
              }
            },
            false);

    getView()
        .addEventListener(
            Event.CONTEXTMENU,
            new EventListener() {
              @Override
              public void handleEvent(Event evt) {
                MouseEvent mouseEvt = (MouseEvent) evt;
                Element listItemElem =
                    CssUtils.getAncestorOrSelfWithClassName(
                        (Element) evt.getTarget(), css.listItem());

                if (listItemElem == null) {
                  Log.warn(
                      SimpleList.class,
                      "Unable to find an ancestor that was a list item for a click on: ",
                      evt.getTarget());
                  return;
                }

                ListItem<M> listItem = ListItem.cast(listItemElem);
                eventDelegate.onListItemContextMenu(
                    mouseEvt.getClientX(), mouseEvt.getClientY(), listItem.getData());
              }
            },
            false);
  }

  public M get(int i) {
    return model.listItems.get(i).getData();
  }

  /** {@inheritDoc} */
  @Override
  public Widget asWidget() {
    if (widget == null) {
      widget = new HTML();
      widget.getElement().appendChild((Node) getView().getElement());
    }

    return widget;
  }
}
