/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.constant;

/** */
public final class TestProjectExplorerContextMenuConstants {

  public interface ContextMenuItems {
    String get();
  }

  /** First level of project explorer's context menu items */
  public enum ContextMenuFirstLevelItems implements ContextMenuItems {
    NEW("gwt-debug-contextMenu/newGroup"),
    COMMANDS("gwt-debug-contextMenu/commandsActionGroup"),
    PREVIEW("gwt-debug-contextMenu/previewHTML"),
    SHOW_REFERENCES("gwt-debug-contextMenu/showReference"),
    GO_INTO("gwt-debug-contextMenu/goInto"),
    GO_BACK("gwt-debug-contextMenu/goInto"),
    OPEN_IN_TERMINAL("gwt-debug-contextMenu/openInTerminal"),
    CUT("gwt-debug-contextMenu/cut"),
    COPY("gwt-debug-contextMenu/copy"),
    PASTE("gwt-debug-contextMenu/paste"),
    RENAME("gwt-debug-contextMenu/renameResource"),
    DELETE("gwt-debug-contextMenu/deleteItem"),
    DOWNLOAD("gwt-debug-contextMenu/downloadItemAction"),
    CONVERT_TO_PROJECT("gwt-debug-contextMenu/convertFolderToProject"),
    BUILD_PATH("gwt-debug-contextMenu/markDirectoryAsSourceGroup"),
    EDIT("gwt-debug-contextMenu/editFile"),
    MAVEN("gwt-debug-contextMenu/mavenGroupContextMenu"),
    REFRESH("gwt-debug-contextMenu/refreshPathAction"),
    REIMPORT("gwt-debug-contextMenu/Maven/reimportMavenDependenciesAction"),
    TEST("gwt-debug-contextMenu/TestingContextGroup"),
    ADD_TO_FILE_WATCHER_EXCLUDES("gwt-debug-contextMenu/Add to File Watcher exclusion list"),
    REMOVE_FROM_FILE_WATCHER_EXCLUDES(
        "gwt-debug-contextMenu/Remove from File Watcher exclusion list");

    private final String itemId;

    ContextMenuFirstLevelItems(String itemId) {
      this.itemId = itemId;
    }

    @Override
    public String get() {
      return this.itemId;
    }
  }

  /** Submenu for "new" project explorer context menu items */
  public enum SubMenuNew implements ContextMenuItems {
    JAVA_CLASS("gwt-debug-contextMenu/New/newJavaClass"),
    JAVA_PACKAGE("gwt-debug-contextMenu/New/newJavaPackage"),
    FILE("gwt-debug-contextMenu/New/newFile"),
    FOLDER("gwt-debug-contextMenu/New/newFolder"),
    XML_FILE("gwt-debug-contextMenu/New/newXmlFile"),
    CSS_FILE("gwt-debug-contextMenu/New/newCssFile"),
    LESS_FILE("gwt-debug-contextMenu/New/newLessFile"),
    HTML_FILE("gwt-debug-contextMenu/New/newHtmlFile"),
    JAVASCRIPT_FILE("gwt-debug-contextMenu/New/newJavaScriptFile"),
    PYTHON_FILE("gwt-debug-contextMenu/New/pythonFile"),
    C_FILE("gwt-debug-contextMenu/New/newCFile"),
    C_PLUS_PLUS_FILE("gwt-debug-contextMenu/New/newCppFile"),
    H_FILE("gwt-debug-contextMenu/New/newHFile");

    private final String itemId;

    SubMenuNew(String itemId) {
      this.itemId = itemId;
    }

    @Override
    public String get() {
      return this.itemId;
    }
  }

  /** Submenu for (uses for Java project only) */
  public enum SubMenuBuildPath implements ContextMenuItems {
    USE_AS_SOURCE_FOLDER("gwt-debug-contextMenu/Build Path/markDirectoryAsSource"),
    UNMARK_AS_SOURCE_FOLDER("gwt-debug-contextMenu/Build Path/unmarkDirectoryAsSource"),
    CONFIGURE_CLASSPATH("gwt-debug-contextMenu/Build Path/projectProperties");

    private final String itemId;

    SubMenuBuildPath(String itemId) {
      this.itemId = itemId;
    }

    @Override
    public String get() {
      return this.itemId;
    }
  }

  /** Submenu for {@link ContextMenuFirstLevelItems#TEST} */
  public enum SubMenuTest implements ContextMenuItems {
    TEST_NG_CLASS("contextMenu/Run Test/TestNG Class"),
    TEST_NG_PROJECT("topmenu/Run/Test/TestNG Project"),
    TEST_NG_XML_SUITE("topmenu/Run/Test/TestNG XML Suite"),
    JUNIT_CLASS("contextMenu/Run Test/JUnit Class"),
    JUNIT_PROJECT("topmenu/Run/Test/JUnit Project");

    private final String itemId;

    SubMenuTest(String itemId) {
      this.itemId = itemId;
    }

    @Override
    public String get() {
      return this.itemId;
    }
  }

  /** Submenu for {@link ContextMenuFirstLevelItems#COMMANDS} */
  public enum ContextMenuCommandGoals implements ContextMenuItems {
    COMMON_GOAL("gwt-debug-contextMenu/Commands/goal_Common"),
    BUILD_GOAL("gwt-debug-contextMenu/Commands/goal_Build"),
    RUN_GOAL("gwt-debug-contextMenu/Commands/goal_Run"),
    DEBUG_GOAL("gwt-debug-contextMenu/Commands/goal_Debug");

    private final String itemId;

    ContextMenuCommandGoals(String itemId) {
      this.itemId = itemId;
    }

    @Override
    public String get() {
      return itemId;
    }
  }
}
