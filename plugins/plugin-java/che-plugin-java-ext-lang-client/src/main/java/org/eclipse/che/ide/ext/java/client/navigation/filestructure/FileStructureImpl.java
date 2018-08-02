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

import static com.google.common.collect.Iterables.all;
import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;

import com.google.common.base.Predicate;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.events.Event;
import java.util.Collections;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.ext.java.client.JavaExtension;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.navigation.factory.NodeFactory;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;
import org.eclipse.che.ide.util.input.SignalEvent;
import org.eclipse.che.ide.util.input.SignalEventUtils;

/**
 * Implementation of {@link FileStructure} view.
 *
 * @author Valeriy Svydenko
 */
@Singleton
final class FileStructureImpl extends Window implements FileStructure {
  interface FileStructureImplUiBinder extends UiBinder<Widget, FileStructureImpl> {}

  private static FileStructureImplUiBinder UI_BINDER = GWT.create(FileStructureImplUiBinder.class);

  private final NodeFactory nodeFactory;
  private final Tree tree;

  private final ActionManager actionManager;
  private final PresentationFactory presentationFactory;
  private final KeyBindingAgent keyBindingAgent;

  private ActionDelegate delegate;

  @UiField DockLayoutPanel treeContainer;
  @UiField Label showInheritedLabel;

  @UiField(provided = true)
  final JavaLocalizationConstant locale;

  private Predicate<Node> LEAFS = Node::isLeaf;

  @Inject
  public FileStructureImpl(
      NodeFactory nodeFactory,
      JavaLocalizationConstant locale,
      ActionManager actionManager,
      PresentationFactory presentationFactory,
      KeyBindingAgent keyBindingAgent) {
    this.nodeFactory = nodeFactory;
    this.locale = locale;
    this.actionManager = actionManager;
    this.presentationFactory = presentationFactory;
    this.keyBindingAgent = keyBindingAgent;

    setWidget(UI_BINDER.createAndBindUi(this));

    NodeStorage storage =
        new NodeStorage((NodeUniqueKeyProvider) item -> String.valueOf(item.hashCode()));
    NodeLoader loader = new NodeLoader(Collections.emptySet());
    tree = new Tree(storage, loader);
    tree.setAutoExpand(false);
    tree.getSelectionModel().setSelectionMode(SINGLE);

    tree.addDomHandler(
        event -> {
          if (all(tree.getSelectionModel().getSelectedNodes(), LEAFS)) {
            hide();
          }
        },
        DoubleClickEvent.getType());

    treeContainer.add(tree);

    tree.enableSpeedSearch(true);
    ensureDebugId("file-structure");
  }

  /** {@inheritDoc} */
  @Override
  public void setStructure(CompilationUnit compilationUnit, boolean showInheritedMembers) {
    showInheritedLabel.setText(
        showInheritedMembers
            ? locale.hideInheritedMembersLabel()
            : locale.showInheritedMembersLabel());
    tree.getNodeStorage().clear();
    tree.getNodeStorage()
        .add(
            nodeFactory.create(
                compilationUnit.getTypes().get(0), compilationUnit, showInheritedMembers, false));
  }

  @Override
  public void setTitleCaption(String title) {
    setTitle(title);
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    hide();
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    show(tree);
  }

  @Override
  protected void onShow() {
    if (!tree.getRootNodes().isEmpty()) {
      tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
    }
    tree.expandAll();
  }

  @Override
  protected void onHide() {
    tree.closeSpeedSearchPopup();
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return super.asWidget();
  }

  @Override
  public void onKeyPress(NativeEvent evt) {
    handleKey(evt);
  }

  @Override
  public void onEscPress(NativeEvent evt) {
    delegate.onEscapeClicked();
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
        ActionEvent e = new ActionEvent(presentationFactory.getPresentation(action), actionManager);
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
