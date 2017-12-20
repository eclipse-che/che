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
package org.eclipse.che.selenium.core.constant;

/** */
public final class TestProjectExplorerContextMenuConstants {

  /** First level context menu items */
  public static final String NEW = "gwt-debug-contextMenu/newGroup";

  public static final String COMMANDS = "gwt-debug-contextMenu/commandsActionGroup";
  public static final String PREVIEW = "gwt-debug-contextMenu/previewHTML";
  public static final String SHOW_REFERENCES = "gwt-debug-contextMenu/showReference";
  public static final String GO_INTO = "gwt-debug-contextMenu/goInto";
  public static final String GO_BACK = "gwt-debug-contextMenu/goInto";
  public static final String CUT = "gwt-debug-contextMenu/cut";
  public static final String PASTE = "gwt-debug-contextMenu/paste";
  public static final String RENAME = "gwt-debug-contextMenu/renameResource";
  public static final String DELETE = "gwt-debug-contextMenu/deleteItem";
  public static final String DOWNLOAD = "gwt-debug-contextMenu/downloadItemAction";
  public static final String CONVERT_TO_PROJECT = "gwt-debug-contextMenu/convertFolderToProject";
  public static final String BUILD_PATH = "gwt-debug-contextMenu/markDirectoryAsSourceGroup";
  public static final String EDIT = "gwt-debug-contextMenu/editFile";
  public static final String MAVEN = "gwt-debug-contextMenu/mavenGroupContextMenu";
  public static final String REFRESH = "gwt-debug-contextMenu/refreshPathAction";
  public static final String REIMPORT =
      "gwt-debug-contextMenu/Maven/reimportMavenDependenciesAction";
  public static final String TEST = "gwt-debug-contextMenu/TestingContextGroup";

  /** Submenu for new items */
  public static final class SubMenuNew {
    public static final String JAVA_CLASS = "gwt-debug-contextMenu/New/newJavaClass";
    public static final String JAVA_PACKAGE = "gwt-debug-contextMenu/New/newJavaPackage";
    public static final String FILE = "gwt-debug-contextMenu/New/newFile";
    public static final String FOLDER = "gwt-debug-contextMenu/New/newFolder";
    public static final String XML_FILE = "gwt-debug-contextMenu/New/newXmlFile";
    public static final String CSS_FILE = "gwt-debug-contextMenu/New/newCssFile";
    public static final String LESS_FILE = "gwt-debug-contextMenu/New/newLessFile";
    public static final String HTML_FILE = "gwt-debug-contextMenu/New/newHtmlFile";
    public static final String JAVASCRIPT_FILE = "gwt-debug-contextMenu/New/newJavaScriptFile";
    public static final String PYTHON_FILE = "gwt-debug-contextMenu/New/pythonFile";
    public static final String C_FILE = "gwt-debug-contextMenu/New/newCFile";
    public static final String C_PLUS_PLUS_FILE = "gwt-debug-contextMenu/New/newCppFile";
    public static final String H_FILE = "gwt-debug-contextMenu/New/newHFile";
  }

  /** Submenu for Build Path Configuration (use for Java project only) */
  public static final class SubMenuBuildPath {
    public static final String USE_AS_SOURCE_FOLDER =
        "gwt-debug-contextMenu/Build Path/markDirectoryAsSource";
    public static final String UNMARK_AS_SOURCE_FOLDER =
        "gwt-debug-contextMenu/Build Path/unmarkDirectoryAsSource";
    public static final String CONFIGURE_CLASSPATH =
        "gwt-debug-contextMenu/Build Path/projectProperties";
  }

  /** Submenu for Test Runner */
  public static class SubMenuTest {
    public static final String TEST_NG_CLASS = "contextMenu/Run Test/TestNG Class";
    public static final String TEST_NG_PROJECT = "topmenu/Run/Test/TestNG Project";
    public static final String TEST_NG_XML_SUITE = "topmenu/Run/Test/TestNG XML Suite";
    public static final String JUNIT_CLASS = "contextMenu/Run Test/JUnit Class";
    public static final String JUNIT_PROJECT = "topmenu/Run/Test/JUnit Project";
  }
}
