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
package org.eclipse.che.ide.ext.git.client.merge;

import static org.eclipse.che.ide.ext.git.client.merge.MergePresenter.LOCAL_BRANCHES_TITLE;
import static org.eclipse.che.ide.ext.git.client.merge.MergePresenter.REMOTE_BRANCHES_TITLE;
import static org.eclipse.che.ide.ext.git.client.merge.Reference.RefType.LOCAL_BRANCH;
import static org.eclipse.che.ide.ext.git.client.merge.Reference.RefType.REMOTE_BRANCH;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.util.input.SignalEvent;

/**
 * The implementation of {@link MergeView}.
 *
 * @author Andrey Plotnikov
 */
@Singleton
public class MergeViewImpl extends Window implements MergeView {
  interface MergeViewImplUiBinder extends UiBinder<Widget, MergeViewImpl> {}

  private static MergeViewImplUiBinder ourUiBinder = GWT.create(MergeViewImplUiBinder.class);

  Button btnCancel;
  Button btnMerge;
  @UiField ScrollPanel referencesPanel;

  @UiField(provided = true)
  final GitResources res;

  @UiField(provided = true)
  final GitLocalizationConstant locale;

  private Tree<Reference> references;
  private ActionDelegate delegate;
  private final Reference localBranch;
  private final Reference remoteBranch;

  /**
   * Create view.
   *
   * @param resources
   * @param locale
   * @param rendererResources
   */
  @Inject
  protected MergeViewImpl(
      GitResources resources,
      GitLocalizationConstant locale,
      ReferenceTreeNodeRenderer.Resources rendererResources) {
    this.res = resources;
    this.locale = locale;
    this.ensureDebugId("git-merge-window");

    Widget widget = ourUiBinder.createAndBindUi(this);

    this.setTitle(locale.mergeTitle());
    this.setWidget(widget);

    this.references =
        Tree.create(
            rendererResources,
            new ReferenceTreeNodeDataAdapter(),
            new ReferenceTreeNodeRenderer(rendererResources, resources));
    this.references.setTreeEventHandler(
        new Tree.Listener<Reference>() {
          @Override
          public void onNodeAction(TreeNodeElement<Reference> node) {}

          @Override
          public void onNodeClosed(TreeNodeElement<Reference> node) {
            // do nothing
          }

          @Override
          public void onNodeContextMenu(int mouseX, int mouseY, TreeNodeElement<Reference> node) {
            // do nothing
          }

          @Override
          public void onNodeDragStart(TreeNodeElement<Reference> node, MouseEvent event) {
            // do nothing
          }

          @Override
          public void onNodeDragDrop(TreeNodeElement<Reference> node, MouseEvent event) {
            // do nothing
          }

          @Override
          public void onNodeExpanded(final TreeNodeElement<Reference> node) {
            delegate.onReferenceSelected(node.getData());
          }

          @Override
          public void onNodeSelected(TreeNodeElement<Reference> node, SignalEvent event) {
            delegate.onReferenceSelected(node.getData());
          }

          @Override
          public void onRootContextMenu(int mouseX, int mouseY) {
            // do nothing
          }

          @Override
          public void onRootDragDrop(MouseEvent event) {
            // do nothing
          }

          @Override
          public void onKeyboard(KeyboardEvent event) {
            // do nothing
          }
        });
    this.referencesPanel.add(references.asWidget());

    Reference root = references.getModel().getRoot();
    if (root == null) {
      root = new Reference("", "", null);
      references.getModel().setRoot(root);
    }

    localBranch = new Reference(LOCAL_BRANCHES_TITLE, LOCAL_BRANCHES_TITLE, LOCAL_BRANCH);

    remoteBranch = new Reference(REMOTE_BRANCHES_TITLE, REMOTE_BRANCHES_TITLE, REMOTE_BRANCH);

    List<Reference> branches = new ArrayList<>();
    branches.add(localBranch);
    branches.add(remoteBranch);
    root.setBranches(branches);

    btnCancel =
        createButton(
            locale.buttonCancel(),
            "git-merge-cancel",
            new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
              }
            });
    addButtonToFooter(btnCancel);

    btnMerge =
        createButton(
            locale.buttonMerge(),
            "git-merge-merge",
            new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                delegate.onMergeClicked();
              }
            });
    addButtonToFooter(btnMerge);
  }

  @Override
  protected void onEnterClicked() {
    if (isWidgetFocused(btnMerge)) {
      delegate.onMergeClicked();
      return;
    }

    if (isWidgetFocused(btnCancel)) {
      delegate.onCancelClicked();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setLocalBranches(@NotNull List<Reference> references) {
    localBranch.setBranches(references);
    this.references.renderTree(0);
  }

  /** {@inheritDoc} */
  @Override
  public void setRemoteBranches(@NotNull List<Reference> references) {
    remoteBranch.setBranches(references);
    this.references.renderTree(0);
  }

  /** {@inheritDoc} */
  @Override
  public void setEnableMergeButton(boolean enabled) {
    btnMerge.setEnabled(enabled);
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    this.hide();
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    this.show();
  }
}
