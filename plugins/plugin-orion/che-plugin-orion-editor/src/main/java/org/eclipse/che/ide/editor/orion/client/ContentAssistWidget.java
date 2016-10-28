/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.editor.orion.client;

import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.CustomEvent;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.EventTarget;
import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;
import elemental.html.SpanElement;
import elemental.html.Window;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.ide.api.editor.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.editor.texteditor.UndoableEditor;
import org.eclipse.che.ide.editor.orion.client.jso.OrionKeyModeOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionModelChangedEventOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionPixelPositionOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextViewOverlay;
import org.eclipse.che.ide.api.editor.codeassist.Completion;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposalExtension;
import org.eclipse.che.ide.api.editor.events.CompletionRequestEvent;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.loging.Log;

import java.util.List;

import static elemental.css.CSSStyleDeclaration.Unit.PX;

/**
 * @author Evgen Vidolob
 * @author Vitaliy Guliy
 */
public class ContentAssistWidget implements EventListener {
    /**
     * Custom event type.
     */
    private static final String CUSTOM_EVT_TYPE_VALIDATE = "itemvalidate";
    private static final String DOCUMENTATION            = "documentation";

    private final PopupResources popupResources;

    /** The related editor. */
    private final OrionEditorWidget   textEditor;
    private       OrionKeyModeOverlay assistMode;

    /** The main element for the popup. */
    private final Element popupElement;
    private final Element popupBodyElement;

    /** The list (ul) element for the popup. */
    private final Element listElement;

    private final EventListener popupListener;

    private boolean visible = false;
    private boolean insert  = true;

    /**
     * The previously focused element.
     */
    private Element   selectedElement;
    private FlowPanel docPopup;

    private OrionTextViewOverlay.EventHandler<OrionModelChangedEventOverlay> handler;

    @AssistedInject
    public ContentAssistWidget(final PopupResources popupResources,
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

        popupListener = new EventListener() {
            @Override
            public void handleEvent(final Event evt) {
                if (evt instanceof MouseEvent) {
                    final MouseEvent mouseEvent = (MouseEvent)evt;
                    final EventTarget target = mouseEvent.getTarget();
                    if (target instanceof Element) {
                        final Element elementTarget = (Element)target;
                        if (elementTarget.equals(docPopup.getElement()) && docPopup.isVisible()) {
                            return;
                        }

                        if (!ContentAssistWidget.this.popupElement.contains(elementTarget)) {
                            hide();
                            evt.preventDefault();
                        }
                    }
                }
                // else won't happen
            }
        };

        handler = new OrionTextViewOverlay.EventHandler<OrionModelChangedEventOverlay>() {
            @Override
            public void onEvent(OrionModelChangedEventOverlay event) {
                callCodeAssistTimer.cancel();
                callCodeAssistTimer.schedule(250);
            }
        };
    }

    private Timer callCodeAssistTimer = new Timer() {
        @Override
        public void run() {
            textEditor.getDocument().getDocumentHandle().getDocEventBus().fireEvent(new CompletionRequestEvent());
        }
    };

    public void validateItem(boolean replace) {
        this.insert = replace;
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
     * Appends new proposal item to the popup
     *
     * @param proposal
     */
    private void addProposalPopupItem(final CompletionProposal proposal) {
        final Element element = Elements.createLiElement(popupResources.popupStyle().item());

        final Element icon = Elements.createDivElement(popupResources.popupStyle().icon());
        if (proposal.getIcon() != null && proposal.getIcon().getSVGImage() != null) {
            icon.appendChild((Node)proposal.getIcon().getSVGImage().getElement());
        } else if (proposal.getIcon() != null && proposal.getIcon().getImage() != null) {
            icon.appendChild((Node)proposal.getIcon().getImage().getElement());
        }
        element.appendChild(icon);

        final SpanElement label = Elements.createSpanElement(popupResources.popupStyle().label());
        label.setInnerHTML(proposal.getDisplayString());
        element.appendChild(label);

        element.setTabIndex(1);

        // add item to the popup
        listElement.appendChild(element);

        final EventListener validateListener = new EventListener() {
            @Override
            public void handleEvent(final Event evt) {
                CompletionProposal.CompletionCallback callback = new CompletionProposal.CompletionCallback() {
                    @Override
                    public void onCompletion(final Completion completion) {
                        applyCompletion(completion);
                    }
                };

                if (proposal instanceof CompletionProposalExtension) {
                    ((CompletionProposalExtension)proposal).getCompletion(insert, callback);
                } else {
                    proposal.getCompletion(callback);
                }

                hide();
            }
        };

        element.addEventListener(Event.DBLCLICK, validateListener, false);
        element.addEventListener(CUSTOM_EVT_TYPE_VALIDATE, validateListener, false);
        element.addEventListener(Event.CLICK, new EventListener() {
            @Override
            public void handleEvent(Event event) {
                selectElement(element);
            }
        }, false);

        element.addEventListener(DOCUMENTATION, new EventListener() {
            @Override
            public void handleEvent(Event event) {
                Widget info = proposal.getAdditionalProposalInfo();

                if (info != null) {
                    docPopup.clear();
                    docPopup.add(info);

                    if (docPopup.isAttached()) {
                        return;
                    }

                    docPopup.getElement().getStyle()
                            .setLeft(popupElement.getOffsetLeft() + popupElement.getOffsetWidth() + 3, Style.Unit.PX);
                    docPopup.getElement().getStyle().setTop(popupElement.getOffsetTop(), Style.Unit.PX);
                    RootPanel.get().add(docPopup);
                    docPopup.getElement().getStyle().setOpacity(1);
                }
            }
        }, false);
    }

    private void addPopupEventListeners() {
        Elements.getDocument().addEventListener(Event.MOUSEDOWN, this.popupListener, false);

        textEditor.getTextView().addKeyMode(assistMode);

        // add key event listener on popup
        textEditor.getTextView().setAction("cheContentAssistCancel", new Action() {
            @Override
            public boolean onAction() {
                hide();
                return true;
            }
        });

        textEditor.getTextView().setAction("cheContentAssistApply", new Action() {
            @Override
            public boolean onAction() {
                validateItem(true);
                return true;
            }
        });

        textEditor.getTextView().setAction("cheContentAssistPreviousProposal", new Action() {
            @Override
            public boolean onAction() {
                selectPrevious();
                return true;
            }
        });

        textEditor.getTextView().setAction("cheContentAssistNextProposal", new Action() {
            @Override
            public boolean onAction() {
                selectNext();
                return true;
            }
        });

        textEditor.getTextView().setAction("cheContentAssistNextPage", new Action() {
            @Override
            public boolean onAction() {
                selectNext(listElement.getParentElement().getOffsetHeight() / listElement.getFirstElementChild().getOffsetHeight() - 1);
                return true;
            }
        });

        textEditor.getTextView().setAction("cheContentAssistPreviousPage", new Action() {
            @Override
            public boolean onAction() {
                selectPrevious(listElement.getParentElement().getOffsetHeight() / listElement.getFirstElementChild().getOffsetHeight() - 1);
                return true;
            }
        });

        textEditor.getTextView().setAction("cheContentAssistEnd", new Action() {
            @Override
            public boolean onAction() {
                selectElement(listElement.getLastElementChild());
                return true;
            }
        });

        textEditor.getTextView().setAction("cheContentAssistHome", new Action() {
            @Override
            public boolean onAction() {
                selectElement(listElement.getFirstElementChild());
                return true;
            }
        });

        textEditor.getTextView().setAction("cheContentAssistTab", new Action() {
            @Override
            public boolean onAction() {
                validateItem(false);
                return true;
            }
        });

        textEditor.getTextView().addEventListener("ModelChanging", handler);
        listElement.addEventListener(Event.KEYDOWN, this, false);
    }

    private void removePopupEventListeners() {
        /* Remove popup listeners. */
        textEditor.getTextView().removeKeyMode(assistMode);
        textEditor.getTextView().removeEventListener("ModelChanging", handler, false);

        // remove the keyboard listener
        listElement.removeEventListener(Event.KEYDOWN, this, false);

        // remove the mouse listener
        Elements.getDocument().removeEventListener(Event.MOUSEDOWN, this.popupListener);
    }

    private void selectPrevious() {
        Element previousElement = selectedElement.getPreviousElementSibling();
        if (previousElement != null) {
            selectElement(previousElement);
        } else {
            selectElement(listElement.getLastElementChild());
        }
    }

    private void selectPrevious(int offset) {
        Element element = selectedElement;

        for (int i = 0; i < offset; i++) {
            Element previousElement = element.getPreviousElementSibling();
            if (previousElement == null) {
                break;
            }
            element = previousElement;
        }

        selectElement(element);
    }

    private void selectNext() {
        Element nextElement = selectedElement.getNextElementSibling();
        if (nextElement != null) {
            selectElement(nextElement);
        } else {
            selectElement(listElement.getFirstElementChild());
        }
    }

    private void selectNext(int offset) {
        Element element = selectedElement;

        for (int i = 0; i < offset; i++) {
            Element nextElement = element.getNextElementSibling();
            if (nextElement == null) {
                break;
            }
            element = nextElement;
        }

        selectElement(element);
    }

    private void selectElement(Element element) {
        if (selectedElement != null) {
            selectedElement.removeAttribute("selected");
        }

        if (docPopup.isAttached()) {
            if (element != selectedElement) {
                element.dispatchEvent(createValidateEvent(DOCUMENTATION));
            }
        } else {
            showDocTimer.cancel();
            showDocTimer.schedule(1500);
        }

        selectedElement = element;
        selectedElement.setAttribute("selected", "true");

        if (selectedElement.getOffsetTop() < this.popupBodyElement.getScrollTop()) {
            selectedElement.scrollIntoView(true);
        } else if ((selectedElement.getOffsetTop() + selectedElement.getOffsetHeight()) >
                   (this.popupBodyElement.getScrollTop() + this.popupBodyElement.getClientHeight())) {
            selectedElement.scrollIntoView(false);
        }
    }

    private Timer showDocTimer = new Timer() {
        @Override
        public void run() {
            if (selectedElement != null) {
                selectedElement.dispatchEvent(createValidateEvent(DOCUMENTATION));
            }
        }
    };

    /**
     * Displays assist popup relative to the current cursor position.
     *
     * @param proposals
     *         proposals to display
     */
    public void show(final List<CompletionProposal> proposals) {
        OrionTextViewOverlay textView = textEditor.getTextView();
        OrionPixelPositionOverlay caretLocation = textView.getLocationAtOffset(textView.getCaretOffset());
        caretLocation.setY(caretLocation.getY() + textView.getLineHeight());
        caretLocation = textView.convert(caretLocation, "document", "page");

        /** The fastest way to remove element children. Clear and add items. */
        listElement.setInnerHTML("");

        /* Display an empty popup when it is nothing to show. */
        if (proposals == null || proposals.isEmpty()) {
            final Element emptyElement = Elements.createLiElement(popupResources.popupStyle().item());
            emptyElement.setTextContent("No proposals");
            listElement.appendChild(emptyElement);
            return;
        }

        if (proposals.size() == 1) {
            CompletionProposal.CompletionCallback callback = new CompletionProposal.CompletionCallback() {
                @Override
                public void onCompletion(Completion completion) {
                    applyCompletion(completion);
                }
            };

            CompletionProposal proposal = proposals.get(0);
            proposal.getCompletion(callback);

            return;
        }

        /* Add new popup items. */
        for (CompletionProposal proposal : proposals) {
            addProposalPopupItem(proposal);
        }

        /* Reset popup dimensions and show. */
        popupElement.getStyle().setLeft(caretLocation.getX(), PX);
        popupElement.getStyle().setTop(caretLocation.getY(), PX);
        popupElement.getStyle().setWidth("400px");
        popupElement.getStyle().setHeight("200px");
        popupElement.getStyle().setOpacity(0);
        Elements.getDocument().getBody().appendChild(this.popupElement);

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                popupElement.getStyle().setOpacity(1);
            }
        });

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
                this.popupElement.getStyle()
                                 .setTop((caretLocation.getY() - this.popupElement.getOffsetHeight() - textView.getLineHeight()) + "px");
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
            this.popupElement.getStyle().setProperty("maxWidth", viewportWidth + caretLocation.getX() + "px");
        }

        /* Don't attach handlers twice. Visible popup must already their attached. */
        if (!visible) {
            addPopupEventListeners();
        }

        /* Indicates the codeassist is visible. */
        visible = true;

        if (docPopup.isAttached()) {
            docPopup.getElement().getStyle().setOpacity(0);
            new Timer() {
                @Override
                public void run() {
                    docPopup.removeFromParent();
                    showDocTimer.schedule(1500);
                }
            }.schedule(250);
        }

        /* Select first row. */
        selectElement(listElement.getFirstElementChild());
    }

    /**
     * Hides the popup and displaying javadoc.
     */
    public void hide() {
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
                popupElement.getParentNode().removeChild(popupElement);
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
        if (evt instanceof KeyboardEvent) {
            final KeyboardEvent keyEvent = (KeyboardEvent)evt;
            switch (keyEvent.getKeyCode()) {
                case KeyCodes.KEY_ESCAPE:
                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            hide();
                        }
                    });
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
                    selectPrevious(listElement.getParentElement().getOffsetHeight() / listElement.getFirstElementChild().getOffsetHeight() - 1);
                    evt.preventDefault();
                    break;

                case KeyCodes.KEY_PAGEDOWN:
                    selectNext(listElement.getParentElement().getOffsetHeight() / listElement.getFirstElementChild().getOffsetHeight() - 1);
                    evt.preventDefault();
                    break;

                case KeyCodes.KEY_HOME:
                    selectElement(listElement.getFirstElementChild());
                    break;

                case KeyCodes.KEY_END:
                    selectElement(listElement.getLastElementChild());
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
                textEditor.getDocument().setSelectedRange(selection, true);
            }
        } catch (final Exception e) {
            Log.error(getClass(), e);
        } finally {
            if (undoRedo != null) {
                undoRedo.endCompoundChange();
            }
        }
    }

}
