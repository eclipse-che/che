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
package org.eclipse.che.selenium.core.constant;

/** @author Musienko Maxim */
public interface TestMenuCommandsConstants {

  interface Workspace {
    String WORKSPACE_MENU_PREFIX = "gwt-debug-topmenu/Workspace/";
    String WORKSPACE = "gwt-debug-MenuItem/workspaceGroup-true";
    String IMPORT_PROJECT = WORKSPACE_MENU_PREFIX + "importProject";
    String CREATE_PROJECT = WORKSPACE_MENU_PREFIX + "createProject";
    String STOP_WORKSPACE = WORKSPACE_MENU_PREFIX + "stopWorkspace";
    String CREATE_FACTORY = WORKSPACE_MENU_PREFIX + "configureFactoryAction";
    String STOP = WORKSPACE_MENU_PREFIX + "stopWorkspace";
    String DOWNLOAD_AS_ZIP = WORKSPACE_MENU_PREFIX + "downloadWsAsZipAction";
  }

  interface Project {
    String PROJECT = "gwt-debug-MenuItem/projectGroup-true";
    String PROJECT_MENU_PREFIX = "gwt-debug-topmenu/Project/";
    String UPLOAD_FILE = PROJECT_MENU_PREFIX + "uploadFile";
    String UPLOAD_FOLDER = PROJECT_MENU_PREFIX + "uploadFolder";
    String CONVERT_TO_PROJECT = PROJECT_MENU_PREFIX + "convertFolderToProject";
    String CONFIGURATION = PROJECT_MENU_PREFIX + "projectConfiguration";
    String CONFIGURE_CLASSPATH = PROJECT_MENU_PREFIX + "projectProperties";
    String SHOW_HIDE_HIDDEN_FILES = PROJECT_MENU_PREFIX + "showHideHiddenFiles";
    String UPDATE_PROJECT_CONFIGURATION = PROJECT_MENU_PREFIX + "projectConfiguration";
    String DOWNLOAD_AS_ZIP = PROJECT_MENU_PREFIX + "downloadAsZipAction";

    interface New {
      String NEW = PROJECT_MENU_PREFIX + "newGroup";
      String JAVA_CLASS = PROJECT_MENU_PREFIX + "New/newJavaClass";
      String PACKAGE = PROJECT_MENU_PREFIX + "New/newJavaPackage";
      String MAVEN_MODULE = PROJECT_MENU_PREFIX + "New/createMavenModule";
      String FILE = PROJECT_MENU_PREFIX + "New/newFile";
      String FOLDER = PROJECT_MENU_PREFIX + "New/newFolder";
      String HTML_FILE = PROJECT_MENU_PREFIX + "New/newHtmlFile";
      String CSS_FILE = PROJECT_MENU_PREFIX + "New/newCssFile";
      String XML_FILE = PROJECT_MENU_PREFIX + "New/newXmlFile";
      String LESS_FILE = PROJECT_MENU_PREFIX + "New/newLessFile";
      String JAVASCRIPT_FILE = PROJECT_MENU_PREFIX + "New/newJavaScriptFile";
    }
  }

  interface Edit {
    String EDIT_MENU_PREFIX = "gwt-debug-topmenu/Edit/";
    String EDIT = "gwt-debug-MenuItem/editGroup-true";
    String RECENT = EDIT_MENU_PREFIX + "recentFiles";
    String FORMAT = EDIT_MENU_PREFIX + "format";
    String UNDO = EDIT_MENU_PREFIX + "undo";
    String REDO = EDIT_MENU_PREFIX + "redo";
    String CUT = EDIT_MENU_PREFIX + "cut";
    String COPY = EDIT_MENU_PREFIX + "copy";
    String PASTE = EDIT_MENU_PREFIX + "paste";
    String RENAME = EDIT_MENU_PREFIX + "renameResource";
    String DELETE = EDIT_MENU_PREFIX + "deleteItem";
    String FIND = EDIT_MENU_PREFIX + "fullTextSearch";
    String OPEN_RECENT_FILE = EDIT_MENU_PREFIX + "openRecentFiles";

    interface Recent {
      String CLEAR_LIST = EDIT_MENU_PREFIX + "Recent/clearRecentList";
    }
  }

  interface Assistant {
    String ASSISTANT_MENU_PREFIX = "gwt-debug-topmenu/Assistant/";
    String ASSISTANT = "gwt-debug-MenuItem/assistantGroup-true";
    String FIND_ACTION = ASSISTANT_MENU_PREFIX + "findAction";
    String KEY_BINDINGS = ASSISTANT_MENU_PREFIX + "hotKeysList";
    String NAVIGATE_TO_FILE = ASSISTANT_MENU_PREFIX + "navigateToFile";
    String QUICK_DOCUMENTATION = ASSISTANT_MENU_PREFIX + "showQuickDoc";
    String OPEN_DECLARATION = ASSISTANT_MENU_PREFIX + "openJavaDeclaration";
    String IMPLEMENTATION_S = ASSISTANT_MENU_PREFIX + "openImplementation";
    String FILE_STRUCTURE = ASSISTANT_MENU_PREFIX + "javaClassStructure";
    String FIND_USAGES = ASSISTANT_MENU_PREFIX + "javaFindUsages";
    String UPDATE_DEPENDENCIES = ASSISTANT_MENU_PREFIX + "updateDependency";
    String ORGANIZE_IMPORTS = ASSISTANT_MENU_PREFIX + "organizeImports";
    String GENERATE_EFFECTIVE_POM = ASSISTANT_MENU_PREFIX + "getEffectivePom";
    String QUICK_FIX = ASSISTANT_MENU_PREFIX + "quickFix";
    String FIND_DEFINITION = ASSISTANT_MENU_PREFIX + "LSFindDefinitionAction";

    interface Refactoring {
      String REFACTORING = ASSISTANT_MENU_PREFIX + "assistantRefactoringGroup";
      String MOVE = ASSISTANT_MENU_PREFIX + "Refactoring/javaMoveRefactoring";
      String RENAME = ASSISTANT_MENU_PREFIX + "Refactoring/javaRenameRefactoring";
    }
  }

  interface Run {
    String TERMINAL = "gwt-debug-topmenu/Run/newTerminal";
    String EDIT_COMMANDS = "gwt-debug-topmenu/Run/editCommands";
    String RUN_MENU = "gwt-debug-MenuItem/runGroup-true";
    String EDIT_DEBUG_CONFIGURATION = "gwt-debug-topmenu/Run/editDebugConfigurations";
    String DEBUG = "gwt-debug-topmenu/Run/Debug";
    String DEBUG_CONFIGURATION = "gwt-debug-topmenu/Run/Debug Configurations";
    String END_DEBUG_SESSION = "gwt-debug-topmenu/Run/disconnectDebug";
    String TEST = "gwt-debug-topmenu/Run/TestingMainGroup";
  }

  interface Profile {
    String PROFILE_PREFIX = "gwt-debug-topmenu/Profile/";
    String PROFILE_MENU = "gwt-debug-MenuItem/profileGroup-true";
    String ACCOUNT = PROFILE_PREFIX + "redirectToDashboardAccount";
    String PROJECTS = PROFILE_PREFIX + "redirectToDashboardProjectsAction";
    String WORKSPACES = PROFILE_PREFIX + "redirectToDashboardWorkspacesAction";
    String PREFERENCES = PROFILE_PREFIX + "showPreferences";
  }

  interface Git {

    String GIT_MENU_PREFFIX = "gwt-debug-topmenu/Git/";
    String GIT = "gwt-debug-MenuItem/git-true";
    String ADD_TO_INDEX = GIT_MENU_PREFFIX + "gitAddToIndex";
    String RESET = GIT_MENU_PREFFIX + "gitResetToCommit";
    String REMOVE_FROM_INDEX = GIT_MENU_PREFFIX + "gitRemoveFromIndexCommit";
    String COMMIT = GIT_MENU_PREFFIX + "gitCommit";
    String BRANCHES = GIT_MENU_PREFFIX + "gitBranches";
    String CHECKOUT_REFERENCE = GIT_MENU_PREFFIX + "gitCheckoutReference";
    String MERGE = GIT_MENU_PREFFIX + "gitMerge";
    String RESET_INDEX = GIT_MENU_PREFFIX + "gitResetFiles";
    String SHOW_HISTORY = GIT_MENU_PREFFIX + "gitHistory";
    String STATUS = GIT_MENU_PREFFIX + "gitStatus";
    String PROJECT_GIT_URL = GIT_MENU_PREFFIX + "gitUrl";
    String INITIALIZE_REPOSITORY = GIT_MENU_PREFFIX + "gitInitRepository";
    String DELETE_REPOSITORY = GIT_MENU_PREFFIX + "gitDeleteRepository";
    String REVERT_COMMIT = GIT_MENU_PREFFIX + "gitRevertCommit";

    interface Remotes {
      String REMOTES_TOP = "gwt-debug-topmenu/Git/gitRemoteGroup";
      String FETCH = "gwt-debug-topmenu/Git/Remotes.../gitFetch";
      String PULL = "gwt-debug-topmenu/Git/Remotes.../gitPull";
      String PUSH = "gwt-debug-topmenu/Git/Remotes.../gitPush";
      String REMOTES = "gwt-debug-topmenu/Git/Remotes.../gitRemote";
    }

    interface Compare {
      String COMPARE_TOP = "gwt-debug-topmenu/Git/gitCompareGroup";
      String COMPARE_LATEST_VER = "gwt-debug-topmenu/Git/Compare/gitCompareWithLatest";
      String COMPARE_WITH_BRANCH = "gwt-debug-topmenu/Git/Compare/gitCompareWithBranch";
      String COMPARE_WITH_REVISION = "gwt-debug-topmenu/Git/Compare/gitCompareWithRevision";
    }
  }

  interface Help {
    String HELP = "gwt-debug-MenuItem/helpGroup-true";
    String SETTINGS = "gwt-debug-topmenu/Help/setupProjectAction";
    String SUPPORT = "gwt-debug-topmenu/Help/redirectToSupport";
    String ABOUT = "gwt-debug-topmenu/Help/showAbout";
  }

  interface Machine {
    String MACHINE = "gwt-debug-MenuItem/machine-true";
    String CREATE = "topmenu/Machine/Create...";
    String RESTART = "gwt-debug-topmenu/Machine/restartMachine";
    String DESTROY = "gwt-debug-topmenu/Machine/destroyMachine";
    String CREATE_SNAPSHOT = "gwt-debug-topmenu/Machine/createSnapshot";
  }

  interface CommandList {
    String WS_MACHINE_LIST = "gwt-debug-dropDownHeader";
    String ITEM_PREFFIX_ID = "gwt-debug-CommandsGroup/";
    String COMMAND_LIST_XPATH = "//div[@id='gwt-debug-dropDownHeader'][2]";
  }

  public static final String TEST_NG_TEST_DROP_DAWN_ITEM = "topmenu/Run/Test/Run Test";
  public static final String JUNIT_TEST_DROP_DAWN_ITEM =
      "gwt-debug-topmenu/Run/Test/TestJUnitActionRun";

  public static final String TEST_DROP_DAWN_ITEM = "gwt-debug-topmenu/Run/Test/RunTest";
}
