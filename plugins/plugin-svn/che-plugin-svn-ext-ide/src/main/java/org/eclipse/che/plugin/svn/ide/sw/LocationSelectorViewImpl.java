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
package org.eclipse.che.plugin.svn.ide.sw;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.KeyboardNavigationHandler;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;

import java.util.List;

import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;

/**
 * Implementation of {@link LocationSelectorView}.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class LocationSelectorViewImpl extends Window implements LocationSelectorView {
    interface LocationSelectorViewImplUiBinder extends UiBinder<Widget, LocationSelectorViewImpl> {}

    private static LocationSelectorViewImplUiBinder uiBinder = GWT.create(LocationSelectorViewImplUiBinder.class);

    private Tree           tree;
    private ActionDelegate delegate;

    Button acceptButton;
    Button cancelButton;

    @UiField
    DockLayoutPanel treeContainer;

    @Inject
    public LocationSelectorViewImpl(SubversionExtensionLocalizationConstants constants) {
        this.setTitle(constants.switchSelectLocationTitle());
        this.setWidget(uiBinder.createAndBindUi(this));

        NodeLoader loader = new NodeLoader();
        NodeStorage nodeStorage = new NodeStorage();

        tree = new Tree(nodeStorage, loader);
        tree.setAutoSelect(true);
        tree.getSelectionModel().setSelectionMode(SINGLE);
        treeContainer.add(tree);

        KeyboardNavigationHandler handler = new KeyboardNavigationHandler() {
            @Override
            public void onEnter(NativeEvent evt) {
                evt.preventDefault();
                acceptButtonClicked();
            }
        };

        handler.bind(tree);

        cancelButton = createButton(constants.buttonCancel(), "select-location-cancel-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        addButtonToFooter(cancelButton);

        acceptButton = createPrimaryButton(constants.buttonSelect(), "select-location-select-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                acceptButtonClicked();
            }
        });
        addButtonToFooter(acceptButton);
    }

    @Override
    protected void onEnterClicked() {
        if (isWidgetFocused(acceptButton)) {
            acceptButtonClicked();
            return;
        }

        if (isWidgetFocused(cancelButton)) {
            hide();
        }
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void showWindow() {
        super.show(tree);
    }

    @Override
    public void setRootNode(SvnNode rootNode) {
        tree.getNodeStorage().clear();
        tree.getNodeStorage().add(rootNode);
        tree.setExpanded(rootNode, true);
    }

    private void acceptButtonClicked() {
        List<Node> nodes = tree.getSelectionModel().getSelectedNodes();
        if (nodes.isEmpty()) {
            return;
        }

        Node selectedNode = nodes.get(0);

        if (selectedNode instanceof SvnNode) {
            delegate.setSelectedNode((SvnNode)selectedNode);
        }

        hide();
    }
}
