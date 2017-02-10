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
package org.eclipse.che.plugin.svn.ide.move;

import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.settings.SettingsProvider;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resources.tree.ContainerNode;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.resources.tree.SkipHiddenNodesInterceptor;
import org.eclipse.che.ide.resources.tree.SkipLeafsInterceptor;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menu.PositionController;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.SelectionModel;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.vectomatic.dom.svg.OMSVGSVGElement;

/**
 * Implementation of {@link MoveView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class MoveViewImpl extends Window implements MoveView {
    interface MoveViewImplUiBinder extends UiBinder<Widget, MoveViewImpl> {
    }

    private static MoveViewImplUiBinder uiBinder = GWT.create(MoveViewImplUiBinder.class);

    Button btnMove;
    Button btnCancel;

    @UiField
    DockLayoutPanel treeContainer;

    @UiField
    CheckBox urlCheckBox;

    @UiField
    DeckPanel deckPanel;

    @UiField
    TextBox sourceUrlTextBox;

    @UiField
    TextBox targetUrlTextBox;

    @UiField
    TextBox commentTextBox;

    @UiField
    DockLayoutPanel newNamePanel;

    @UiField
    TextBox newNameTextBox;

    @UiField(provided = true)
    SubversionExtensionResources             resources;
    @UiField(provided = true)
    SubversionExtensionLocalizationConstants constants;
    private final ResourceNode.NodeFactory nodeFactory;
    private final SettingsProvider         settingsProvider;

    private MoveView.ActionDelegate delegate;
    private Tree                    tree;
    private OMSVGSVGElement         alertMarker;

    private static final String PLACEHOLDER       = "placeholder";
    private static final String PLACEHOLDER_DUMMY = "https://subversion.site.com/svn/sht_site/trunk";

    @Inject
    public MoveViewImpl(SubversionExtensionResources resources,
                        SubversionExtensionLocalizationConstants constants,
                        SkipHiddenNodesInterceptor skipHiddenNodesInterceptor,
                        SkipLeafsInterceptor skipLeafsInterceptor,
                        ResourceNode.NodeFactory nodeFactory,
                        SettingsProvider settingsProvider) {
        this.resources = resources;
        this.constants = constants;
        this.nodeFactory = nodeFactory;
        this.settingsProvider = settingsProvider;

        this.ensureDebugId("svn-move-window");
        this.setTitle(constants.moveViewTitle());

        this.setWidget(uiBinder.createAndBindUi(this));

        btnCancel = createButton(constants.buttonCancel(), "svn-move-cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        addButtonToFooter(btnCancel);

        btnMove = createButton(constants.moveButton(), "svn-move-move", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onMoveClicked();
            }
        });
        addButtonToFooter(btnMove);

        alertMarker = resources.alert().getSvg();
        alertMarker.getStyle().setWidth(22, Style.Unit.PX);
        alertMarker.getStyle().setHeight(22, Style.Unit.PX);
        alertMarker.getStyle().setMargin(10, Style.Unit.PX);
        getFooter().getElement().appendChild(alertMarker.getElement());
        alertMarker.getStyle().setVisibility(Style.Visibility.HIDDEN);

        tree = new Tree(new NodeStorage(), new NodeLoader(Sets.newHashSet(skipHiddenNodesInterceptor, skipLeafsInterceptor)));
        tree.getSelectionModel().setSelectionMode(SelectionModel.Mode.SINGLE);

        treeContainer.add(tree);

        sourceUrlTextBox.getElement().setAttribute(PLACEHOLDER, PLACEHOLDER_DUMMY);
        targetUrlTextBox.getElement().setAttribute(PLACEHOLDER, PLACEHOLDER_DUMMY);
        commentTextBox.getElement().setAttribute(PLACEHOLDER, "Comment...");

        urlCheckBox.setValue(false, true);
        deckPanel.showWidget(0);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void onClose() {
        hide();
    }

    /** {@inheritDoc} */
    @Override
    public void setProject(Project project) {
        final ContainerNode node = nodeFactory.newContainerNode(project, settingsProvider.getSettings());

        tree.getNodeStorage().clear();
        tree.getNodeStorage().add(node);

        tree.setExpanded(node, true);
    }

    /** {@inheritDoc} */
    @Override
    public void showErrorMarker(String message) {
        alertMarker.getStyle().setVisibility(Style.Visibility.VISIBLE);

        Tooltip.create((elemental.dom.Element)alertMarker.getElement(),
                       PositionController.VerticalAlign.TOP,
                       PositionController.HorizontalAlign.MIDDLE,
                       message);

        btnMove.setEnabled(false);
    }

    /** {@inheritDoc} */
    @Override
    public void hideErrorMarker() {
        alertMarker.getStyle().setVisibility(Style.Visibility.HIDDEN);

        btnMove.setEnabled(true);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isURLSelected() {
        return urlCheckBox.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public String getSourceUrl() {
        return sourceUrlTextBox.getText();
    }

    /** {@inheritDoc} */
    @Override
    public String getTargetUrl() {
        return targetUrlTextBox.getText();
    }

    /** {@inheritDoc} */
    @Override
    public Resource getDestinationNode() {
        final Node node = tree.getSelectionModel().getSelectedNodes().get(0);

        if (node instanceof ResourceNode) {
            return ((ResourceNode)node).getData();
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void onShow(boolean singleSelectedItem) {
        newNamePanel.setVisible(singleSelectedItem);
        newNameTextBox.setText(null);
        show();
    }

    /** {@inheritDoc} */
    @Override
    public String getComment() {
        return commentTextBox.getText();
    }

    @UiHandler({"sourceUrlTextBox", "targetUrlTextBox", "commentTextBox"})
    @SuppressWarnings("unused")
    public void onUrlFieldsChanged(KeyUpEvent event) {
        delegate.onUrlsChanged();
    }

    @UiHandler("urlCheckBox")
    @SuppressWarnings("unused")
    public void onUrlCheckBoxClicked(ClickEvent event) {
        if (isURLSelected()) {
            sourceUrlTextBox.setText(null);
            targetUrlTextBox.setText(null);
            delegate.onUrlsChanged();
            deckPanel.showWidget(1);
        } else {
            btnMove.setEnabled(true);
            deckPanel.showWidget(0);
        }
    }
}
