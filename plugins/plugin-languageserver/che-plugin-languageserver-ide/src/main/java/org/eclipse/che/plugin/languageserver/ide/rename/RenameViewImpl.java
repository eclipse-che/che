/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

package org.eclipse.che.plugin.languageserver.ide.rename;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import elemental.dom.Element;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.languageserver.shared.model.ExtendedTextDocumentEdit;
import org.eclipse.che.api.languageserver.shared.model.ExtendedWorkspaceEdit;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.autocomplete.AutoCompleteResources;
import org.eclipse.che.ide.api.data.tree.NodeInterceptor;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ui.SplitterFancyUtil;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.ui.list.SimpleList.ListEventDelegate;
import org.eclipse.che.ide.ui.list.SimpleList.ListItemRenderer;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.plugin.languageserver.ide.rename.RenameView.ActionDelegate;
import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameProject;
import org.eclipse.che.plugin.languageserver.ide.rename.node.RenameNodeFactory;

/** */
public class RenameViewImpl extends BaseView<ActionDelegate> implements RenameView {

  private final Tree tree;
  private final RenameNodeFactory nodeFactory;
  private SplitLayoutPanel splitLayoutPanel;
  private DockLayoutPanel treeDock;

  private FlowPanel editPanel;

  private SimpleList<String> lsList;
  private Map<String, ExtendedWorkspaceEdit> editMap;
  private String currentLSId;
  @Inject
  public RenameViewImpl(
      PartStackUIResources resources,
      SplitterFancyUtil splitterFancyUtil,
      Resources coreRes,
      AutoCompleteResources autoCompleteResources,
      RenameNodeFactory nodeFactory) {
    super(resources);
    this.nodeFactory = nodeFactory;
    splitLayoutPanel = new SplitLayoutPanel(1);
    splitLayoutPanel.setSize("100%", "100%");
    editPanel = new FlowPanel();
    splitLayoutPanel.addWest(editPanel, 185);
    setContentWidget(splitLayoutPanel);

    Element itemHolder = Elements.createDivElement();
    itemHolder.setClassName(autoCompleteResources.autocompleteComponentCss().items());
    editPanel.getElement().appendChild(((com.google.gwt.dom.client.Element) itemHolder));

    lsList =
        SimpleList.create(
            editPanel.getElement().cast(),
            editPanel.getElement().cast(),
            itemHolder,
            coreRes.defaultSimpleListCss(),
            new ListItemRenderer<String>() {
              @Override
              public void render(Element listItemBase, String itemData) {
                listItemBase.setInnerText(itemData);
              }
            },
            new ListEventDelegate<String>() {
              @Override
              public void onListItemClicked(Element listItemBase, String itemData) {
                selectedEditSet(itemData);
              }

              @Override
              public void onListItemDoubleClicked(Element listItemBase, String itemData) {}
            });
    splitterFancyUtil.tuneSplitter(splitLayoutPanel);
    //    splitLayoutPanel.setWidgetHidden(editPanel, true);

    treeDock = new DockLayoutPanel(Unit.PX);
    FlowPanel buttonPanel = new FlowPanel();
    treeDock.addSouth(buttonPanel, 25);
    Button refactorButton = new Button();
    refactorButton.setText("Do Rename");

    splitLayoutPanel.add(treeDock);

    Button cancelButton = new Button();
    cancelButton.setText("Cancel");

    buttonPanel.add(refactorButton);
    buttonPanel.add(cancelButton);

    NodeStorage storage =
        new NodeStorage((NodeUniqueKeyProvider) item -> String.valueOf(item.hashCode()));
    NodeLoader loader = new NodeLoader(Collections.emptySet());
    tree = new Tree(storage, loader);
    treeDock.add(tree);
  }

  private void selectedEditSet(String editSetId) {
    if (editSetId.equals(currentLSId)) {
      return;
    }
    if (editMap.containsKey(editSetId)) {
      ExtendedWorkspaceEdit workspaceEdit = editMap.get(editSetId);
      tree.getNodeStorage().clear();
      List<ExtendedTextDocumentEdit> documentChanges = workspaceEdit.getDocumentChanges();
      List<RenameProject> projects = delegate.convert(documentChanges);
      for (RenameProject project : projects) {
        tree.getNodeStorage().add(nodeFactory.create(project));
      }
      tree.expandAll();
    }
  }

  @Override
  public void showRenameResult(Map<String, ExtendedWorkspaceEdit> editMap) {
    this.editMap = editMap;
    currentLSId = null;
    lsList.render(new ArrayList<>(editMap.keySet()));
    lsList.getSelectionModel().selectNext();
    selectedEditSet(lsList.getSelectionModel().getSelectedItem());
  }
}
