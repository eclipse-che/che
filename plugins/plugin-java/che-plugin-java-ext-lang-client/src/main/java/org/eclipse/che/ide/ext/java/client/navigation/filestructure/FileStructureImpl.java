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
package org.eclipse.che.ide.ext.java.client.navigation.filestructure;

import static com.google.common.collect.Iterables.all;
import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;

import com.google.common.base.Predicate;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.navigation.factory.NodeFactory;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ui.smartTree.KeyboardNavigationHandler;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.NodeInterceptor;
import org.eclipse.che.ide.ui.window.Window;

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

  private ActionDelegate delegate;

  @UiField DockLayoutPanel treeContainer;
  @UiField Label showInheritedLabel;

  @UiField(provided = true)
  final JavaLocalizationConstant locale;

  private Predicate<Node> LEAFS =
      new Predicate<Node>() {
        @Override
        public boolean apply(Node input) {
          return input.isLeaf();
        }
      };

  @Inject
  public FileStructureImpl(NodeFactory nodeFactory, JavaLocalizationConstant locale) {
    super(false);
    this.nodeFactory = nodeFactory;
    this.locale = locale;
    setWidget(UI_BINDER.createAndBindUi(this));

    NodeStorage storage =
        new NodeStorage(
            new NodeUniqueKeyProvider() {
              @Override
              public String getKey(@NotNull Node item) {
                return String.valueOf(item.hashCode());
              }
            });
    NodeLoader loader = new NodeLoader(Collections.<NodeInterceptor>emptySet());
    tree = new Tree(storage, loader);
    tree.setAutoExpand(false);
    tree.getSelectionModel().setSelectionMode(SINGLE);

    KeyboardNavigationHandler handler =
        new KeyboardNavigationHandler() {
          @Override
          public void onEnter(NativeEvent evt) {
            hide();
          }
        };
    tree.addDomHandler(
        new DoubleClickHandler() {
          @Override
          public void onDoubleClick(DoubleClickEvent event) {
            if (all(tree.getSelectionModel().getSelectedNodes(), LEAFS)) {
              hide();
            }
          }
        },
        DoubleClickEvent.getType());

    handler.bind(tree);

    treeContainer.add(tree);
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

  /** {@inheritDoc} */
  @Override
  public void close() {
    hide();
  }

  /** {@inheritDoc} */
  @Override
  public void show() {
    super.show(tree);
    if (!tree.getRootNodes().isEmpty()) {
      tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
    }
    tree.expandAll();
  }

  /** {@inheritDoc} */
  @Override
  public void hide() {
    super.hide();
    delegate.onEscapeClicked();
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }
}
