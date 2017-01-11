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
package org.eclipse.che.plugin.svn.ide.copy;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.settings.SettingsProvider;
import org.eclipse.che.ide.api.resources.Project;
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
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.vectomatic.dom.svg.OMSVGSVGElement;

import java.util.List;


/**
 * Implementation of {@link CopyView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class CopyViewImpl extends Window implements CopyView {
    interface CopyViewImplUiBinder extends UiBinder<Widget, CopyViewImpl> {
    }

    private static CopyViewImplUiBinder uiBinder = GWT.create(CopyViewImplUiBinder.class);

    Button btnCopy;
    Button btnCancel;

    @UiField
    DockLayoutPanel treeContainer;

    @UiField
    TextBox newNameTextBox;

    @UiField
    CheckBox sourceCheckBox;

    @UiField
    CheckBox targetCheckBox;

    @UiField
    DeckPanel deckPanel;

    @UiField
    Label sourceLabel;

    @UiField
    TextBox targetUrlTextBox;

    @UiField
    TextBox sourceTextBox;

    @UiField
    TextBox commentTextBox;

    @UiField
    DeckPanel commentDeckPanel;

    @UiField(provided = true)
    SubversionExtensionResources             resources;
    @UiField(provided = true)
    SubversionExtensionLocalizationConstants constants;
    private final ResourceNode.NodeFactory nodeFactory;
    private final SettingsProvider         settingsProvider;

    private CopyView.ActionDelegate delegate;
    private Tree                    tree;
    private OMSVGSVGElement         alertMarker;

    private static final String PLACEHOLDER       = "placeholder";
    private static final String PLACEHOLDER_DUMMY = "https://subversion.site.com/svn/sht_site/trunk";

    private static final int ANIMATION_DURATION = 350;

    @Inject
    public CopyViewImpl(SubversionExtensionResources resources,
                        SubversionExtensionLocalizationConstants constants,
                        SkipHiddenNodesInterceptor skipHiddenNodesInterceptor,
                        SkipLeafsInterceptor skipLeafsInterceptor,
                        ResourceNode.NodeFactory nodeFactory,
                        SettingsProvider settingsProvider) {
        this.resources = resources;
        this.constants = constants;
        this.nodeFactory = nodeFactory;
        this.settingsProvider = settingsProvider;

        this.ensureDebugId("svn-copy-window");

        this.setWidget(uiBinder.createAndBindUi(this));

        btnCancel = createButton(constants.buttonCancel(), "svn-copy-cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        addButtonToFooter(btnCancel);

        btnCopy = createButton("Copy", "svn-copy-copy", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCopyClicked();
            }
        });
        addButtonToFooter(btnCopy);

        alertMarker = resources.alert().getSvg();
        alertMarker.getStyle().setWidth(22, Style.Unit.PX);
        alertMarker.getStyle().setHeight(22, Style.Unit.PX);
        alertMarker.getStyle().setMargin(10, Style.Unit.PX);
        getFooter().getElement().appendChild(alertMarker.getElement());
        alertMarker.getStyle().setVisibility(Style.Visibility.HIDDEN);

        tree = new Tree(new NodeStorage(), new NodeLoader(Sets.newHashSet(skipHiddenNodesInterceptor, skipLeafsInterceptor)));

        tree.getSelectionModel().setSelectionMode(SelectionModel.Mode.SINGLE);

        tree.getSelectionModel().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                final List<Node> selection = event.getSelection();

                if (selection == null || selection.isEmpty()) {
                    return;
                }

                final Node node = selection.get(0);

                if (node instanceof ResourceNode) {
                    delegate.onNodeSelected(((ResourceNode)node).getData());
                }

            }
        });

        treeContainer.add(tree);
    }

    /** {@inheritDoc} */
    @Override
    public void setDialogTitle(String title) {
        super.setTitle(title);
    }

    /** {@inheritDoc} */
    @Override
    public void setNewName(String name) {
        newNameTextBox.getElement().setAttribute(PLACEHOLDER, name);
    }

    /** {@inheritDoc} */
    @Override
    public String getNewName() {
        if (!Strings.isNullOrEmpty(newNameTextBox.getText())) {
            return newNameTextBox.getText();
        } else {
            return newNameTextBox.getElement().getAttribute(PLACEHOLDER);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getSourcePath() {
        return sourceTextBox.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setSourcePath(String path, boolean editable) {
        sourceTextBox.setText(path);
        sourceTextBox.setEnabled(editable);
        sourceTextBox.getElement().setAttribute(PLACEHOLDER, PLACEHOLDER_DUMMY);
    }

    @UiHandler("newNameTextBox")
    @SuppressWarnings("unused")
    public void onNewNameFieldChanged(KeyUpEvent event) {
        delegate.onNewNameChanged(newNameTextBox.getText());
        setComment(getNewName());
    }

    @UiHandler("targetCheckBox")
    @SuppressWarnings("unused")
    public void onDestinationUrlCheckBoxActivated(ClickEvent event) {
        new SlideAnimation()
                .showWidget(deckPanel.getWidget(targetCheckBox.getValue() ? 0 : 1), deckPanel.getWidget(targetCheckBox.getValue() ? 1 : 0));
        delegate.onTargetCheckBoxChanged();

        new SlideAnimation().showWidget(commentDeckPanel.getWidget(targetCheckBox.getValue() ? 1 : 0),
                                        commentDeckPanel.getWidget(targetCheckBox.getValue() ? 0 : 1));

    }

    @UiHandler("sourceCheckBox")
    @SuppressWarnings("unused")
    public void onSourceUrlCheckBoxActivated(ClickEvent event) {
        sourceLabel.setText(sourceCheckBox.getValue() ? "URL:" : "Path:");
        delegate.onSourceCheckBoxChanged();

        setComment(getNewName());
    }

    @UiHandler("targetUrlTextBox")
    @SuppressWarnings("unused")
    public void onTargetUrlTextBoxChanged(KeyUpEvent event) {
        delegate.onTargetUrlChanged();
    }

    @UiHandler("sourceTextBox")
    @SuppressWarnings("unused")
    public void onSourceTextBoxChanged(KeyUpEvent event) {
        delegate.onSourcePathChanged();

        final String[] parts = sourceTextBox.getText().trim().split("/");
        final String projectName = parts[parts.length - 1];

        newNameTextBox.getElement().setAttribute(PLACEHOLDER, Strings.isNullOrEmpty(projectName) ? "name" : projectName);
        setComment(Strings.isNullOrEmpty(projectName) ? "name" : projectName);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(final CopyView.ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void setProjectNode(Project project) {
        final ContainerNode node = nodeFactory.newContainerNode(project, settingsProvider.getSettings());

        tree.getNodeStorage().clear();
        tree.getNodeStorage().add(node);

        tree.setExpanded(node, true);
    }

    /** {@inheritDoc} */
    @Override
    public void show() {
        new SlideAnimation().showWidget(null, deckPanel.getWidget(0));

        sourceLabel.setText("Path:");
        newNameTextBox.setText(null);
        sourceCheckBox.setValue(false);

        targetCheckBox.setValue(false);

        targetUrlTextBox.setText(null);
        targetUrlTextBox.getElement().setAttribute(PLACEHOLDER, PLACEHOLDER_DUMMY);

        super.show();
    }

    /** {@inheritDoc} */
    @Override
    public void showErrorMarker(String message) {
        alertMarker.getStyle().setVisibility(Style.Visibility.VISIBLE);

        Tooltip.create((elemental.dom.Element)alertMarker.getElement(),
                       PositionController.VerticalAlign.TOP,
                       PositionController.HorizontalAlign.MIDDLE,
                       message);

        btnCopy.setEnabled(false);
    }

    /** {@inheritDoc} */
    @Override
    public void hideErrorMarker() {
        alertMarker.getStyle().setVisibility(Style.Visibility.HIDDEN);

        btnCopy.setEnabled(true);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSourceCheckBoxSelected() {
        return sourceCheckBox.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isTargetCheckBoxSelected() {
        return targetCheckBox.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public String getTargetUrl() {
        return targetUrlTextBox.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setComment(String comment) {
        commentTextBox.getElement().setAttribute(PLACEHOLDER, "copy '" + comment + "'");
    }

    /** {@inheritDoc} */
    @Override
    public String getComment() {
        if (!Strings.isNullOrEmpty(commentTextBox.getText())) {
            return commentTextBox.getText();
        } else {
            return commentTextBox.getElement().getAttribute(PLACEHOLDER);
        }
    }

    private class SlideAnimation extends Animation {
        /**
         * The {@link com.google.gwt.dom.client.Element} holding the {@link Widget} with a lower index.
         */
        private Element container1 = null;

        /**
         * The {@link Element} holding the {@link Widget} with a higher index.
         */
        private Element container2 = null;

        /**
         * A boolean indicating whether container1 is growing or shrinking.
         */
        private boolean growing = false;

        /**
         * The fixed height of a {@link com.google.gwt.user.client.ui.TabPanel} in pixels. If the {@link com.google.gwt.user.client.ui
         * .TabPanel}
         * does not have a fixed height, this will be set to -1.
         */
        private int fixedHeight = -1;

        /**
         * The old {@link Widget} that is being hidden.
         */
        private Widget oldWidget = null;

        /**
         * Switch to a new {@link Widget}.
         *
         * @param oldWidget
         *         the {@link Widget} to hide
         * @param newWidget
         *         the {@link Widget} to show
         */
        public void showWidget(Widget oldWidget, Widget newWidget) {
            // Immediately complete previous animation
            cancel();

            // Get the container and index of the new widget
            Element newContainer = DOM.getParent(newWidget.getElement());
            int newIndex = DOM.getChildIndex(DOM.getParent(newContainer),
                                             newContainer);

            // If we aren't showing anything, don't bother with the animation
            if (oldWidget == null) {
                UIObject.setVisible(newContainer, true);
                newWidget.setVisible(true);
                return;
            }
            this.oldWidget = oldWidget;

            // Get the container and index of the old widget
            Element oldContainer = DOM.getParent(oldWidget.getElement());
            int oldIndex = DOM.getChildIndex(DOM.getParent(oldContainer),
                                             oldContainer);

            // Figure out whether to grow or shrink the container
            if (newIndex > oldIndex) {
                container1 = oldContainer;
                container2 = newContainer;
                growing = false;
            } else {
                container1 = newContainer;
                container2 = oldContainer;
                growing = true;
            }

            // Figure out if the deck panel has a fixed height
            com.google.gwt.dom.client.Element deckElem = container1.getParentElement();
            int deckHeight = deckElem.getOffsetHeight();
            if (growing) {
                fixedHeight = container2.getOffsetHeight();
                container2.getStyle().setPropertyPx("height", Math.max(1, fixedHeight - 1));
            } else {
                fixedHeight = container1.getOffsetHeight();
                container1.getStyle().setPropertyPx("height", Math.max(1, fixedHeight - 1));
            }
            if (deckElem.getOffsetHeight() != deckHeight) {
                fixedHeight = -1;
            }

            // Only scope to the deck if it's fixed height, otherwise it can affect
            // the rest of the page, even if it's not visible to the user.
            run(ANIMATION_DURATION, fixedHeight == -1 ? null : deckElem);


            // We call newWidget.setVisible(true) immediately after showing the
            // widget's container so users can delay render their widget. Ultimately,
            // we should have a better way of handling this, but we need to call
            // setVisible for legacy support.
            newWidget.setVisible(true);
        }

        @Override
        protected void onComplete() {
            if (growing) {
                container1.getStyle().setProperty("height", "100%");
                UIObject.setVisible(container1, true);
                UIObject.setVisible(container2, false);
                container2.getStyle().setProperty("height", "100%");
            } else {
                UIObject.setVisible(container1, false);
                container1.getStyle().setProperty("height", "100%");
                container2.getStyle().setProperty("height", "100%");
                UIObject.setVisible(container2, true);
            }
            container1.getStyle().setProperty("overflow", "visible");
            container2.getStyle().setProperty("overflow", "visible");
            container1 = null;
            container2 = null;
            hideOldWidget();
        }

        @Override
        protected void onStart() {
            // Start the animation
            container1.getStyle().setProperty("overflow", "hidden");
            container2.getStyle().setProperty("overflow", "hidden");
            onUpdate(0.0);
            UIObject.setVisible(container1, true);
            UIObject.setVisible(container2, true);
        }

        @Override
        protected void onUpdate(double progress) {
            if (!growing) {
                progress = 1.0 - progress;
            }

            // Container1 expands (shrinks) to its target height
            int height1;
            int height2;
            if (fixedHeight == -1) {
                height1 = (int)(progress * container1.getPropertyInt("scrollHeight"));
                height2 = (int)((1.0 - progress) * container2.getPropertyInt("scrollHeight"));
            } else {
                height1 = (int)(progress * fixedHeight);
                height2 = fixedHeight - height1;
            }

            // Issue 2339: If the height is 0px, IE7 will display the entire content
            // widget instead of hiding it completely.
            if (height1 == 0) {
                height1 = 1;
                height2 = Math.max(1, height2 - 1);
            } else if (height2 == 0) {
                height2 = 1;
                height1 = Math.max(1, height1 - 1);
            }
            container1.getStyle().setProperty("height", height1 + "px");
            container2.getStyle().setProperty("height", height2 + "px");
        }

        /**
         * Hide the old widget when the animation completes.
         */
        private void hideOldWidget() {
            // Issue 2510: Hiding the widget isn't necessary because we hide its
            // wrapper, but its in here for legacy support.
            oldWidget.setVisible(false);
            oldWidget = null;
        }
    }
}
