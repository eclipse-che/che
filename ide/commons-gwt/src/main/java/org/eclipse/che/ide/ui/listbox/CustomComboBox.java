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
package org.eclipse.che.ide.ui.listbox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * ComboBox widget.
 *
 * @author Oleksii Orel
 * @author Anatoliy Bazko
 */
public class CustomComboBox extends FocusWidget implements HasChangeHandlers {

  private static final CustomListBoxResources RESOURCES = GWT.create(CustomListBoxResources.class);

  static {
    RESOURCES.getCSS().ensureInjected();
  }

  private final InputElement currentInputElement = Document.get().createTextInputElement();
  private final FlowPanel optionsPanel = new FlowPanel();
  private final String optionsGroupName = "comboBox-" + Document.get().createUniqueId();

  private int dropDownSize = 6;
  private int selectedIndex = -1;
  private int defaultSelectedIndex = -1;
  private boolean isActive = false;

  public static CustomComboBox wrap(Element element) {
    // Assert that the element is attached.
    assert Document.get().getBody().isOrHasChild(element);

    CustomComboBox customComboBox = new CustomComboBox(element);

    // Mark it attached and remember it for cleanup.
    customComboBox.onAttach();
    RootPanel.detachOnWindowClose(customComboBox);

    return customComboBox;
  }

  /** Creates an empty custom list box. */
  public CustomComboBox() {
    super(Document.get().createDivElement());

    Element comboBoxElement = getElement();

    comboBoxElement.appendChild(currentInputElement);
    comboBoxElement.appendChild(optionsPanel.getElement());
    comboBoxElement.appendChild(RESOURCES.arrow().getSvg().getElement());

    optionsPanel.setVisible(false);
    optionsPanel.ensureDebugId("custom-combobox-content-panel");

    addDomHandler(
        new BlurHandler() {
          @Override
          public void onBlur(BlurEvent event) {
            optionsPanel.setVisible(false);
            isActive = false;
          }
        },
        BlurEvent.getType());

    addDomHandler(
        new MouseDownHandler() {
          @Override
          public void onMouseDown(MouseDownEvent event) {
            if (!currentInputElement.isDisabled()) {
              // Update isActive state. It actually when we lose onBlur event in the parent widget.
              isActive = isActive(getElement());
              if (!isActive) {
                optionsPanel.setVisible(true);
              }
            }
          }
        },
        MouseDownEvent.getType());

    addDomHandler(
        new MouseUpHandler() {
          @Override
          public void onMouseUp(MouseUpEvent event) {
            if (!currentInputElement.isDisabled()) {
              if (isActive) {
                optionsPanel.setVisible(!optionsPanel.isVisible());
              } else {
                isActive = true;
              }
            }
          }
        },
        MouseUpEvent.getType());

    addDomHandler(
        new KeyDownHandler() {
          @Override
          public void onKeyDown(KeyDownEvent event) {
            resetSelectedIndex();
          }
        },
        KeyDownEvent.getType());

    addChangeHandler(
        new ChangeHandler() {
          @Override
          public void onChange(ChangeEvent event) {
            NodeList<Element> selectionElements =
                optionsPanel.getElement().getElementsByTagName("input");
            for (int pos = 0; pos < selectionElements.getLength(); pos++) {
              InputElement inputElement = (InputElement) selectionElements.getItem(pos);
              if (inputElement.isChecked()) {
                currentInputElement.setValue(getItemText(pos));
                selectedIndex = pos;
                break;
              }
            }
          }
        });

    setStyleName(RESOURCES.getCSS().listBox());
  }

  /**
   * This constructor may be used by subclasses to explicitly use an existing element.
   *
   * @param element the element to be used
   */
  protected CustomComboBox(Element element) {
    super(element);
  }

  private InputElement getListItemElement(int index) {
    final Element optionElement = (Element) optionsPanel.getElement().getChild(index);

    return (InputElement) optionElement.getElementsByTagName("input").getItem(0);
  }

  /**
   * Adds an ChangeHandler.
   *
   * @param handler the change handler
   */
  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return addDomHandler(handler, ChangeEvent.getType());
  }

  /**
   * Adds an item to the list box.
   *
   * @param item the text of the item to be added
   */
  public void addItem(String item) {
    this.insertItem(item);
  }

  /**
   * Adds an item to the list box, specifying an initial value for the item.
   *
   * @param item the text of the item to be added
   * @param value the item's value, to be submitted if it is part of a {@link
   *     com.google.gwt.user.client.ui.FormPanel}; cannot be <code>null</code>
   */
  public void addItem(String item, String value) {
    this.insertItem(item, value);
  }

  /** Removes all items from the list box. */
  public void clear() {
    selectedIndex = -1;
    optionsPanel.getElement().removeAllChildren();
  }

  /**
   * Gets the number of items present in the list box.
   *
   * @return the number of items
   */
  public int getItemCount() {
    return optionsPanel.getElement().getChildCount();
  }

  /**
   * Gets the text associated with the item at the specified index.
   *
   * @param index the index of the item whose text is to be retrieved
   * @return the text associated with the item
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  public String getItemText(int index) {
    checkIndex(index);
    final Element optionElement = (Element) optionsPanel.getElement().getChild(index);
    final InputElement labelElement =
        (InputElement) optionElement.getElementsByTagName("input").getItem(0);

    return labelElement.getValue();
  }

  /**
   * Gets the text for currently selected item. If multiple items are selected, this method will
   * return the text of the first selected item.
   *
   * @return the text for selected item, or {@code null} if none is selected
   */
  public String getSelectedItemText() {
    int index = getSelectedIndex();

    return index == -1 ? null : getItemText(index);
  }

  /**
   * Gets the currently-selected item.
   *
   * @return the selected index, or <code>-1</code> if none is selected
   */
  public int getSelectedIndex() {
    return selectedIndex;
  }

  /**
   * Gets the value associated with the item at a given index.
   *
   * @param index the index of the item to be retrieved
   * @return the item's associated value
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  public String getValue(int index) {
    checkIndex(index);
    final Element optionElement = (Element) optionsPanel.getElement().getChild(index);
    final InputElement inputElement =
        (InputElement) optionElement.getElementsByTagName("input").getItem(0);

    return inputElement.getValue();
  }

  /**
   * Gets the value for currently selected item.
   *
   * @return the value for selected item, or {@code null} if none is selected
   */
  public String getValue() {
    return currentInputElement.getValue();
  }

  /**
   * Inserts an item into the custom list box.
   *
   * @param item the text of the item to be inserted
   */
  public void insertItem(String item) {
    this.insertItem(item, item);
  }

  /**
   * Inserts an item into the list box.
   *
   * @param item the text of the item to be inserted.
   * @param value the item's value.
   */
  public void insertItem(String item, String value) {
    // create new widget
    final RadioButton radioButton = new RadioButton(optionsGroupName, item);
    // remove the default gwt-RadioButton style
    radioButton.removeStyleName("gwt-RadioButton");
    // set value
    final InputElement inputElement =
        (InputElement) radioButton.getElement().getElementsByTagName("input").getItem(0);
    inputElement.removeAttribute("tabindex");
    inputElement.setAttribute("value", value);
    // set default state
    if (defaultSelectedIndex > -1
        && optionsPanel.getElement().getChildCount() == defaultSelectedIndex) {
      inputElement.setChecked(true);
      currentInputElement.setValue("");
    }
    // add to widget
    optionsPanel.add(radioButton);
  }

  /**
   * Sets custom height inside widget as height and line-height properties.
   *
   * @param height
   */
  public void setHeight(String height) {
    this.getElement().getStyle().setProperty("height", height);
    currentInputElement.getStyle().setProperty("lineHeight", height);
    optionsPanel.getElement().getStyle().setProperty("lineHeight", height);
    optionsPanel
        .getElement()
        .getStyle()
        .setProperty("maxHeight", "calc(" + dropDownSize + "*" + height + ")");
  }

  /**
   * Sets dropdown part size.
   *
   * @param dropDownSize
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  public void setSize(int dropDownSize) {
    if (dropDownSize < 1 || dropDownSize > 99) {
      throw new IndexOutOfBoundsException();
    }
    this.dropDownSize = dropDownSize;
  }

  /**
   * Determines whether an individual list item is selected.
   *
   * @param index the index of the item to be tested
   * @return <code>true</code> if the item is selected
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  public boolean isItemSelected(int index) {
    checkIndex(index);

    return selectedIndex == index;
  }

  /**
   * Removes the item at the specified index.
   *
   * @param index the index of the item to be removed
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  public void removeItem(int index) {
    checkIndex(index);
    if (index == selectedIndex) {
      currentInputElement.setValue("");
      selectedIndex = -1;
    }
    optionsPanel.getElement().removeChild(optionsPanel.getElement().getChild(index));
  }

  /**
   * Sets whether an individual list item is selected.
   *
   * @param index the index of the item to be selected or unselected
   * @param selected <code>true</code> to select the item
   */
  public void setItemSelected(int index, boolean selected) {
    if (index < 0 || index >= getItemCount()) {
      return;
    }
    if (selected) {
      selectedIndex = index;
      currentInputElement.setValue(getItemText(index));
    }
    final InputElement inputElement = getListItemElement(index);
    inputElement.setChecked(selected);
  }

  /**
   * Sets the text associated with the item at a given index.
   *
   * @param index the index of the item to be set
   * @param text the item's new text
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  public void setItemText(int index, String text) {
    checkIndex(index);
    final Element optionElement = (Element) optionsPanel.getElement().getChild(index);
    final LabelElement labelElement =
        (LabelElement) optionElement.getElementsByTagName("label").getItem(0);
    labelElement.setInnerText(text);
    if (selectedIndex == index) {
      currentInputElement.setValue(text);
    }
  }

  /**
   * Sets the currently selected index.
   *
   * @param index the index of the item to be selected
   */
  public void setSelectedIndex(int index) {
    if (index < 0) {
      return;
    }
    // set default index if not added options yet
    if (index >= getItemCount()) {
      defaultSelectedIndex = index;
      return;
    }
    selectedIndex = index;
    currentInputElement.setValue(getItemText(index));
    final InputElement inputElement = getListItemElement(index);
    inputElement.setChecked(true);
  }

  /**
   * Sets the value associated with the item at a given index.
   *
   * @param index the index of the item to be set
   * @param value the item's new value
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  public void setValue(int index, String value) {
    checkIndex(index);
    final InputElement inputElement = getListItemElement(index);
    inputElement.setValue(value);
  }

  /**
   * Sets the value without association to any specific index.
   *
   * @param value the new value
   */
  public void setValue(String value) {
    resetSelectedIndex();
    currentInputElement.setValue(value);
  }

  /** @see com.google.gwt.user.client.ui.UIObject#onEnsureDebugId(String) */
  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
  }

  private void checkIndex(int index) {
    if (index < 0 || index >= getItemCount()) {
      throw new IndexOutOfBoundsException();
    }
  }

  /** Ensures that no item is selected. */
  private void resetSelectedIndex() {
    if (getSelectedIndex() != -1) {
      selectedIndex = -1;
      NodeList<Element> selectionElements = optionsPanel.getElement().getElementsByTagName("input");
      for (int pos = 0; pos < selectionElements.getLength(); pos++) {
        InputElement inputElement = (InputElement) selectionElements.getItem(pos);
        inputElement.setChecked(false);
      }
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    currentInputElement.setDisabled(!enabled);
  }

  @Override
  public void setFocus(boolean focused) {
    currentInputElement.focus();
  }

  /** Check isActive status. */
  private native boolean isActive(Element element) /*-{
        var activeElement = $doc.activeElement;
        return activeElement.isEqualNode(element);
    }-*/;
}
