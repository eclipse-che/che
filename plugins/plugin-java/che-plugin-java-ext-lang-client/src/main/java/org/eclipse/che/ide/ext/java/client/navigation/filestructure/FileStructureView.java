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

import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.data.NodeInterceptor;
import org.eclipse.che.jdt.ls.extension.api.dto.ExtendedSymbolInformation;

/**
 * Displays a hierarchy of {@link ExtendedSymbolInformation} objects
 *
 * @author Thomas Mäder
 */
@Singleton
public final class FileStructureView
    implements View<ElementSelectionDelegate<ExtendedSymbolInformation>>, IsWidget, Focusable {
  interface FileStructureViewUiBinder extends UiBinder<Widget, FileStructureView> {}

  private static FileStructureViewUiBinder UI_BINDER = GWT.create(FileStructureViewUiBinder.class);

  private final NodeFactory nodeFactory;
  private ElementSelectionDelegate<ExtendedSymbolInformation> delegate;

  @UiField(provided = true)
  FileStructureTree tree;

  @Inject
  public FileStructureView(NodeFactory nodeFactory) {
    this.nodeFactory = nodeFactory;

    NodeStorage storage = new NodeStorage(item -> String.valueOf(item.hashCode()));
    NodeLoader loader = new NodeLoader(Collections.<NodeInterceptor>emptySet());
    tree = new FileStructureTree(storage, loader);
    UI_BINDER.createAndBindUi(this);
    tree.setAutoExpand(false);
    tree.getSelectionModel().setSelectionMode(SINGLE);
    tree.enableSpeedSearch(true);
  }

  public void setInput(List<ExtendedSymbolInformation> input) {
    tree.getNodeStorage().clear();
    for (ExtendedSymbolInformation symbol : input) {
      tree.getNodeStorage().add(nodeFactory.create(delegate, symbol));
    }
    if (!tree.getRootNodes().isEmpty()) {
      tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
    }
    tree.expandAll();
  }

  public void onShow() {
    tree.setFocus(true);
    if (!tree.getRootNodes().isEmpty()) {
      tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
    }
    tree.expandAll();
  }

  public void onClose() {
    tree.closeSpeedSearchPopup();
  }

  @Override
  public void setDelegate(ElementSelectionDelegate<ExtendedSymbolInformation> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return tree;
  }

  @Override
  public int getTabIndex() {
    return tree.getTabIndex();
  }

  @Override
  public void setAccessKey(char key) {
    tree.setAccessKey(key);
  }

  @Override
  public void setFocus(boolean focused) {
    tree.setFocus(focused);
  }

  @Override
  public void setTabIndex(int index) {
    tree.setTabIndex(index);
  }
}
