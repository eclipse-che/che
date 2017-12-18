/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.action;

/**
 * @author Evgen Vidolob
 * @author Vlad Zhukovskyi
 */
public interface IdeActions {

  String GROUP_MAIN_MENU = "mainMenu";
  String GROUP_WORKSPACE = "workspaceGroup";
  String GROUP_PROJECT = "projectGroup";
  String GROUP_EDIT = "editGroup";
  String GROUP_ASSISTANT = "assistantGroup";
  String TOOL_WINDOWS_GROUP = "toolWindows";
  String GROUP_RUN = "runGroup";
  String GROUP_PROFILE = "profileGroup";
  String GROUP_FILE_NEW = "newGroup";
  String GROUP_WINDOW = "windowGroup";
  String GROUP_HELP = "helpGroup";
  String GROUP_RECENT_FILES = "recentFiles";

  String GROUP_MAIN_TOOLBAR = "mainToolBar";
  String GROUP_CENTER_TOOLBAR = "centerToolBar";
  String GROUP_RIGHT_TOOLBAR = "rightToolBar";

  String GROUP_MAIN_CONTEXT_MENU = "mainContextMenu";
  String GROUP_RUN_CONTEXT_MENU = "runGroupContextMenu";
  String GROUP_DEBUG_CONTEXT_MENU = "debugGroupContextMenu";
  String GROUP_PROJECT_EXPLORER_CONTEXT_MENU = "projectExplorerContextMenu";
  String GROUP_EDITOR_TAB_CONTEXT_MENU = "editorTabContextMenu";
  String GROUP_CONSOLES_TREE_CONTEXT_MENU = "consolesTreeContextMenu";
  String GROUP_EDITOR_CONTEXT_MENU = "editorContextMenu";

  String GROUP_PART_MENU = "partMenu";
  String GROUP_TOOLBAR_CONTROLLER = "toolbarController";

  String GROUP_OTHER_MENU = "otherMenu";
  String GROUP_LEFT_MAIN_MENU = "leftMainMenu";
  @Deprecated String GROUP_RIGHT_MAIN_MENU = "rightMainMenu";

  String GROUP_CENTER_STATUS_PANEL = "centerStatusPanelGroup";
  String GROUP_LEFT_STATUS_PANEL = "leftStatusPanelGroup";
  String GROUP_RIGHT_STATUS_PANEL = "rightStatusPanelGroup";

  String NAVIGATE_TO_FILE = "navigateToFile";
  String FULL_TEXT_SEARCH = "fullTextSearch";
  String FIND_ACTION = "findActionAction";
  String FORMAT = "format";
  String COPY = "copy";
  String CUT = "cut";
  String PASTE = "paste";
  String SWITCH_LEFT_TAB = "switchLeftTab";
  String SWITCH_RIGHT_TAB = "switchRightTab";
  String OPEN_RECENT_FILES = "openRecentFiles";
  String DELETE_ITEM = "deleteItem";
  String NEW_FILE = "newFile";
  String CREATE_PROJECT = "createProject";
  String IMPORT_PROJECT = "importProject";
  String CLOSE_ACTIVE_EDITOR = "closeActiveEditor";
  String SIGNATURE_HELP = "signatureHelp";
  String RENAME = "renameResource";
  String SHOW_REFERENCE = "showReference";
}
