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
package org.eclipse.che.plugin.svn.ide.merge;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
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
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.data.tree.Node;
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
 * An implementation of MergeView, represented as popup modal dialog.
 */
@Singleton
public class MergeViewImpl extends Window implements MergeView {

    /** UI binder */
    interface CopyViewImplUiBinder extends UiBinder<Widget, MergeViewImpl> {
    }

    /** UI binder instance */
    private static CopyViewImplUiBinder uiBinder = GWT.create(CopyViewImplUiBinder.class);

    /** Localization constants. */
    @UiField(provided = true)
    SubversionExtensionLocalizationConstants constants;

    /** Bundled resources. */
    @UiField(provided = true)
    SubversionExtensionResources resources;

    /** Delegate to perform actions */
    private ActionDelegate delegate;

    /** Target text box. */
    @UiField
    TextBox targetTextBox;

    @UiField
    DeckPanel deckPanel;

    /** Source URL check box. */
    @UiField
    CheckBox sourceURLCheckBox;

    /** Source URl text box. */
    @UiField
    TextBox sourceUrlTextBox;

    private Tree tree;

    @UiField
    DockLayoutPanel treeContainer;

    /** Merge button */
    private Button mergeButton;

    /** Cancel button */
    private Button cancelButton;

    /** Attention icon. */
    private OMSVGSVGElement alertMarker;

    /* Default constructor creating an instance of this MergeViewImpl */
    @Inject
    public MergeViewImpl(SubversionExtensionLocalizationConstants constants,
                         SubversionExtensionResources resources) {
        this.constants = constants;
        this.resources = resources;


        ensureDebugId("plugin-svn merge-dialog");
        setWidget(uiBinder.createAndBindUi(this));
        setTitle(constants.mergeDialogTitle());

        mergeButton = createButton(constants.buttonMerge(), "plugin-svn-merge-dialog-merge-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {delegate.mergeClicked();}
        });
        mergeButton.addStyleName(Window.resources.windowCss().button());
        addButtonToFooter(mergeButton);

        cancelButton = createButton(constants.buttonCancel(), "plugin-svn-merge-dialog-cancel-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {delegate.cancelClicked();}
        });
        addButtonToFooter(cancelButton);

        alertMarker = resources.alert().getSvg();
        alertMarker.getStyle().setWidth(22, Style.Unit.PX);
        alertMarker.getStyle().setHeight(22, Style.Unit.PX);
        alertMarker.getStyle().setMargin(10, Style.Unit.PX);
        getFooter().getElement().appendChild(alertMarker.getElement());
        alertMarker.getStyle().setVisibility(Style.Visibility.HIDDEN);

        targetTextBox.setEnabled(false);

        tree = new Tree(new NodeStorage(), new NodeLoader());
        tree.getSelectionModel().setSelectionMode(SelectionModel.Mode.SINGLE);
        tree.getSelectionModel().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                final List<Node> selection = event.getSelection();

                if (selection == null || selection.isEmpty()) {
                    return;
                }

                delegate.onNodeSelected(selection.get(0));
            }
        });

        treeContainer.add(tree);

    }

    @Override
    public void setRootNode(Node node) {
        tree.getNodeStorage().clear();
        tree.getNodeStorage().add(node);
    }

    @Override
    public HasValue<Boolean> sourceCheckBox() {
        return sourceURLCheckBox;
    }

    @Override
    public void enableMergeButton(boolean enable) {
        mergeButton.setEnabled(enable);
    }

    @Override
    public void setError(final String message) {
        if (message == null) {
            alertMarker.getStyle().setVisibility(Style.Visibility.HIDDEN);
            return;
        }

        alertMarker.getStyle().setVisibility(Style.Visibility.VISIBLE);

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                Tooltip.create((elemental.dom.Element) alertMarker.getElement(),
                        PositionController.VerticalAlign.TOP,
                        PositionController.HorizontalAlign.MIDDLE,
                        message);
            }
        });
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void show() {
        deckPanel.showWidget(0);
        super.show();
    }

    @Override
    public HasValue<String> targetTextBox() {
        return targetTextBox;
    }

    @Override
    public HasValue<String> sourceURLTextBox() {
        return sourceUrlTextBox;
    }

    @UiHandler("sourceURLCheckBox")
    @SuppressWarnings("unused")
    public void onSourceUrlCheckBoxActivated(ClickEvent event) {
        deckPanel.showWidget(sourceURLCheckBox.getValue() ? 1 : 0);
        delegate.onSourceCheckBoxClicked();
    }

    @UiHandler("sourceUrlTextBox")
    @SuppressWarnings("unused")
    public void onSourceURLChanged(KeyUpEvent event) {
        delegate.onSourceURLChanged(sourceUrlTextBox.getText());
    }

}
