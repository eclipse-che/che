/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_ASSISTANT;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_FILE_NEW;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_PROJECT;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RIGHT_STATUS_PANEL;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.action.Separator;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerRunningEvent;
import org.eclipse.che.ide.ext.java.client.action.FileStructureAction;
import org.eclipse.che.ide.ext.java.client.action.FindUsagesAction;
import org.eclipse.che.ide.ext.java.client.action.MarkDirAsSourceAction;
import org.eclipse.che.ide.ext.java.client.action.MarkDirectoryAsGroup;
import org.eclipse.che.ide.ext.java.client.action.NewJavaSourceFileAction;
import org.eclipse.che.ide.ext.java.client.action.NewPackageAction;
import org.eclipse.che.ide.ext.java.client.action.OpenImplementationAction;
import org.eclipse.che.ide.ext.java.client.action.OrganizeImportsAction;
import org.eclipse.che.ide.ext.java.client.action.ProjectClasspathAction;
import org.eclipse.che.ide.ext.java.client.action.QuickDocumentationAction;
import org.eclipse.che.ide.ext.java.client.action.QuickFixAction;
import org.eclipse.che.ide.ext.java.client.action.UnmarkDirAsSourceAction;
import org.eclipse.che.ide.ext.java.client.progressor.ProgressMessagesHandler;
import org.eclipse.che.ide.ext.java.client.progressor.background.ProgressMonitorAction;
import org.eclipse.che.ide.ext.java.client.refactoring.move.CutJavaSourceAction;
import org.eclipse.che.ide.ext.java.client.refactoring.move.MoveAction;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.RenameRefactoringAction;
import org.eclipse.che.ide.ext.java.client.service.CustomNotificationReceiver;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.ide.util.input.KeyCodeMap;

/** @author Evgen Vidolob */
@Extension(title = "Java", version = "3.0.0")
public class JavaExtension {

  public static final String OPEN_IMPLEMENTATION = "openImplementation";
  public static final String SHOW_QUICK_DOC = "showQuickDoc";
  public static final String JAVA_CLASS_STRUCTURE = "javaClassStructure";
  public static final String ORGANIZE_IMPORTS = "organizeImports";
  public static final String QUICK_FIX = "quickFix";
  public static final String JAVA_RENAME_REFACTORING = "javaRenameRefactoring";
  public static final String JAVA_CUT_REFACTORING = "javaCutRefactoring";
  public static final String JAVA_MOVE_REFACTORING = "javaMoveRefactoring";
  public static final String JAVA_FIND_USAGES = "javaFindUsages";
  private static final String GROUP_ASSISTANT_REFACTORING = "assistantRefactoringGroup";

  @Inject
  public JavaExtension(
      FileTypeRegistry fileTypeRegistry,
      AppContext appContext,
      EventBus eventBus,
      CustomNotificationReceiver customNotificationReceiver,
      @Named("JavaFileType") FileType javaFile,
      @Named("JavaClassFileType") FileType classFile,
      @Named("JspFileType") FileType jspFile) {
    JavaResources.INSTANCE.css().ensureInjected();

    fileTypeRegistry.registerFileType(javaFile);
    fileTypeRegistry.registerFileType(jspFile);
    fileTypeRegistry.registerFileType(classFile);

    eventBus.addHandler(
        WsAgentServerRunningEvent.TYPE,
        e -> {
          customNotificationReceiver.subscribe();
        });

    if (appContext.getWorkspace().getStatus() == RUNNING) {
      customNotificationReceiver.subscribe();
    }
  }

  @Inject
  private void prepareActions(
      NewPackageAction newPackageAction,
      KeyBindingAgent keyBinding,
      NewJavaSourceFileAction newJavaSourceFileAction,
      ActionManager actionManager,
      ProjectClasspathAction projectClasspathAction,
      MoveAction moveAction,
      CutJavaSourceAction cutAction,
      FileStructureAction fileStructureAction,
      MarkDirAsSourceAction markDirAsSourceAction,
      UnmarkDirAsSourceAction unmarkDirAsSourceAction,
      MarkDirectoryAsGroup markDirectoryAsGroup,
      ProgressMonitorAction progressMonitorAction,
      ProgressMessagesHandler progressMessagesHandler,
      OrganizeImportsAction organizeImportsAction,
      RenameRefactoringAction renameRefactoringAction,
      QuickDocumentationAction quickDocumentationAction,
      QuickFixAction quickFixAction,
      OpenImplementationAction openImplementationAction,
      FindUsagesAction findUsagesAction) {

    DefaultActionGroup newGroup = (DefaultActionGroup) actionManager.getAction(GROUP_FILE_NEW);

    actionManager.registerAction("newJavaClass", newJavaSourceFileAction);
    newGroup.add(newJavaSourceFileAction, Constraints.FIRST);

    actionManager.registerAction("newJavaPackage", newPackageAction);
    newGroup.add(newPackageAction, new Constraints(Anchor.AFTER, "newJavaClass"));

    newGroup.add(Separator.getInstance(), new Constraints(Anchor.AFTER, "newJavaPackage"));

    DefaultActionGroup refactorGroup =
        (DefaultActionGroup) actionManager.getAction(GROUP_ASSISTANT_REFACTORING);
    if (refactorGroup == null) {
      refactorGroup = new DefaultActionGroup("Refactoring", true, actionManager);
      actionManager.registerAction(GROUP_ASSISTANT_REFACTORING, refactorGroup);
    }

    DefaultActionGroup projectGroup = (DefaultActionGroup) actionManager.getAction(GROUP_PROJECT);
    actionManager.registerAction("projectProperties", projectClasspathAction);
    projectGroup.add(projectClasspathAction, new Constraints(Anchor.LAST, null));

    DefaultActionGroup assistantGroup =
        (DefaultActionGroup) actionManager.getAction(GROUP_ASSISTANT);
    refactorGroup.addSeparator();
    refactorGroup.add(moveAction);
    refactorGroup.add(renameRefactoringAction);
    assistantGroup.add(refactorGroup, new Constraints(Anchor.BEFORE, "updateDependency"));

    actionManager.registerAction(SHOW_QUICK_DOC, quickDocumentationAction);
    actionManager.registerAction(OPEN_IMPLEMENTATION, openImplementationAction);
    actionManager.registerAction(JAVA_RENAME_REFACTORING, renameRefactoringAction);
    actionManager.registerAction(JAVA_MOVE_REFACTORING, moveAction);
    actionManager.registerAction(JAVA_CUT_REFACTORING, cutAction);
    actionManager.registerAction(JAVA_FIND_USAGES, findUsagesAction);
    actionManager.registerAction(JAVA_CLASS_STRUCTURE, fileStructureAction);
    actionManager.registerAction(ORGANIZE_IMPORTS, organizeImportsAction);
    actionManager.registerAction(QUICK_FIX, quickFixAction);

    assistantGroup.add(
        quickDocumentationAction, new Constraints(Anchor.BEFORE, GROUP_ASSISTANT_REFACTORING));
    assistantGroup.add(quickFixAction, new Constraints(Anchor.BEFORE, GROUP_ASSISTANT_REFACTORING));
    assistantGroup.add(
        organizeImportsAction, new Constraints(Anchor.BEFORE, GROUP_ASSISTANT_REFACTORING));
    assistantGroup.add(
        openImplementationAction, new Constraints(Anchor.BEFORE, GROUP_ASSISTANT_REFACTORING));
    assistantGroup.add(
        fileStructureAction, new Constraints(Anchor.BEFORE, GROUP_ASSISTANT_REFACTORING));
    assistantGroup.add(
        findUsagesAction, new Constraints(Anchor.BEFORE, GROUP_ASSISTANT_REFACTORING));

    // Configure Build Path action group
    actionManager.registerAction("markDirectoryAsSourceGroup", markDirectoryAsGroup);
    actionManager.registerAction("markDirectoryAsSource", markDirAsSourceAction);
    actionManager.registerAction("unmarkDirectoryAsSource", unmarkDirAsSourceAction);
    markDirectoryAsGroup.add(markDirAsSourceAction);
    markDirectoryAsGroup.add(unmarkDirAsSourceAction);
    markDirectoryAsGroup.add(projectClasspathAction);

    DefaultActionGroup mainContextMenuGroup =
        (DefaultActionGroup) actionManager.getAction("resourceOperation");
    mainContextMenuGroup.addSeparator();
    mainContextMenuGroup.add(markDirectoryAsGroup);
    mainContextMenuGroup.addSeparator();

    // add resolver widget on right part of bottom panel
    final DefaultActionGroup rightStatusPanelGroup =
        (DefaultActionGroup) actionManager.getAction(GROUP_RIGHT_STATUS_PANEL);
    rightStatusPanelGroup.add(progressMonitorAction);

    DefaultActionGroup editorContextMenuGroup =
        (DefaultActionGroup) actionManager.getAction(IdeActions.GROUP_EDITOR_CONTEXT_MENU);

    editorContextMenuGroup.add(quickDocumentationAction, new Constraints(Anchor.AFTER, "format"));
    editorContextMenuGroup.add(quickFixAction, new Constraints(Anchor.AFTER, SHOW_QUICK_DOC));
    editorContextMenuGroup.add(
        fileStructureAction, new Constraints(Anchor.AFTER, GROUP_ASSISTANT_REFACTORING));

    if (UserAgent.isMac()) {
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().alt().control().charCode('b').build(), OPEN_IMPLEMENTATION);
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().control().charCode('j').build(), SHOW_QUICK_DOC);
      keyBinding
          .getGlobal()
          .addKey(
              new KeyBuilder().control().charCode(KeyCodeMap.F12).build(), JAVA_CLASS_STRUCTURE);
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().alt().control().charCode('o').build(), ORGANIZE_IMPORTS);
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().action().charCode(KeyCodeMap.ENTER).build(), QUICK_FIX);
    } else {
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().alt().action().charCode('b').build(), OPEN_IMPLEMENTATION);
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().action().charCode('q').build(), SHOW_QUICK_DOC);
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().action().charCode(KeyCodeMap.F12).build(), JAVA_CLASS_STRUCTURE);
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().alt().action().charCode('o').build(), ORGANIZE_IMPORTS);
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().alt().charCode(KeyCodeMap.ENTER).build(), QUICK_FIX);
    }
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().shift().charCode(KeyCodeMap.F6).build(), JAVA_RENAME_REFACTORING);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().action().charCode('x').build(), JAVA_CUT_REFACTORING);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().charCode(KeyCodeMap.F6).build(), JAVA_MOVE_REFACTORING);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().alt().charCode(KeyCodeMap.F7).build(), JAVA_FIND_USAGES);
  }

  @Inject
  private void registerIcons(IconRegistry iconRegistry, JavaResources resources) {
    // icon for category in Wizard
    iconRegistry.registerIcon(
        new Icon(Constants.JAVA_CATEGORY + ".samples.category.icon", resources.javaCategoryIcon()));
  }
}
