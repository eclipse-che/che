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
package org.eclipse.che.ide.editor.texteditor;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import elemental.dom.Element;
import org.eclipse.che.ide.api.editor.codeassist.AdditionalInfoCallback;
import org.eclipse.che.ide.api.editor.codeassist.CompletionsSource;
import org.eclipse.che.ide.api.editor.events.CursorActivityEvent;
import org.eclipse.che.ide.api.editor.events.CursorActivityHandler;
import org.eclipse.che.ide.api.editor.keymap.Keymap;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.EditorWidget;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPartView;
import org.eclipse.che.ide.editor.codeassist.AdditionalInformationWidget;
import org.eclipse.che.ide.editor.texteditor.infopanel.InfoPanel;
import org.eclipse.che.ide.ui.popup.PopupResources;

/**
 * Implementation of the View part of the editors.
 *
 * @author "Mickaël Leduque"
 */
public class TextEditorPartViewImpl extends Composite implements TextEditorPartView {

  /** The UI binder for the component. */
  private static final EditorViewUiBinder uibinder = GWT.create(EditorViewUiBinder.class);

  /** The info panel bar for the editor. */
  @UiField(provided = true)
  InfoPanel infoPanel;

  /** The container for the real editor widget. */
  @UiField SimplePanel editorPanel;

  /** The view delegate. */
  private Delegate delegate;

  /** The resources for the additional infos popup. */
  private PopupResources popupResources;

  @Inject
  public TextEditorPartViewImpl(final InfoPanel infoPanel) {
    this.infoPanel = infoPanel;

    final HTMLPanel panel = uibinder.createAndBindUi(this);
    initWidget(panel);
  }

  @Override
  public void onResize() {
    getDelegate().onResize();
  }

  @Override
  public void showCompletionProposals(
      final EditorWidget editorWidget, final CompletionsSource source) {
    editorWidget.showCompletionProposals(
        source,
        new AdditionalInfoCallback() {

          @Override
          public Element onAdditionalInfoNeeded(
              final float pixelX, final float pixelY, final Element infoWidget) {
            final AdditionalInformationWidget popup =
                new AdditionalInformationWidget(popupResources);
            popup.addItem(infoWidget);
            popup.show(pixelX, pixelY);
            return popup.asElement();
          }
        });
  }

  @Override
  public void showCompletionProposals(final EditorWidget editorWidget) {
    editorWidget.showCompletionProposals();
  }

  @Override
  public void setDelegate(final Delegate delegate) {
    this.delegate = delegate;
  }

  protected Delegate getDelegate() {
    return this.delegate;
  }

  /**
   * Sets the additional infos popup resource.
   *
   * @param popupResources the resource
   */
  @Inject
  public void setPopupResource(final PopupResources popupResources) {
    this.popupResources = popupResources;
  }

  @Override
  public void setEditorWidget(final EditorWidget editorWidget) {
    if (this.editorPanel.getWidget() != null) {
      throw new RuntimeException("Editor already set");
    }
    this.editorPanel.setWidget(editorWidget);

    editorWidget.addCursorActivityHandler(
        new CursorActivityHandler() {
          @Override
          public void onCursorActivity(final CursorActivityEvent event) {
            delegate.editorCursorPositionChanged();
          }
        });
    editorWidget.addBlurHandler(
        new BlurHandler() {
          @Override
          public void onBlur(final BlurEvent event) {
            delegate.editorLostFocus();
          }
        });
    editorWidget.addFocusHandler(
        new FocusHandler() {
          @Override
          public void onFocus(final FocusEvent event) {
            delegate.editorGotFocus();
          }
        });
  }

  @Override
  public void showPlaceHolder(final Widget placeHolder) {
    this.editorPanel.setWidget(placeHolder);
  }

  @Override
  public void setInfoPanelVisible(boolean visible) {
    infoPanel.setVisible(visible);
  }

  @Override
  public void initInfoPanel(
      final String mode, final Keymap keymap, final int lineCount, final int tabSize) {
    this.infoPanel.createDefaultState(mode, lineCount, tabSize);
  }

  @Override
  public void updateInfoPanelPosition(final TextPosition position) {
    this.infoPanel.updateCursorPosition(position);
  }

  @Override
  public void updateInfoPanelUnfocused(final int lineCount) {}

  @Override
  public NotificationRemover addNotification(final com.google.gwt.dom.client.Element element) {
    getElement().insertFirst(element);
    return new NotificationRemover() {
      @Override
      public void remove() {
        element.removeFromParent();
      }
    };
  }

  /**
   * UI binder interface for this component.
   *
   * @author "Mickaël Leduque"
   */
  interface EditorViewUiBinder extends UiBinder<HTMLPanel, TextEditorPartViewImpl> {}
}
