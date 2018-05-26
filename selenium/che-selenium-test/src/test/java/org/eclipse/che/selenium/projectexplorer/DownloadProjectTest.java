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
package org.eclipse.che.selenium.projectexplorer;

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.DOWNLOAD_AS_ZIP;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.utils.WaitUtils.sleepQuietly;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.DownloadedFileUtil;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class DownloadProjectTest {
  private static final String TEST_PROJECT_1 = "TestProject1";
  private static final String TEST_PROJECT_2 = "TestProject2";
  private static final ImmutableList<String> PROJECT_NAMES =
      ImmutableList.of(TEST_PROJECT_1, TEST_PROJECT_2);

  private static final String TEST_FILE_NAME = "README.md";
  private static final String TEST_DIRECTORY_NAME = "src";

  private static final int MAX_ATTEMPTS = 5;

  private static final URL PROJECT_SOURCES =
      DownloadProjectTest.class.getResource("/projects/ProjectWithDifferentTypeOfFiles");

  private static final String DOWNLOADED_PROJECTS_PACKAGE_NAME = "download.zip";
  private static final String DOWNLOADED_TEST_PROJECT_1_PACKAGE_NAME = TEST_PROJECT_1 + ".zip";
  private static final String DOWNLOADED_TEST_DIRECTORY_PACKAGE_NAME = TEST_DIRECTORY_NAME + ".zip";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private DownloadedFileUtil downloadedFileUtil;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private DefaultTestUser user;

  @BeforeClass
  public void setUp() throws Exception {
    PROJECT_NAMES.forEach(
        testProject -> {
          try {
            testProjectServiceClient.importProject(
                workspace.getId(),
                Paths.get(PROJECT_SOURCES.toURI()),
                testProject,
                ProjectTemplates.MAVEN_SPRING);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });

    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
  }

  @BeforeMethod
  private void cleanUpDownloads() throws IOException {
    downloadedFileUtil.removeDownloadedFiles(
        seleniumWebDriver.getSessionId(),
        DOWNLOADED_PROJECTS_PACKAGE_NAME,
        DOWNLOADED_TEST_PROJECT_1_PACKAGE_NAME,
        DOWNLOADED_TEST_DIRECTORY_PACKAGE_NAME,
        TEST_FILE_NAME);
  }

  @Test
  private void downloadAllProjectsTest() throws IOException {
    // given
    List<String> expectedPackageFileList =
        asList(
            "TestProject1/.che/classpath",
            "TestProject1/README.md",
            "TestProject1/pom.xml",
            "TestProject1/src/main/java/org/eclipse/qa/examples/AppController.java",
            "TestProject1/src/main/webapp/WEB-INF/jsp/guess_num.jsp",
            "TestProject1/src/main/webapp/WEB-INF/spring-servlet.xml",
            "TestProject1/src/main/webapp/WEB-INF/web.xml",
            "TestProject1/src/main/webapp/index.jsp",
            "TestProject2/.che/classpath",
            "TestProject2/README.md",
            "TestProject2/pom.xml",
            "TestProject2/src/main/java/org/eclipse/qa/examples/AppController.java",
            "TestProject2/src/main/webapp/WEB-INF/jsp/guess_num.jsp",
            "TestProject2/src/main/webapp/WEB-INF/spring-servlet.xml",
            "TestProject2/src/main/webapp/WEB-INF/web.xml",
            "TestProject2/src/main/webapp/index.jsp");

    // when
    menu.runCommand(WORKSPACE, DOWNLOAD_AS_ZIP);

    // then
    assertEquals(
        getPackageFileList(DOWNLOADED_PROJECTS_PACKAGE_NAME), expectedPackageFileList.toString());
  }

  @Test
  private void downloadSingleProjectTest() throws IOException {
    // given
    List<String> expectedPackageFileList =
        asList(
            ".che/classpath",
            "README.md",
            "pom.xml",
            "src/main/java/org/eclipse/qa/examples/AppController.java",
            "src/main/webapp/WEB-INF/jsp/guess_num.jsp",
            "src/main/webapp/WEB-INF/spring-servlet.xml",
            "src/main/webapp/WEB-INF/web.xml",
            "src/main/webapp/index.jsp");

    projectExplorer.waitAndSelectItem(TEST_PROJECT_1);

    // when
    projectExplorer.openContextMenuByPathSelectedItem(TEST_PROJECT_1);
    projectExplorer.clickOnItemInContextMenu(ContextMenuFirstLevelItems.DOWNLOAD);

    // then
    assertEquals(
        getPackageFileList(DOWNLOADED_TEST_PROJECT_1_PACKAGE_NAME),
        expectedPackageFileList.toString());

    // when
    downloadedFileUtil.removeDownloadedFiles(
        seleniumWebDriver.getSessionId(), DOWNLOADED_TEST_PROJECT_1_PACKAGE_NAME);

    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.DOWNLOAD_AS_ZIP);

    // then
    assertEquals(
        getPackageFileList(DOWNLOADED_TEST_PROJECT_1_PACKAGE_NAME),
        expectedPackageFileList.toString());
  }

  @Test
  private void downloadDirectoryTest() throws IOException {
    // given
    String pathToTestDirectory = "TestProject1/" + TEST_DIRECTORY_NAME;
    List<String> expectedPackageFileList =
        asList(
            "main/java/org/eclipse/qa/examples/AppController.java",
            "main/webapp/WEB-INF/jsp/guess_num.jsp",
            "main/webapp/WEB-INF/spring-servlet.xml",
            "main/webapp/WEB-INF/web.xml",
            "main/webapp/index.jsp");

    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitAndSelectItem(pathToTestDirectory);

    // when
    projectExplorer.openContextMenuByPathSelectedItem(pathToTestDirectory);
    projectExplorer.clickOnItemInContextMenu(ContextMenuFirstLevelItems.DOWNLOAD);

    // then
    String packageFileList = getPackageFileList(DOWNLOADED_TEST_DIRECTORY_PACKAGE_NAME);
    assertEquals(packageFileList, expectedPackageFileList.toString());
  }

  @Test
  private void downloadFileTest() throws IOException {
    // given
    String pathToTestFile = "TestProject1/" + TEST_FILE_NAME;
    String testFileContent = "Developer Workspace";

    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitAndSelectItem(pathToTestFile);

    // when
    projectExplorer.openContextMenuByPathSelectedItem(pathToTestFile);
    projectExplorer.clickOnItemInContextMenu(ContextMenuFirstLevelItems.DOWNLOAD);

    // then
    String downloadedFileContent = getDownloadedFileContent(TEST_FILE_NAME);
    assertEquals(downloadedFileContent, testFileContent);
  }

  private String getDownloadedFileContent(String testFileName) throws IOException {
    IOException lastException = null;
    for (int i = 0; i < MAX_ATTEMPTS; i++) {
      try {
        return downloadedFileUtil.getDownloadedFileContent(
            seleniumWebDriver.getSessionId(), testFileName);
      } catch (IOException e) {
        lastException = e;
        sleepQuietly(TestTimeoutsConstants.MINIMUM_SEC);
      }
    }

    throw lastException;
  }

  private String getPackageFileList(String downloadedTestProject1PackageName) throws IOException {
    IOException lastException = null;
    for (int i = 0; i < MAX_ATTEMPTS; i++) {
      try {
        return downloadedFileUtil
            .getPackageFileList(seleniumWebDriver.getSessionId(), downloadedTestProject1PackageName)
            .toString();
      } catch (IOException e) {
        lastException = e;
        sleepQuietly(TestTimeoutsConstants.MINIMUM_SEC);
      }
    }

    throw lastException;
  }
}
