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
package org.eclipse.che.plugin.svn.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.svn.ide.action.AddAction;
import org.eclipse.che.plugin.svn.ide.action.CleanupAction;
import org.eclipse.che.plugin.svn.ide.action.CommitAction;
import org.eclipse.che.plugin.svn.ide.action.CopyAction;
import org.eclipse.che.plugin.svn.ide.action.DiffAction;
import org.eclipse.che.plugin.svn.ide.action.ExportAction;
import org.eclipse.che.plugin.svn.ide.action.LockAction;
import org.eclipse.che.plugin.svn.ide.action.LogAction;
import org.eclipse.che.plugin.svn.ide.action.MergeAction;
import org.eclipse.che.plugin.svn.ide.action.MoveAction;
import org.eclipse.che.plugin.svn.ide.action.PropertiesAction;
import org.eclipse.che.plugin.svn.ide.action.RemoveAction;
import org.eclipse.che.plugin.svn.ide.action.ResolveAction;
import org.eclipse.che.plugin.svn.ide.action.RevertAction;
import org.eclipse.che.plugin.svn.ide.action.StatusAction;
import org.eclipse.che.plugin.svn.ide.action.SwitchAction;
import org.eclipse.che.plugin.svn.ide.action.UnlockAction;
import org.eclipse.che.plugin.svn.ide.action.UpdateAction;
import org.eclipse.che.plugin.svn.ide.action.UpdateToRevisionAction;


/**
 * Extension adding Subversion support.
 *
 * @author Jeremy Whitlock
 */
@Singleton
@Extension(title = "Subversion", version = "1.0.0")
public class SubversionExtension {

    private final String FILE_COMMAND_GROUP          = "SvnFileCommandGroup";
    private final String REMOTE_COMMAND_GROUP        = "SvnRemoteCommandGroup";
    private final String REPOSITORY_COMMAND_GROUP    = "SvnRepositoryCommandGroup";
    private final String ADD_COMMAND_GROUP           = "SvnAddCommandGroup";
    private final String MISCELLANEOUS_COMMAND_GROUP = "SvnMiscellaneousCommandGroup";
    private final String CREDENTIALS_COMMAND_GROUP   = "SvnCredentialsCommandGroup";
    private final String SVN_GROUP_MAIN_MENU;

    @Inject
    public SubversionExtension(final ActionManager actionManager,
                               final AddAction addAction,
                               final CleanupAction cleanupAction,
                               final CommitAction commitAction,
                               final DiffAction diffAction,
                               final ExportAction exportAction,
                               final LockAction lockAction,
                               final LogAction logAction,
                               final MergeAction mergeAction,
                               final PropertiesAction propertiesAction,
                               final RemoveAction removeAction,
                               final ResolveAction resolveAction,
                               final CopyAction copyAction,
                               final MoveAction moveAction,
                               final RevertAction revertAction,
                               final StatusAction statusAction,
                               final UnlockAction unlockAction,
                               final UpdateAction updateAction,
                               final SwitchAction switchAction,
                               final UpdateToRevisionAction updateToRevisionAction,
                               final SubversionExtensionLocalizationConstants constants,
                               final SubversionExtensionResources resources) {
        SVN_GROUP_MAIN_MENU = constants.subversionLabel();

        final DefaultActionGroup addCommandGroup = new DefaultActionGroup(ADD_COMMAND_GROUP, false, actionManager);
        final DefaultActionGroup mainMenu = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_MAIN_MENU);
        final DefaultActionGroup fileCommandGroup = new DefaultActionGroup(FILE_COMMAND_GROUP, false, actionManager);
        final DefaultActionGroup miscellaneousCommandGroup = new DefaultActionGroup(MISCELLANEOUS_COMMAND_GROUP, false,
                                                                                    actionManager);
        final DefaultActionGroup remoteCommandGroup = new DefaultActionGroup(REMOTE_COMMAND_GROUP, false,
                                                                             actionManager);
        final DefaultActionGroup repositoryCommandGroup = new DefaultActionGroup(REPOSITORY_COMMAND_GROUP, false,
                                                                                 actionManager);
        final DefaultActionGroup credentialsCommandGroup = new DefaultActionGroup(CREDENTIALS_COMMAND_GROUP, false,
                                                                                  actionManager);
        final DefaultActionGroup svnMenu = new DefaultActionGroup(SVN_GROUP_MAIN_MENU, true, actionManager);

        resources.subversionCSS().ensureInjected();

        // Register action groups
        actionManager.registerAction(SVN_GROUP_MAIN_MENU, svnMenu);
        mainMenu.add(svnMenu, new Constraints(Anchor.BEFORE, IdeActions.GROUP_PROFILE));

        actionManager.registerAction(REMOTE_COMMAND_GROUP, remoteCommandGroup);
        svnMenu.add(remoteCommandGroup);
        svnMenu.addSeparator();

        actionManager.registerAction(FILE_COMMAND_GROUP, fileCommandGroup);
        svnMenu.add(fileCommandGroup);
        svnMenu.addSeparator();

        actionManager.registerAction(REPOSITORY_COMMAND_GROUP, repositoryCommandGroup);
        svnMenu.add(repositoryCommandGroup);
        svnMenu.addSeparator();

        actionManager.registerAction(ADD_COMMAND_GROUP, addCommandGroup);
        svnMenu.add(addCommandGroup);
        svnMenu.addSeparator();

        actionManager.registerAction(MISCELLANEOUS_COMMAND_GROUP, miscellaneousCommandGroup);
        svnMenu.add(miscellaneousCommandGroup);
        svnMenu.addSeparator();

        actionManager.registerAction(CREDENTIALS_COMMAND_GROUP, credentialsCommandGroup);
        svnMenu.add(credentialsCommandGroup);

        // Register actions

        // Commands that provide status of project or files
        actionManager.registerAction("SvnStatus", statusAction);
        remoteCommandGroup.add(statusAction);
        actionManager.registerAction("SvnViewLog", logAction);
        remoteCommandGroup.add(logAction);
        actionManager.registerAction("SvnDiff", diffAction);
        remoteCommandGroup.add(diffAction);

        // Commands that manage pull and push of changes
        actionManager.registerAction("SvnSwitch", switchAction);
        fileCommandGroup.add(switchAction);
        actionManager.registerAction("SvnUpdate", updateAction);
        fileCommandGroup.add(updateAction);
        actionManager.registerAction("SvnUpdateToRevision", updateToRevisionAction);
        fileCommandGroup.add(updateToRevisionAction);
        actionManager.registerAction("SvnCommit", commitAction);
        fileCommandGroup.add(commitAction);
        actionManager.registerAction("SvnResolve", resolveAction);
        fileCommandGroup.add(resolveAction);

        actionManager.registerAction("SvnCopy", copyAction);
        fileCommandGroup.add(copyAction);
        actionManager.registerAction("SvnMove", moveAction);
        fileCommandGroup.add(moveAction);

        // Commands that interact with the repository
        actionManager.registerAction("SvnMerge", mergeAction);
        repositoryCommandGroup.add(mergeAction);
        actionManager.registerAction("SvnExport", exportAction);
        repositoryCommandGroup.add(exportAction);

        // Commands for miscellany
        actionManager.registerAction("SvnProperties", propertiesAction);
        miscellaneousCommandGroup.add(propertiesAction);

        // Commands that manage working copy
        actionManager.registerAction("SvnAdd", addAction);
        addCommandGroup.add(addAction);
        actionManager.registerAction("SvnRemove", removeAction);
        addCommandGroup.add(removeAction);
        actionManager.registerAction("SvnRevert", revertAction);
        addCommandGroup.add(revertAction);
        actionManager.registerAction("SvnLock", lockAction);
        addCommandGroup.add(lockAction);
        actionManager.registerAction("SvnUnlock", unlockAction);
        addCommandGroup.add(unlockAction);
        actionManager.registerAction("SvnCleanup", cleanupAction);
        addCommandGroup.add(cleanupAction);

        //context menu
        DefaultActionGroup contextGroup = new DefaultActionGroup("Subversion", true, actionManager);
        contextGroup.getTemplatePresentation().setDescription("Subversion operation...");

        contextGroup.add(statusAction);
        contextGroup.add(logAction);
        contextGroup.add(diffAction);
        contextGroup.addSeparator();
        contextGroup.add(switchAction);
        contextGroup.add(updateAction);
        contextGroup.add(commitAction);
        contextGroup.add(resolveAction);
        contextGroup.addSeparator();
        contextGroup.add(mergeAction);
        contextGroup.addSeparator();
        contextGroup.add(addAction);
        contextGroup.add(removeAction);
        contextGroup.add(cleanupAction);
        contextGroup.add(revertAction);

        contextGroup.getTemplatePresentation().setSVGResource(resources.svn());

        DefaultActionGroup resourceOperationPartOfMainContextMenuGroup = (DefaultActionGroup)actionManager.getAction("resourceOperation");
        resourceOperationPartOfMainContextMenuGroup.add(contextGroup);
    }
}
