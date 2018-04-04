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
package org.eclipse.che.ide.ext.git.client;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_LEFT_STATUS_PANEL;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_PROFILE;
import static org.eclipse.che.ide.api.constraints.Anchor.BEFORE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.ext.git.client.action.AddToIndexAction;
import org.eclipse.che.ide.ext.git.client.action.CheckoutReferenceAction;
import org.eclipse.che.ide.ext.git.client.action.CommitAction;
import org.eclipse.che.ide.ext.git.client.action.CompareWithBranchAction;
import org.eclipse.che.ide.ext.git.client.action.CompareWithLatestAction;
import org.eclipse.che.ide.ext.git.client.action.CompareWithRevisionAction;
import org.eclipse.che.ide.ext.git.client.action.DeleteRepositoryAction;
import org.eclipse.che.ide.ext.git.client.action.FetchAction;
import org.eclipse.che.ide.ext.git.client.action.HistoryAction;
import org.eclipse.che.ide.ext.git.client.action.InitRepositoryAction;
import org.eclipse.che.ide.ext.git.client.action.NextDiffAction;
import org.eclipse.che.ide.ext.git.client.action.PreviousDiffAction;
import org.eclipse.che.ide.ext.git.client.action.PullAction;
import org.eclipse.che.ide.ext.git.client.action.PushAction;
import org.eclipse.che.ide.ext.git.client.action.RemoveFromIndexAction;
import org.eclipse.che.ide.ext.git.client.action.ResetFilesAction;
import org.eclipse.che.ide.ext.git.client.action.ResetToCommitAction;
import org.eclipse.che.ide.ext.git.client.action.RevertCommitAction;
import org.eclipse.che.ide.ext.git.client.action.ShowBranchesAction;
import org.eclipse.che.ide.ext.git.client.action.ShowMergeAction;
import org.eclipse.che.ide.ext.git.client.action.ShowRemoteAction;
import org.eclipse.che.ide.ext.git.client.action.ShowStatusAction;
import org.eclipse.che.ide.ext.git.client.action.StatusBarBranchPointer;
import org.eclipse.che.ide.ext.git.client.action.ToggleGitPanelAction;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * Extension add Git support to the IDE Application.
 *
 * @author Andrey Plotnikov
 */
@Singleton
@Extension(title = "Git", version = "3.0.0")
public class GitExtension {
  public static final String GIT_GROUP_MAIN_MENU = "Git";
  public static final String REPOSITORY_GROUP_MAIN_MENU = "GitRepositoryGroup";
  public static final String COMMAND_GROUP_MAIN_MENU = "GitCommandGroup";
  public static final String HISTORY_GROUP_MAIN_MENU = "GitHistoryGroup";

  public static final String GIT_COMPARE_WITH_LATEST = "gitCompareWithLatest";
  public static final String GIT_SHOW_BRANCHES_LIST = "gitBranches";
  public static final String GIT_SHOW_COMMIT_WINDOW = "gitCommit";
  public static final String GIT_SHOW_PULL_DIALOG = "gitPull";
  public static final String GIT_SHOW_PUSH_DIALOG = "gitPush";

  public static final String NEXT_DIFF_ACTION_ID = "nextDiff";
  public static final String PREV_DIFF_ACTION_ID = "prevDiff";

  public static final String GIT_PANEL_ACTION_ID = "gitPanel";

  @Inject
  public GitExtension(
      GitResources resources,
      ActionManager actionManager,
      InitRepositoryAction initAction,
      DeleteRepositoryAction deleteAction,
      AddToIndexAction addToIndexAction,
      ResetToCommitAction resetToCommitAction,
      RevertCommitAction revertCommitAction,
      RemoveFromIndexAction removeFromIndexAction,
      CommitAction commitAction,
      CheckoutReferenceAction checkoutReferenceAction,
      ShowBranchesAction showBranchesAction,
      ShowMergeAction showMergeAction,
      ResetFilesAction resetFilesAction,
      ShowStatusAction showStatusAction,
      ShowRemoteAction showRemoteAction,
      PushAction pushAction,
      FetchAction fetchAction,
      PullAction pullAction,
      GitLocalizationConstant constant,
      HistoryAction historyAction,
      CompareWithLatestAction compareWithLatestAction,
      CompareWithBranchAction compareWithBranchAction,
      CompareWithRevisionAction compareWithRevisionAction,
      NextDiffAction nextDiffAction,
      PreviousDiffAction previousDiffAction,
      KeyBindingAgent keyBinding,
      ToggleGitPanelAction gitPanelAction,
      GitNotificationsSubscriber gitNotificationsSubscriber,
      StatusBarBranchPointer statusBarBranchPointer) {
    gitNotificationsSubscriber.subscribe();

    resources.gitCSS().ensureInjected();
    resources.gitPanelCss().ensureInjected();

    DefaultActionGroup mainMenu = (DefaultActionGroup) actionManager.getAction(GROUP_MAIN_MENU);

    DefaultActionGroup git = new DefaultActionGroup(GIT_GROUP_MAIN_MENU, true, actionManager);
    actionManager.registerAction("git", git);
    mainMenu.add(git, new Constraints(BEFORE, GROUP_PROFILE));

    DefaultActionGroup commandGroup =
        new DefaultActionGroup(COMMAND_GROUP_MAIN_MENU, false, actionManager);
    actionManager.registerAction("gitCommandGroup", commandGroup);
    git.add(commandGroup);
    git.addSeparator();

    DefaultActionGroup historyGroup =
        new DefaultActionGroup(HISTORY_GROUP_MAIN_MENU, false, actionManager);
    actionManager.registerAction("gitHistoryGroup", historyGroup);
    git.add(historyGroup);
    git.addSeparator();

    DefaultActionGroup repositoryGroup =
        new DefaultActionGroup(REPOSITORY_GROUP_MAIN_MENU, false, actionManager);
    actionManager.registerAction("gitRepositoryGroup", repositoryGroup);
    git.add(repositoryGroup);

    actionManager.registerAction("gitInitRepository", initAction);
    repositoryGroup.add(initAction);
    actionManager.registerAction("gitDeleteRepository", deleteAction);
    repositoryGroup.add(deleteAction);

    actionManager.registerAction("gitAddToIndex", addToIndexAction);
    commandGroup.add(addToIndexAction);
    DefaultActionGroup compareGroup = new DefaultActionGroup("Compare", true, actionManager);
    actionManager.registerAction("gitCompareGroup", compareGroup);
    commandGroup.add(compareGroup);
    actionManager.registerAction("gitResetToCommit", resetToCommitAction);
    commandGroup.add(resetToCommitAction);
    actionManager.registerAction("gitRevertCommit", revertCommitAction);
    commandGroup.add(revertCommitAction);
    actionManager.registerAction("gitRemoveFromIndexCommit", removeFromIndexAction);
    commandGroup.add(removeFromIndexAction);
    actionManager.registerAction(GIT_SHOW_COMMIT_WINDOW, commitAction);
    commandGroup.add(commitAction);
    actionManager.registerAction(GIT_SHOW_BRANCHES_LIST, showBranchesAction);
    commandGroup.add(showBranchesAction);
    actionManager.registerAction("gitCheckoutReference", checkoutReferenceAction);
    commandGroup.add(checkoutReferenceAction);
    actionManager.registerAction("gitMerge", showMergeAction);
    commandGroup.add(showMergeAction);
    DefaultActionGroup remoteGroup =
        new DefaultActionGroup(constant.remotesControlTitle(), true, actionManager);
    remoteGroup
        .getTemplatePresentation()
        .setImageElement(new SVGImage(resources.remote()).getElement());
    actionManager.registerAction("gitRemoteGroup", remoteGroup);
    commandGroup.add(remoteGroup);
    actionManager.registerAction("gitResetFiles", resetFilesAction);
    commandGroup.add(resetFilesAction);

    actionManager.registerAction("gitHistory", historyAction);
    historyGroup.add(historyAction);
    actionManager.registerAction("gitStatus", showStatusAction);
    historyGroup.add(showStatusAction);
    actionManager.registerAction(GIT_SHOW_PUSH_DIALOG, pushAction);
    remoteGroup.add(pushAction);
    actionManager.registerAction("gitFetch", fetchAction);
    remoteGroup.add(fetchAction);
    actionManager.registerAction(GIT_SHOW_PULL_DIALOG, pullAction);
    remoteGroup.add(pullAction);
    actionManager.registerAction("gitRemote", showRemoteAction);
    remoteGroup.add(showRemoteAction);

    actionManager.registerAction(GIT_COMPARE_WITH_LATEST, compareWithLatestAction);
    compareGroup.add(compareWithLatestAction);
    actionManager.registerAction("gitCompareWithBranch", compareWithBranchAction);
    compareGroup.add(compareWithBranchAction);
    actionManager.registerAction("gitCompareWithRevision", compareWithRevisionAction);
    compareGroup.add(compareWithRevisionAction);

    DefaultActionGroup gitContextMenuGroup = new DefaultActionGroup("Git", true, actionManager);
    actionManager.registerAction("gitCompareContextMenu", gitContextMenuGroup);
    gitContextMenuGroup.add(addToIndexAction);
    gitContextMenuGroup.add(removeFromIndexAction);
    gitContextMenuGroup.add(resetFilesAction);
    gitContextMenuGroup.add(commitAction);
    gitContextMenuGroup.add(historyAction);
    gitContextMenuGroup.addSeparator();
    gitContextMenuGroup.add(compareWithLatestAction);
    gitContextMenuGroup.add(compareWithBranchAction);
    gitContextMenuGroup.add(compareWithRevisionAction);

    DefaultActionGroup projectExplorerContextMenuGroup =
        (DefaultActionGroup) actionManager.getAction("resourceOperation");
    projectExplorerContextMenuGroup.add(gitContextMenuGroup);

    DefaultActionGroup editorContextMenuGroup =
        (DefaultActionGroup) actionManager.getAction("editorContextMenu");
    editorContextMenuGroup.addSeparator();
    editorContextMenuGroup.add(gitContextMenuGroup);

    actionManager.registerAction(NEXT_DIFF_ACTION_ID, nextDiffAction);
    actionManager.registerAction(PREV_DIFF_ACTION_ID, previousDiffAction);

    actionManager.registerAction(GIT_PANEL_ACTION_ID, gitPanelAction);

    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().action().alt().charCode('d').build(), GIT_COMPARE_WITH_LATEST);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().action().charCode('b').build(), GIT_SHOW_BRANCHES_LIST);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().alt().charCode('c').build(), GIT_SHOW_COMMIT_WINDOW);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().alt().charCode('C').build(), GIT_SHOW_PUSH_DIALOG);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().alt().charCode('p').build(), GIT_SHOW_PULL_DIALOG);

    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().alt().charCode('.').build(), NEXT_DIFF_ACTION_ID);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().alt().charCode(',').build(), PREV_DIFF_ACTION_ID);

    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().alt().charCode('g').build(), GIT_PANEL_ACTION_ID);

    DefaultActionGroup rightStatusPanelGroup =
        (DefaultActionGroup) actionManager.getAction(GROUP_LEFT_STATUS_PANEL);
    rightStatusPanelGroup.add(statusBarBranchPointer);
  }
}
