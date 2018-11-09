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
package org.eclipse.che.ide.ext.java.client.navigation.filestructure;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.events.Event;
import java.util.List;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.java.client.JavaExtension;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;
import org.eclipse.che.ide.util.input.SignalEvent;
import org.eclipse.che.ide.util.input.SignalEventUtils;
import org.eclipse.che.jdt.ls.extension.api.dto.ExtendedSymbolInformation;

/**
 * A window showing a java specific file structure
 *
 * @author Thomas Mäder
 */
@Singleton
public final class FileStructureWindow extends Window
    implements View<ElementSelectionDelegate<ExtendedSymbolInformation>> {
  interface FileStructureWindowUiBinder extends UiBinder<Widget, FileStructureWindow> {}

  private static FileStructureWindowUiBinder UI_BINDER =
      GWT.create(FileStructureWindowUiBinder.class);
  private final FileStructureView view;
  private final ActionManager actionManager;
  private final PresentationFactory presentationFactor;
  private final KeyBindingAgent keyBindingAgent;

  @UiField DockLayoutPanel treeContainer;
  @UiField Label showInheritedLabel;

  @UiField(provided = true)
  final JavaLocalizationConstant locale;

  private ElementSelectionDelegate<ExtendedSymbolInformation> delegate;

  @Inject
  public FileStructureWindow(
      NodeFactory nodeFactory,
      ActionManager actionManager,
      PresentationFactory presentationFactor,
      KeyBindingAgent keyBindingAgent,
      JavaLocalizationConstant locale) {
    super();
    this.actionManager = actionManager;
    this.presentationFactor = presentationFactor;
    this.keyBindingAgent = keyBindingAgent;
    this.locale = locale;
    setWidget(UI_BINDER.createAndBindUi(this));
    view = new FileStructureView(nodeFactory);

    treeContainer.add(view);
    ensureDebugId("file-structure");
  }

  public void setShowInherited(boolean on) {
    showInheritedLabel.setText(
        on ? locale.hideInheritedMembersLabel() : locale.showInheritedMembersLabel());
  }

  public void setInput(String title, List<ExtendedSymbolInformation> input) {
    setTitle(title);
    view.setInput(input);
  }

  @Override
  protected void onShow() {
    view.onShow();
  }

  @Override
  public void setDelegate(ElementSelectionDelegate<ExtendedSymbolInformation> delegate) {
    view.setDelegate(delegate);
    this.delegate = delegate;
  }

  @Override
  protected void onHide() {
    view.onClose();
    delegate.onCancel();
  }

  @Override
  public void onKeyPress(NativeEvent evt) {
    handleKey(evt);
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    handleKey(evt);
  }

  private void handleKey(NativeEvent event) {
    SignalEvent signalEvent = SignalEventUtils.create((Event) event, false);
    CharCodeWithModifiers keyBinding =
        keyBindingAgent.getKeyBinding(JavaExtension.JAVA_CLASS_STRUCTURE);
    if (signalEvent == null || keyBinding == null) {
      return;
    }
    int digest = CharCodeWithModifiers.computeKeyDigest(signalEvent);
    if (digest == keyBinding.getKeyDigest()) {
      Action action = actionManager.getAction(JavaExtension.JAVA_CLASS_STRUCTURE);
      if (action != null) {
        ActionEvent e = new ActionEvent(presentationFactor.getPresentation(action), actionManager);
        action.update(e);

        if (e.getPresentation().isEnabled()) {
          event.preventDefault();
          event.stopPropagation();
          action.actionPerformed(e);
        }
      }
    }
  }
}
