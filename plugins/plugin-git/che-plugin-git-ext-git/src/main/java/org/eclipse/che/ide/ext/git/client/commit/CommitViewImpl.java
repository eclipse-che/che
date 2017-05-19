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
package org.eclipse.che.ide.ext.git.client.commit;

import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.compare.changedpanel.ChangedFileNode;
import org.eclipse.che.ide.ext.git.client.compare.changedpanel.ChangedFolderNode;
import org.eclipse.che.ide.ext.git.client.compare.changedpanel.ChangedPanelView;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.ShiftableTextArea;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.presentation.DefaultPresentationRenderer;
import org.eclipse.che.ide.ui.window.Window;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The implementation of {@link CommitView}.
 *
 * @author Andrey Plotnikov
 */
@Singleton
public class CommitViewImpl extends Window implements CommitView {
    interface CommitViewImplUiBinder extends UiBinder<Widget, CommitViewImpl> {
    }

    private static CommitViewImplUiBinder uiBinder = GWT.create(CommitViewImplUiBinder.class);

    @UiField(provided = true)
    final TextArea message;
    @UiField
    FlowPanel changedPanel;
    @UiField
    CheckBox  amend;
    @UiField(provided = true)
    final GitResources            res;
    @UiField(provided = true)
    final GitLocalizationConstant locale;

    private ListBox            remoteBranches;
    private CheckBox           pushAfterCommit;
    private Button             btnCommit;
    private Button             btnCancel;
    private ActionDelegate     delegate;
    private ChangedPanelRender render;
    private ChangedPanelView   changedPanelView;

    /**
     * Create view.
     */
    @Inject
    protected CommitViewImpl(GitResources res,
                             GitLocalizationConstant locale,
                             ChangedPanelView changedPanelView) {
        this.res = res;
        this.locale = locale;
        this.changedPanelView = changedPanelView;
        this.message = new ShiftableTextArea();
        this.ensureDebugId("git-commit-window");

        this.setTitle(locale.commitTitle());

        Widget widget = uiBinder.createAndBindUi(this);
        this.setWidget(widget);

        btnCancel = createButton(locale.buttonCancel(), "git-commit-cancel", event -> delegate.onCancelClicked());
        btnCommit = createButton(locale.buttonCommit(), "git-commit-commit", event -> delegate.onCommitClicked());
        btnCommit.addStyleName(resources.windowCss().primaryButton());

        remoteBranches = new ListBox();
        remoteBranches.setEnabled(false);

        pushAfterCommit = new CheckBox();
        pushAfterCommit.setHTML(locale.commitPushCheckboxTitle());
        pushAfterCommit.ensureDebugId("git-commit-push_after_commit");
        pushAfterCommit.addValueChangeHandler(event -> remoteBranches.setEnabled(event.getValue()));

        pushAfterCommit.addStyleName(res.gitCSS().spacing());
        getFooter().add(pushAfterCommit);
        getFooter().add(remoteBranches);

        addButtonToFooter(btnCommit);
        addButtonToFooter(btnCancel);
    }

    @Override
    protected void onEnterClicked() {
        if (isWidgetFocused(btnCommit)) {
            delegate.onCommitClicked();
            return;
        }

        if (isWidgetFocused(btnCancel)) {
            delegate.onCancelClicked();
        }
    }

    @NotNull
    @Override
    public String getMessage() {
        return message.getText();
    }

    @Override
    public void setMessage(@NotNull String message) {
        this.message.setText(message);
    }

    @Override
    public void setRemoteBranchesList(List<Branch> branches) {
        remoteBranches.clear();
        branches.forEach(branch -> remoteBranches.addItem(branch.getDisplayName()));
    }

    @Override
    public boolean isAmend() {
        return amend.getValue();
    }

    @Override
    public void setValueToAmendCheckBox(boolean value) {
        amend.setValue(value);
    }

    @Override
    public void setValueToPushAfterCommitCheckBox(boolean value) {
        pushAfterCommit.setValue(value);
    }

    @Override
    public void setEnableAmendCheckBox(boolean enable) {
        amend.setEnabled(enable);
    }

    @Override
    public void setEnablePushAfterCommitCheckBox(boolean enable) {
        pushAfterCommit.setEnabled(enable);
    }

    @Override
    public void setEnableRemoteBranchesDropDownLis(boolean enable) {
        remoteBranches.setEnabled(enable);
    }

    @Override
    public boolean isPushAfterCommit() {
        return pushAfterCommit.getValue();
    }

    @Override
    public void setEnableCommitButton(boolean enable) {
        btnCommit.setEnabled(enable);
    }

    @Override
    public void focusInMessageField() {
        new Timer() {
            @Override
            public void run() {
                message.setFocus(true);
            }
        }.schedule(300);
    }

    @Override
    public void close() {
        this.hide();
    }

    @Override
    public void showDialog() {
        this.show();
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler("message")
    public void onMessageChanged(KeyUpEvent event) {
        delegate.onValueChanged();
    }

    @UiHandler("amend")
    public void onAmendValueChange(final ValueChangeEvent<Boolean> event) {
        if (event.getValue()) {
            this.delegate.setAmendCommitMessage();
        } else {
            this.message.setValue("");
        }
        delegate.onValueChanged();
    }

    @Override
    public void setChangedPanelView(ChangedPanelView changedPanelView) {
        this.render = new ChangedPanelRender(changedPanelView);
        this.changedPanelView = changedPanelView;
        changedPanelView.setTreeRender(render);
        this.changedPanel.add(changedPanelView);
    }

    @Override
    public void
    checkCheckBoxes(Set<Path> paths) {
        render.setNodePaths(changedPanelView.getNodePaths());
        paths.forEach(path -> render.handleNodeCheckBox(path, false));
    }

    @Override
    public String getRemoteBranch() {
        return remoteBranches.getSelectedValue();
    }

    private class ChangedPanelRender extends DefaultPresentationRenderer<Node> {

        private final ChangedPanelView changedPanelView;
        private final Set<Path>        unselectedNodePaths;

        private Set<Path> allNodePaths;

        ChangedPanelRender(ChangedPanelView changedPanelView) {
            super(changedPanelView.getTreeStyles());
            this.changedPanelView = changedPanelView;
            this.unselectedNodePaths = new HashSet<>();
            this.allNodePaths = new HashSet<>();
        }

        @Override
        public Element render(final Node node, final String domID, final Tree.Joint joint, final int depth) {
            Element rootContainer = super.render(node, domID, joint, depth);
            Element nodeContainer = rootContainer.getFirstChildElement();
            final Element checkBoxElement = new CheckBox().getElement();
            final InputElement checkBoxInputElement = (InputElement)checkBoxElement.getElementsByTagName("input").getItem(0);
            final Path nodePath = node instanceof ChangedFileNode ? Path.valueOf(node.getName()) : ((ChangedFolderNode)node).getPath();
            checkBoxInputElement.setChecked(!unselectedNodePaths.contains(nodePath));
            Event.sinkEvents(checkBoxElement, Event.ONCLICK);
            Event.setEventListener(checkBoxElement, event -> {
                if (Event.ONCLICK == event.getTypeInt() && event.getTarget().getTagName().equalsIgnoreCase("label")) {
                    handleNodeCheckBox(nodePath, checkBoxInputElement.isChecked());

                    changedPanelView.refreshNodes();
                    delegate.onValueChanged();
                }
            });

            nodeContainer.insertAfter(checkBoxElement, nodeContainer.getFirstChild());
            return rootContainer;
        }

        void setNodePaths(Set<Path> paths) {
            allNodePaths = paths;
            unselectedNodePaths.clear();
            unselectedNodePaths.addAll(paths);
        }

        void handleNodeCheckBox(Path nodePath, boolean value) {
            allNodePaths.stream()
                        .sorted(Comparator.comparing(Path::toString))
                        .filter(path -> !(path.equals(nodePath) || path.isEmpty()) && path.isPrefixOf(nodePath) &&
                                        !hasSelectedChildes(path))
                        .forEach(path -> saveCheckBoxSelection(value, path));

            allNodePaths.stream().sorted((path1, path2) -> path2.toString().compareTo(path1.toString()))
                        .filter(path -> !path.isEmpty() &&
                                        (nodePath.isPrefixOf(path) || path.isPrefixOf(nodePath) && !hasSelectedChildes(path)))
                        .forEach(path -> saveCheckBoxSelection(value, path));
        }

        private void saveCheckBoxSelection(boolean checkBoxValue, Path path) {
            if (checkBoxValue) {
                unselectedNodePaths.add(path);
            } else {
                unselectedNodePaths.remove(path);
            }
            if (delegate.getChangedFiles().contains(path.toString())) {
                delegate.onFileNodeCheckBoxValueChanged(path, !checkBoxValue);
            }
        }

        private boolean hasSelectedChildes(Path givenPath) {
            Optional<Path> optional = allNodePaths.stream()
                                                  .filter(path -> givenPath.isPrefixOf(path) &&
                                                                  !path.equals(givenPath) &&
                                                                  !unselectedNodePaths.contains(path))
                                                  .findAny();
            return optional.isPresent();
        }
    }
}
