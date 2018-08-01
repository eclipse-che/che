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
package org.eclipse.che.ide.editor.quickfix;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.CustomEvent;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.SpanElement;
import org.eclipse.che.ide.api.editor.codeassist.Completion;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.editor.texteditor.UndoableEditor;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.ui.popup.PopupWidget;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.loging.Log;

/** Widget for quick assist display. */
public class QuickAssistWidget extends PopupWidget<CompletionProposal> {

  /** Custom event type. */
  private static final String CUSTOM_EVT_TYPE_VALIDATE = "itemvalidate";

  /** The related editor. */
  private final TextEditor textEditor;

  @AssistedInject
  public QuickAssistWidget(
      final PopupResources popupResources, @Assisted final TextEditor textEditor) {
    super(popupResources, "Proposals:");
    this.textEditor = textEditor;
  }

  public Element createItem(final CompletionProposal proposal) {
    final Element element = Elements.createLiElement(popupResources.popupStyle().item());

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

    final EventListener validateListener =
        new EventListener() {
          @Override
          public void handleEvent(final Event evt) {
            proposal.getCompletion(
                new CompletionProposal.CompletionCallback() {
                  @Override
                  public void onCompletion(final Completion completion) {
                    HandlesUndoRedo undoRedo = null;
                    if (textEditor instanceof UndoableEditor) {
                      UndoableEditor undoableEditor =
                          (UndoableEditor) QuickAssistWidget.this.textEditor;
                      undoRedo = undoableEditor.getUndoRedo();
                    }
                    try {
                      if (undoRedo != null) {
                        undoRedo.beginCompoundChange();
                      }
                      completion.apply(textEditor.getDocument());
                      final LinearRange selection =
                          completion.getSelection(textEditor.getDocument());
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
                });
            hide();
          }
        };

    element.addEventListener(Event.DBLCLICK, validateListener, false);
    element.addEventListener(CUSTOM_EVT_TYPE_VALIDATE, validateListener, false);

    return element;
  }

  @Override
  public String getEmptyMessage() {
    return "No proposals";
  }

  @Override
  public void validateItem(final Element validatedItem) {
    validatedItem.dispatchEvent(createValidateEvent(CUSTOM_EVT_TYPE_VALIDATE));
    super.validateItem(validatedItem);
  }

  /* Overriden to give the popup the focus for keyboard control */
  @Override
  public boolean needsFocus() {
    return true;
  }

  private native CustomEvent createValidateEvent(String eventType) /*-{
        return new CustomEvent(eventType);
    }-*/;
}
