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
package org.eclipse.che.ide.editor.orion.client;

import static elemental.css.CSSStyleDeclaration.Unit.PX;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.CustomEvent;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.EventTarget;
import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;
import elemental.html.HTMLCollection;
import elemental.html.SpanElement;
import elemental.html.Window;
import java.util.List;
import org.eclipse.che.ide.api.editor.codeassist.Completion;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposalExtension;
import org.eclipse.che.ide.api.editor.events.CompletionRequestEvent;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.editor.texteditor.UndoableEditor;
import org.eclipse.che.ide.editor.orion.client.jso.OrionKeyModeOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionModelChangedEventOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionPixelPositionOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextViewOverlay;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.loging.Log;

/**
 * @author Evgen Vidolob
 * @author Vitaliy Guliy
 * @author Kaloyan Raev
 */
public class ContentAssistWidget implements EventListener {
  /** Custom event type. */
  private static final String CUSTOM_EVT_TYPE_VALIDATE = "itemvalidate";

  private static final String DOCUMENTATION = "documentation";
  private static final int DOM_ITEMS_SIZE = 50;

  private final PopupResources popupResources;

  /** The related editor. */
  private final OrionEditorWidget textEditor;
  /** The main element for the popup. */
  private final Element popupElement;

  private final Element popupBodyElement;
  /** The list (ul) element for the popup. */
  private final Element listElement;

  private final EventListener popupListener;
  private OrionKeyModeOverlay assistMode;
  private boolean visible = false;
  private boolean focused = false;
  private boolean insert = true;

  /** The previously focused element. */
  private Element selectedElement;

  private FlowPanel docPopup;

  private OrionTextViewOverlay.EventHandler<OrionModelChangedEventOverlay> handler;

  private List<CompletionProposal> proposals;
  private Timer callCodeAssistTimer =
      new Timer() {
        @Override
        public void run() {
          textEditor
              .getDocument()
              .getDocumentHandle()
              .getDocEventBus()
              .fireEvent(new CompletionRequestEvent());
        }
      };
  private Timer showDocTimer =
      new Timer() {
        @Override
        public void run() {
          if (selectedElement != null) {
            selectedElement.dispatchEvent(createValidateEvent(DOCUMENTATION));
          }
        }
      };

  @AssistedInject
  public ContentAssistWidget(
      final PopupResources popupResources,
      @Assisted final OrionEditorWidget textEditor,
      @Assisted OrionKeyModeOverlay assistMode) {
    this.popupResources = popupResources;
    this.textEditor = textEditor;
    this.assistMode = assistMode;

    popupElement = Elements.createDivElement(popupResources.popupStyle().popup());

    Element headerElement = Elements.createDivElement(popupResources.popupStyle().header());
    headerElement.setInnerText("Proposals:");
    popupElement.appendChild(headerElement);

    popupBodyElement = Elements.createDivElement(popupResources.popupStyle().body());
    popupElement.appendChild(popupBodyElement);

    listElement = Elements.createUListElement();
    popupBodyElement.appendChild(listElement);

    docPopup = new FlowPanel();
    docPopup.setStyleName(popupResources.popupStyle().popup());
    docPopup.setSize("370px", "180px");

    popupListener =
        evt -> {
          if (!(evt instanceof MouseEvent)) {
            return;
          }
          final MouseEvent mouseEvent = (MouseEvent) evt;
          final EventTarget target = mouseEvent.getTarget();
          if (target instanceof Element) {
            final Element elementTarget = (Element) target;
            if (docPopup.isVisible()
                && (elementTarget.equals(docPopup.getElement())
                    || elementTarget.getParentElement().equals(docPopup.getElement()))) {
              return;
            }

            if (!ContentAssistWidget.this.popupElement.contains(elementTarget)) {
              hide();
              evt.preventDefault();
            }
          }
        };

    handler =
        event -> {
          callCodeAssistTimer.cancel();
          callCodeAssistTimer.schedule(250);
        };
  }

  public void validateItem(boolean insert) {
    this.insert = insert;
    selectedElement.dispatchEvent(createValidateEvent(CUSTOM_EVT_TYPE_VALIDATE));
  }

  /**
   * @param eventType
   * @return
   */
  private native CustomEvent createValidateEvent(String eventType) /*-{
        return new CustomEvent(eventType);
    }-*/;

  /**
   * Creates a new proposal item.
   *
   * @param index of proposal
   */
  private Element createProposalPopupItem(int index) {
    final CompletionProposal proposal = proposals.get(index);
    final Element element = Elements.createLiElement(popupResources.popupStyle().item());
    element.setId(Integer.toString(index));

    final Element icon = Elements.createDivElement(popupResources.popupStyle().icon());
    if (proposal.getIcon() != null && proposal.getIcon().getSVGImage() != null) {
      icon.appendChild((Node) proposal.getIcon().getSVGImage().getElement());
    } else if (proposal.getIcon() != null && proposal.getIcon().getImage() != null) {
      icon.appendChild((Node) proposal.getIcon().getImage().getElement());
    }
    element.appendChild(icon);

    final SpanElement label = Elements.createSpanElement(popupResources.popupStyle().label());
    label.setInnerHTML(proposal.getDisplayString());
    element.appendChild(label);

    element.setTabIndex(1);

    final EventListener validateListener = evt -> applyProposal(proposal);

    element.addEventListener(Event.DBLCLICK, validateListener, false);
    element.addEventListener(CUSTOM_EVT_TYPE_VALIDATE, validateListener, false);
    element.addEventListener(Event.CLICK, event -> select(element), false);
    element.addEventListener(Event.FOCUS, this, false);

    element.addEventListener(
        DOCUMENTATION,
        new EventListener() {
          @Override
          public void handleEvent(Event event) {
            proposal.getAdditionalProposalInfo(
                new AsyncCallback<Widget>() {
                  @Override
                  public void onSuccess(Widget info) {
                    if (info != null) {
                      docPopup.clear();
                      docPopup.add(info);
                      docPopup.getElement().getStyle().setOpacity(1);

                      if (!docPopup.isAttached()) {
                        final int x =
                            popupElement.getOffsetLeft() + popupElement.getOffsetWidth() + 3;
                        final int y = popupElement.getOffsetTop();
                        RootPanel.get().add(docPopup);
                        updateMenuPosition(docPopup, x, y);
                      }
                    } else {
                      docPopup.getElement().getStyle().setOpacity(0);
                    }
                  }

                  @Override
                  public void onFailure(Throwable e) {
                    Log.error(getClass(), e);
                    docPopup.getElement().getStyle().setOpacity(0);
                  }
                });
          }
        },
        false);

    return element;
  }

  private void updateMenuPosition(FlowPanel popupMenu, int x, int y) {
    if (x + popupMenu.getOffsetWidth() > com.google.gwt.user.client.Window.getClientWidth()) {
      popupMenu
          .getElement()
          .getStyle()
          .setLeft(
              x - popupMenu.getOffsetWidth() - popupElement.getOffsetWidth() - 5, Style.Unit.PX);
    } else {
      popupMenu.getElement().getStyle().setLeft(x, Style.Unit.PX);
    }

    if (y + popupMenu.getOffsetHeight() > com.google.gwt.user.client.Window.getClientHeight()) {
      popupMenu
          .getElement()
          .getStyle()
          .setTop(
              y - popupMenu.getOffsetHeight() - popupElement.getOffsetHeight() - 3, Style.Unit.PX);
    } else {
      popupMenu.getElement().getStyle().setTop(y, Style.Unit.PX);
    }
  }

  private void addPopupEventListeners() {
    Elements.getDocument().addEventListener(Event.MOUSEDOWN, this.popupListener, false);

    textEditor.getTextView().addKeyMode(assistMode);

    // add key event listener on popup
    textEditor
        .getTextView()
        .setAction(
            "cheContentAssistCancel",
            () -> {
              hide();
              return true;
            });

    textEditor
        .getTextView()
        .setAction(
            "cheContentAssistApply",
            () -> {
              validateItem(true);
              return true;
            });

    textEditor
        .getTextView()
        .setAction(
            "cheContentAssistPreviousProposal",
            () -> {
              selectPrevious();
              return true;
            });

    textEditor
        .getTextView()
        .setAction(
            "cheContentAssistNextProposal",
            () -> {
              selectNext();
              return true;
            });

    textEditor
        .getTextView()
        .setAction(
            "cheContentAssistNextPage",
            () -> {
              selectNextPage();
              return true;
            });

    textEditor
        .getTextView()
        .setAction(
            "cheContentAssistPreviousPage",
            () -> {
              selectPreviousPage();
              return true;
            });

    textEditor
        .getTextView()
        .setAction(
            "cheContentAssistEnd",
            () -> {
              selectLast();
              return true;
            });

    textEditor
        .getTextView()
        .setAction(
            "cheContentAssistHome",
            () -> {
              selectFirst();
              return true;
            });

    textEditor
        .getTextView()
        .setAction(
            "cheContentAssistTab",
            () -> {
              validateItem(false);
              return true;
            });

    textEditor.getTextView().addEventListener("ModelChanging", handler);
    listElement.addEventListener(Event.KEYDOWN, this, false);
    popupBodyElement.addEventListener(Event.SCROLL, this, false);
  }

  private void removePopupEventListeners() {
    /* Remove popup listeners. */
    textEditor.getTextView().removeKeyMode(assistMode);
    textEditor.getTextView().removeEventListener("ModelChanging", handler, false);

    // remove the keyboard listener
    listElement.removeEventListener(Event.KEYDOWN, this, false);

    // remove the scroll listener
    popupBodyElement.removeEventListener(Event.SCROLL, this, false);

    // remove the mouse listener
    Elements.getDocument().removeEventListener(Event.MOUSEDOWN, this.popupListener);
  }

  private void selectFirst() {
    scrollTo(0);
    updateIfNecessary();
    select(getFirstItemInDOM());
  }

  private void selectLast() {
    scrollTo(getTotalItems() - 1);
    updateIfNecessary();
    select(getLastItemInDOM());
  }

  private void select(int index) {
    select(getItem(index));
  }

  private void selectPrevious() {
    Element previousElement = selectedElement.getPreviousElementSibling();
    if (previousElement != null
        && previousElement == getExtraTopRow()
        && getExtraTopRow().isHidden()) {
      selectLast();
    } else {
      selectOffset(-1);
    }
  }

  private void selectPreviousPage() {
    int offset = getItemsPerPage() - 1;
    selectOffset(-offset);
  }

  private void selectNext() {
    Element nextElement = selectedElement.getNextElementSibling();
    if (nextElement != null
        && nextElement == getExtraBottomRow()
        && getExtraBottomRow().isHidden()) {
      selectFirst();
    } else {
      selectOffset(1);
    }
  }

  private void selectNextPage() {
    int offset = getItemsPerPage() - 1;
    selectOffset(offset);
  }

  private void selectOffset(int offset) {
    int index = getItemId(selectedElement) + offset;
    index = Math.max(index, 0);
    index = Math.min(index, getTotalItems() - 1);

    if (!isItemInDOM(index)) {
      scrollTo(index);
      update();
    }

    select(index);
  }

  private void select(Element element) {
    if (element == selectedElement) {
      return;
    }

    if (selectedElement != null) {
      selectedElement.removeAttribute("selected");
    }
    selectedElement = element;
    selectedElement.setAttribute("selected", "true");

    showDocTimer.cancel();
    showDocTimer.schedule(docPopup.isAttached() ? 100 : 1500);

    if (selectedElement.getOffsetTop() < this.popupBodyElement.getScrollTop()) {
      selectedElement.scrollIntoView(true);
    } else if ((selectedElement.getOffsetTop() + selectedElement.getOffsetHeight())
        > (this.popupBodyElement.getScrollTop() + this.popupBodyElement.getClientHeight())) {
      selectedElement.scrollIntoView(false);
    }
  }

  /**
   * Displays assist popup relative to the current cursor position.
   *
   * @param proposals proposals to display
   */
  public void show(final List<CompletionProposal> proposals) {
    this.proposals = proposals;

    OrionTextViewOverlay textView = textEditor.getTextView();
    OrionPixelPositionOverlay caretLocation =
        textView.getLocationAtOffset(textView.getCaretOffset());
    caretLocation.setY(caretLocation.getY() + textView.getLineHeight());
    caretLocation = textView.convert(caretLocation, "document", "page");

    /** The fastest way to remove element children. Clear and add items. */
    listElement.setInnerHTML("");

    /* Display an empty popup when it is nothing to show. */
    if (getTotalItems() == 0) {
      final Element emptyElement = Elements.createLiElement(popupResources.popupStyle().item());
      emptyElement.setTextContent("No proposals");
      listElement.appendChild(emptyElement);
      return;
    }

    /* Automatically apply the completion proposal if it only one. */
    if (getTotalItems() == 1) {
      applyProposal(proposals.get(0));
      return;
    }

    /* Reset popup dimensions and show. */
    popupElement.getStyle().setLeft(caretLocation.getX(), PX);
    popupElement.getStyle().setTop(caretLocation.getY(), PX);
    popupElement.getStyle().setWidth("400px");
    popupElement.getStyle().setHeight("200px");
    popupElement.getStyle().setOpacity(1);
    Elements.getDocument().getBody().appendChild(this.popupElement);

    /* Add the top extra row. */
    setExtraRowHeight(appendExtraRow(), 0);

    /* Add the popup items. */
    for (int i = 0; i < Math.min(DOM_ITEMS_SIZE, getTotalItems()); i++) {
      listElement.appendChild(createProposalPopupItem(i));
    }

    /* Add the bottom extra row. */
    setExtraRowHeight(appendExtraRow(), Math.max(0, getTotalItems() - DOM_ITEMS_SIZE));

    /* Correct popup position (wants to be refactored) */
    final Window window = Elements.getWindow();
    final int viewportWidth = window.getInnerWidth();
    final int viewportHeight = window.getInnerHeight();

    int spaceBelow = viewportHeight - caretLocation.getY();
    if (this.popupElement.getOffsetHeight() > spaceBelow) {
      // Check if div is too large to fit above
      int spaceAbove = caretLocation.getY() - textView.getLineHeight();
      if (this.popupElement.getOffsetHeight() > spaceAbove) {
        // Squeeze the div into the larger area
        if (spaceBelow > spaceAbove) {
          this.popupElement.getStyle().setProperty("maxHeight", spaceBelow + "px");
        } else {
          this.popupElement.getStyle().setProperty("maxHeight", spaceAbove + "px");
          this.popupElement.getStyle().setTop("0");
        }
      } else {
        // Put the div above the line
        this.popupElement
            .getStyle()
            .setTop(
                (caretLocation.getY()
                        - this.popupElement.getOffsetHeight()
                        - textView.getLineHeight())
                    + "px");
        this.popupElement.getStyle().setProperty("maxHeight", spaceAbove + "px");
      }
    } else {
      this.popupElement.getStyle().setProperty("maxHeight", spaceBelow + "px");
    }

    if (caretLocation.getX() + this.popupElement.getOffsetWidth() > viewportWidth) {
      int leftSide = viewportWidth - this.popupElement.getOffsetWidth();
      if (leftSide < 0) {
        leftSide = 0;
      }
      this.popupElement.getStyle().setLeft(leftSide + "px");
      this.popupElement.getStyle().setProperty("maxWidth", (viewportWidth - leftSide) + "px");
    } else {
      this.popupElement
          .getStyle()
          .setProperty("maxWidth", viewportWidth + caretLocation.getX() + "px");
    }

    /* Don't attach handlers twice. Visible popup must already have their attached. */
    if (!visible) {
      addPopupEventListeners();
    }

    /* Indicates the codeassist is visible. */
    visible = true;
    focused = false;

    /* Update documentation popup position */
    docPopup
        .getElement()
        .getStyle()
        .setLeft(popupElement.getOffsetLeft() + popupElement.getOffsetWidth() + 3, Style.Unit.PX);
    docPopup.getElement().getStyle().setTop(popupElement.getOffsetTop(), Style.Unit.PX);

    /* Select first row. */
    selectFirst();
  }

  /** Hides the popup and displaying javadoc. */
  public void hide() {
    textEditor.setFocus();

    if (docPopup.isAttached()) {
      docPopup.getElement().getStyle().setOpacity(0);
      new Timer() {
        @Override
        public void run() {
          docPopup.removeFromParent();
        }
      }.schedule(250);
    }

    popupElement.getStyle().setOpacity(0);
    new Timer() {
      @Override
      public void run() {
        // detach assist popup
        if (popupElement.getParentNode() != null) {
          popupElement.getParentNode().removeChild(popupElement);
        }
        // remove all items from popup element
        listElement.setInnerHTML("");
      }
    }.schedule(250);

    visible = false;
    selectedElement = null;
    showDocTimer.cancel();

    removePopupEventListeners();
  }

  @Override
  public void handleEvent(Event evt) {
    if (Event.KEYDOWN.equalsIgnoreCase(evt.getType())) {
      final KeyboardEvent keyEvent = (KeyboardEvent) evt;
      switch (keyEvent.getKeyCode()) {
        case KeyCodes.KEY_ESCAPE:
          Scheduler.get().scheduleDeferred(this::hide);
          break;

        case KeyCodes.KEY_DOWN:
          selectNext();
          evt.preventDefault();
          break;

        case KeyCodes.KEY_UP:
          selectPrevious();
          evt.preventDefault();
          break;

        case KeyCodes.KEY_PAGEUP:
          selectPreviousPage();
          evt.preventDefault();
          break;

        case KeyCodes.KEY_PAGEDOWN:
          selectNextPage();
          evt.preventDefault();
          break;

        case KeyCodes.KEY_HOME:
          selectFirst();
          break;

        case KeyCodes.KEY_END:
          selectLast();
          break;

        case KeyCodes.KEY_ENTER:
          evt.preventDefault();
          evt.stopImmediatePropagation();
          validateItem(true);
          break;

        case KeyCodes.KEY_TAB:
          evt.preventDefault();
          evt.stopImmediatePropagation();
          validateItem(false);
          break;
      }
    } else if (Event.SCROLL.equalsIgnoreCase(evt.getType())) {
      updateIfNecessary();
    } else if (Event.FOCUS.equalsIgnoreCase(evt.getType())) {
      focused = true;
    }
  }

  private void updateIfNecessary() {
    int scrollTop = popupBodyElement.getScrollTop();
    int extraTopHeight = getExtraTopRow().getClientHeight();

    if (scrollTop < extraTopHeight) {
      // the scroll bar is above the buffered area
      update();
    } else if (scrollTop + popupBodyElement.getClientHeight()
        > extraTopHeight + getItemHeight() * DOM_ITEMS_SIZE) {
      // the scroll bar is below the buffered area
      update();
    }
  }

  private void update() {
    int scrollTop = popupBodyElement.getScrollTop();
    int itemHeight = getItemHeight();
    int topVisibleItem = scrollTop == 0 || itemHeight == 0 ? 0 : scrollTop / itemHeight;
    int topDOMItem = Math.max(0, topVisibleItem - (DOM_ITEMS_SIZE - getItemsPerPage()) / 2);
    int bottomDOMItem = Math.min(getTotalItems() - 1, topDOMItem + DOM_ITEMS_SIZE - 1);
    if (bottomDOMItem == getTotalItems() - 1) {
      topDOMItem = Math.max(0, bottomDOMItem - DOM_ITEMS_SIZE + 1);
    }

    // resize the extra top row
    setExtraRowHeight(getExtraTopRow(), topDOMItem);

    // replace the DOM items with new content based on the scroll position
    HTMLCollection nodes = listElement.getChildren();
    for (int i = 0; i <= (bottomDOMItem - topDOMItem); i++) {
      Element newNode = createProposalPopupItem(topDOMItem + i);
      listElement.replaceChild(newNode, nodes.item(i + 1));

      // check if the item is the selected
      if (newNode.getId().equals(selectedElement.getId())) {
        selectedElement = newNode;
        selectedElement.setAttribute("selected", "true");
      }
    }

    // resize the extra bottom row
    setExtraRowHeight(getExtraBottomRow(), getTotalItems() - (bottomDOMItem + 1));

    // ensure the keyboard focus is in the visible area
    if (focused) {
      getItem(topDOMItem + (bottomDOMItem - topDOMItem) / 2).focus();
    }
  }

  /**
   * Uses to determine the autocompletion popup visibility.
   *
   * @return <b>true</b> if the popup is visible, otherwise returns <b>false</b>
   */
  public boolean isVisible() {
    return visible;
  }

  public void showCompletionInfo() {
    if (visible && selectedElement != null) {
      selectedElement.dispatchEvent(createValidateEvent(DOCUMENTATION));
    }
  }

  private void applyProposal(CompletionProposal proposal) {
    CompletionProposal.CompletionCallback callback = this::applyCompletion;

    hide();

    if (proposal instanceof CompletionProposalExtension) {
      ((CompletionProposalExtension) proposal).getCompletion(insert, callback);
    } else {
      proposal.getCompletion(callback);
    }
  }

  private void applyCompletion(Completion completion) {
    textEditor.setFocus();
    UndoableEditor undoableEditor = textEditor;
    HandlesUndoRedo undoRedo = undoableEditor.getUndoRedo();

    try {
      if (undoRedo != null) {
        undoRedo.beginCompoundChange();
      }
      completion.apply(textEditor.getDocument());
      final LinearRange selection = completion.getSelection(textEditor.getDocument());
      if (selection != null) {
        selectInEditor(selection);
      }
    } catch (final Exception e) {
      Log.error(getClass(), e);
    } finally {
      if (undoRedo != null) {
        undoRedo.endCompoundChange();
      }
    }
  }

  private void selectInEditor(LinearRange selection) {
    int lineAtOffset = textEditor.getDocument().getLineAtOffset(selection.getStartOffset());
    boolean scroll = false;
    if (lineAtOffset < textEditor.getTextView().getTopIndex()
        || lineAtOffset > textEditor.getTextView().getBottomIndex()) {
      scroll = true;
    }
    textEditor.getDocument().setSelectedRange(selection, scroll);
  }

  private Element getExtraTopRow() {
    return (listElement == null) ? null : listElement.getFirstElementChild();
  }

  private Element getExtraBottomRow() {
    return (listElement == null) ? null : listElement.getLastElementChild();
  }

  private Element getFirstItemInDOM() {
    Element extraTopRow = getExtraTopRow();
    return (extraTopRow == null) ? null : extraTopRow.getNextElementSibling();
  }

  private Element getLastItemInDOM() {
    Element extraBottomRow = getExtraBottomRow();
    return (extraBottomRow == null) ? null : extraBottomRow.getPreviousElementSibling();
  }

  private int getItemId(Element item) {
    return Integer.parseInt(item.getId());
  }

  private Element getItem(int index) {
    return (Element) listElement.getChildren().namedItem(Integer.toString(index));
  }

  private int getItemHeight() {
    Element item = getFirstItemInDOM();
    return (item == null) ? 0 : item.getClientHeight();
  }

  private Element appendExtraRow() {
    Element extraRow = Elements.createLiElement();
    listElement.appendChild(extraRow);
    return extraRow;
  }

  private void setExtraRowHeight(Element extraRow, int items) {
    int height = items * getItemHeight();
    extraRow.getStyle().setHeight(height + "px");
    extraRow.setHidden(height <= 0);
  }

  private int getItemsPerPage() {
    int itemHeight = getItemHeight();
    if (itemHeight == 0) {
      return 0;
    }
    return (int) Math.ceil((double) popupBodyElement.getClientHeight() / itemHeight);
  }

  private int getTotalItems() {
    return (proposals == null) ? 0 : proposals.size();
  }

  private boolean isItemInDOM(int index) {
    return index >= getItemId(getFirstItemInDOM()) && index <= getItemId(getLastItemInDOM());
  }

  private void scrollTo(int index) {
    int currentScrollTop = popupBodyElement.getScrollTop();
    int newScrollTop = index * getItemHeight();
    if (currentScrollTop < newScrollTop) {
      // the scrolling direction is from top to bottom, so show the item
      // at the bottom of the widget
      newScrollTop -= popupBodyElement.getClientHeight();
    }
    popupBodyElement.setScrollTop(newScrollTop);
  }
}
