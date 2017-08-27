/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.datasource.ide.newDatasource.view;


import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import elemental.dom.Element;
import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;
import elemental.html.SpanElement;


import org.eclipse.che.datasource.ide.newDatasource.connector.NewDatasourceConnector;
import org.eclipse.che.datasource.ide.newDatasource.presenter.NewDatasourceWizardMainPagePresenter;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.collections.Array;
import org.eclipse.che.ide.ui.tree.NodeDataAdapter;
import org.eclipse.che.ide.ui.tree.NodeRenderer;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.input.SignalEvent;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.datasource.ide.DatabaseCategoryType.AMAZON;
import static org.eclipse.che.datasource.ide.DatabaseCategoryType.GOOGLE;
import static org.eclipse.che.datasource.ide.DatabaseCategoryType.NOTCLOUD;

public class NewDatasourceWizardMainPageViewImpl implements NewDatasourceWizardMainPageView {

    private static NewDatasourceWizardMainPageViewImplUiBinder ourUiBinder = GWT.create(NewDatasourceWizardMainPageViewImplUiBinder.class);

    interface Style extends CssResource {
    }

    @UiField
    Style       style;
    @UiField
    SimplePanel datasourceCategoriesPanel;

    private Collection<NewDatasourceConnector> connectors;

    protected ActionDelegate delegate;

    private DockLayoutPanel rootElement;

    private Resources resources;

    Collection<NewDatasourceConnector> notCloudCollection;
    Collection<NewDatasourceConnector> googleCollection;
    Collection<NewDatasourceConnector> amazonCollection;

    private Tree<String> categoriesTree;

    private final Tree.Listener<String> treeEventHandler = new Tree.Listener<String>() {
        @Override
        public void onNodeAction(TreeNodeElement<String> node) {

        }

        @Override
        public void onNodeClosed(TreeNodeElement<String> node) {

        }

        @Override
        public void onNodeContextMenu(int mouseX,
                                      int mouseY,
                                      TreeNodeElement<String> node) {

        }

        @Override
        public void onNodeDragStart(TreeNodeElement<String> node,
                                    MouseEvent event) {

        }

        @Override
        public void onNodeDragDrop(TreeNodeElement<String> node,
                                   MouseEvent event) {

        }

        @Override
        public void onNodeExpanded(TreeNodeElement<String> node) {

        }

        @Override
        public void onNodeSelected(TreeNodeElement<String> node,
                                   SignalEvent event) {
            Log.info(NewDatasourceWizardMainPageViewImpl.class,"Node selected");
            String key = node.getData();
            if (key.equals(NOTCLOUD.toString())
                || key.equals(GOOGLE.toString())
                || key.equals(AMAZON.toString())) {
                delegate.onCategorySelected();
            } else {
                NewDatasourceConnector connector = null;
                for (NewDatasourceConnector ndconnector : connectors) {
                    connector = ndconnector;
                    if (connector.getTitle().equals(key)) {
                        if (delegate.connectorEnabled(connector.getId())) {
                            Log.info(NewDatasourceWizardMainPageViewImpl.class,"on connector selected");
                            delegate.onConnectorSelected(connector.getId());
                        } else {
                            categoriesTree.getSelectionModel()
                                          .clearSelections();
                        }
                    }
                }
            }
        }

        @Override
        public void onRootContextMenu(int mouseX, int mouseY) {

        }

        @Override
        public void onRootDragDrop(MouseEvent event) {

        }

        @Override
        public void onKeyboard(KeyboardEvent event) {
        }
    };

    @Inject
    public NewDatasourceWizardMainPageViewImpl(Resources resources) {
        // splitting the parent list to get the maximum size for each category
        notCloudCollection = new ArrayList<>();
        googleCollection = new ArrayList<>();
        amazonCollection = new ArrayList<>();
        this.resources = resources;
        rootElement = ourUiBinder.createAndBindUi(this);
         reset();
    }

    @Override
    public void reset() {
        datasourceCategoriesPanel.clear();
        categoriesTree = Tree.create(resources, new CategoriesDataAdapter(), new CategoriesNodeRenderer());
        datasourceCategoriesPanel.add(categoriesTree);
        com.google.gwt.dom.client.Style style = categoriesTree.asWidget().getElement().getStyle();
        style.setWidth(100, com.google.gwt.dom.client.Style.Unit.PCT);
        style.setHeight(100, com.google.gwt.dom.client.Style.Unit.PCT);
        style.setPosition(com.google.gwt.dom.client.Style.Position.RELATIVE);
        categoriesTree.setTreeEventHandler(treeEventHandler);
        categoriesTree.getModel().setRoot("");
        categoriesTree.renderTree(0);
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

    @Override
    public void setConnectors(final Collection<NewDatasourceConnector> connectors) {

        this.connectors = connectors;


        for (NewDatasourceConnector connector : connectors) {
            if (connector.getCategoryType() == NOTCLOUD) {
                notCloudCollection.add(connector);
            } else if (connector.getCategoryType() == GOOGLE) {
                googleCollection.add(connector);
            } else {
                amazonCollection.add(connector);
            }
        }
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void selectConnector(NewDatasourceConnector connector) {
        String categoryType = connector.getCategoryType().toString();
        TreeNodeElement<String> categoryNode = categoriesTree.getNode(categoryType);
        categoriesTree.expandNode(categoryNode);

        String connectorId = connector.getId();
        String connectorTitle = connector.getTitle();
        if (delegate.connectorEnabled(connectorId)) {
            categoriesTree.getSelectionModel().selectSingleNode(connectorTitle);
            delegate.onConnectorSelected(connectorId);
        } else {
            Window.alert("Cannot select connector " + connectorTitle + " - no driver available");
        }
    }

    interface NewDatasourceWizardMainPageViewImplUiBinder extends UiBinder<DockLayoutPanel, NewDatasourceWizardMainPageViewImpl> {
    }

    private class CategoriesDataAdapter implements NodeDataAdapter<String> {

        private Map<String, TreeNodeElement<String>> elements = new HashMap<>();

        @Override
        public int compare(String a, String b) {
            return 0;
        }

        @Override
        public boolean hasChildren(String data) {
            return data.equals(GOOGLE.toString()) || data.equals(NOTCLOUD.toString()) || data.equals(AMAZON.toString());
        }

        @Override
        public ArrayList<String> getChildren(String data) {
            if ("".equals(data)) {
                ArrayList<String> array = new ArrayList<String>();
                if (!notCloudCollection.isEmpty()) {
                    array.add(NOTCLOUD.toString());
                }
                if (!googleCollection.isEmpty()) {
                    array.add(GOOGLE.toString());
                }
                if (!amazonCollection.isEmpty()) {
                    array.add(AMAZON.toString());
                }
                return array;
            }
            if (NOTCLOUD.toString().equals(data)) {
                ArrayList<String> array = new ArrayList<String>();
                for (NewDatasourceConnector connector : notCloudCollection) {
                    String datasourceName = connector.getTitle();
                    array.add(datasourceName);
                }
                return array;
            }
            if (GOOGLE.toString().equals(data)) {
                ArrayList<String> array = new ArrayList<String>();
                for (NewDatasourceConnector connector : googleCollection) {
                    String datasourceName = connector.getTitle();
                    array.add(datasourceName);
                }
                return array;
            }
            if (AMAZON.toString().equals(data)) {
                ArrayList<String> array = new ArrayList<String>();
                for (NewDatasourceConnector connector : amazonCollection) {
                    String datasourceName = connector.getTitle();
                    array.add(datasourceName);
                }
                return array;
            }

            return null;
        }

        @Override
        public String getNodeId(String data) {
            return data;
        }

        @Override
        public String getNodeName(String data) {
            return data;
        }

        @Override
        public String getParent(String data) {
            return "";
        }

        @Override
        public TreeNodeElement<String> getRenderedTreeNode(String data) {
            return elements.get(data);
        }

        @Override
        public void setNodeName(String data, String name) {

        }

        @Override
        public void setRenderedTreeNode(String data, TreeNodeElement<String> renderedNode) {
            elements.put(data, renderedNode);
        }

        @Override
        public String getDragDropTarget(String data) {
            return null;
        }

        @Override
        public List<String> getNodePath(String data) {
            return PathUtils.getNodePath(this, data);
        }

        @Override
        public String getNodeByPath(String root, List<String> relativeNodePath) {
            return null;
        }
    }

    private class CategoriesNodeRenderer implements NodeRenderer<String> {

        @Override
        public Element getNodeKeyTextContainer(SpanElement treeNodeLabel) {
            return null;
        }

        @Override
        public SpanElement renderNodeContents(String data) {
            SpanElement spanElement = Elements.createSpanElement();
            if (data.equals(NOTCLOUD.toString())) {
                spanElement.getStyle().setFontWeight("bold");
                spanElement.setAttribute("id", "category-" + NOTCLOUD.getLabel());
            }
            if (data.equals(GOOGLE.toString())) {
                spanElement.getStyle().setFontWeight("bold");
                spanElement.setAttribute("id", "category-" + GOOGLE.getLabel());
            }
            if (data.equals(AMAZON.toString())) {
                spanElement.getStyle().setFontWeight("bold");
                spanElement.setAttribute("id", "category-" + AMAZON.getLabel());
            }
            for (NewDatasourceConnector connector : connectors) {
                if (data.equals(connector.getTitle())) {
                    spanElement.setAttribute("id", "connector-" + connector.getId());
                    if (!delegate.connectorEnabled(connector.getId())) {
                        spanElement.getStyle().setColor("rgb(255,155,155)");
                        spanElement.getStyle().setOpacity(0.5);
                        Elements.addClassName("tree-element-disabled", spanElement);
                        break;
                    }
                }
            }
            spanElement.setInnerHTML(data);
            return spanElement;
        }

        @Override
        public void updateNodeContents(TreeNodeElement<String> treeNode) {

        }
    }
}
