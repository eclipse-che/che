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
  public static final String NEW = "contextMenu/New";

  public static final String COMMANDS = "contextMenu/Commands";
  public static final String PREVIEW = "contextMenu/Preview";
  public static final String SHOW_REFERENCES = "contextMenu/Show References";
  public static final String GO_INTO = "gwt-debug-contextMenu/goInto";
  public static final String GO_BACK = "contextMenu/Go Back";
  public static final String CUT = "contextMenu/Cut";
  public static final String PASTE = "contextMenu/Paste";
  public static final String RENAME = "contextMenu/Rename...";
  public static final String DELETE = "contextMenu/Delete...";
  public static final String DOWNLOAD = "contextMenu/Download...";
  public static final String CONVERT_TO_PROJECT = "contextMenu/Convert To Project";
  public static final String BUILD_PATH = "contextMenu/Build Path";
  public static final String EDIT = "contextMenu/Edit file";
  public static final String MAVEN = "contextMenu/Maven";
  public static final String REFRESH = "gwt-debug-contextMenu/refreshPathAction";
  public static final String REIMPORT = "contextMenu/Maven/Reimport";
  public static final String TEST = "gwt-debug-contextMenu/TestingContextGroup";

  /** Submenu for new items */
  public static final class SubMenuNew {
    public static final String JAVA_CLASS = "contextMenu/New/Java Class";
    public static final String JAVA_PACKAGE = "contextMenu/New/Java Package";
    public static final String FILE = "contextMenu/New/File";
    public static final String FOLDER = "contextMenu/New/Folder";
    public static final String XML_FILE = "contextMenu/New/XML File";
    public static final String CSS_FILE = "contextMenu/New/CSS File";
    public static final String LESS_FILE = "contextMenu/New/Less File";
    public static final String HTML_FILE = "contextMenu/New/HTML File";
    public static final String JAVASCRIPT_FILE = "contextMenu/New/JavaScript File";
    public static final String PYTHON_FILE = "contextMenu/New/Python File";
    public static final String C_FILE = "contextMenu/New/New C File";
    public static final String C_PLUS_PLUS_FILE = "contextMenu/New/New C++ File";
    public static final String H_FILE = "contextMenu/New/New H File";
  }

  /** Submenu for Build Path Configuration (use for Java project only) */
  public static final class SubMenuBuildPath {
    public static final String USE_AS_SOURCE_FOLDER = "contextMenu/Build Path/Use as Source Folder";
    public static final String UNMARK_AS_SOURCE_FOLDER =
        "contextMenu/Build Path/Unmark as Source Folder";
    public static final String CONFIGURE_CLASSPATH = "contextMenu/Build Path/Configure Classpath";
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
