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

import static java.lang.String.format;

import com.google.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.upload.UploadDirectoryDialogPage;
import org.eclipse.che.selenium.pageobject.upload.UploadFileDialogPage;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class UploadIntoProjectTest {
  private static final String PROJECT_NAME = "TestProject";
  private static final URL PROJECT_SOURCES =
      UploadIntoProjectTest.class.getResource("/projects/default-spring-project");

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private Wizard projectWizard;
  @Inject private Menu menu;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private UploadFileDialogPage uploadFileDialogPage;
  @Inject private UploadDirectoryDialogPage uploadDirectoryDialogPage;
  @Inject private NotificationsPopupPanel notificationPopup;

  @BeforeClass
  public void setup() throws Exception {
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(PROJECT_SOURCES.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    ide.open(testWorkspace);
    projectExplorer.waitVisibleItem(PROJECT_NAME);
  }

  @BeforeMethod
  public void selectProjectItem() {
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
  }

  @Test
  public void shouldUploadFileWithDefaultOptions() throws URISyntaxException, IOException {
    // given
    final String uploadingFileName = "Aclass.java";
    final Path localPathToFileToUpload =
        Paths.get(PROJECT_SOURCES.getPath())
            .resolve("src/main/java/che/eclipse/sample")
            .resolve(uploadingFileName);

    // open upload file window
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT, TestMenuCommandsConstants.Project.UPLOAD_FILE);

    uploadFileDialogPage.waitOnOpen();

    // when
    uploadFileDialogPage.selectResourceToUpload(localPathToFileToUpload);
    uploadFileDialogPage.clickOnUploadButton();

    // then
    uploadFileDialogPage.waitOnClose();
    notificationPopup.waitExpectedMessageOnProgressPanelAndClosed(
        format("File '%s' has uploaded successfully", uploadingFileName));
    projectExplorer.waitVisibleItem(format("%s/%s", PROJECT_NAME, uploadingFileName));
  }

  @Test
  public void shouldUploadFileWithOverwriting() {
    // open upload file window
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT, TestMenuCommandsConstants.Project.UPLOAD_FOLDER);

    uploadDirectoryDialogPage.waitOnOpen();
  }

  @Test
  public void shouldUploadDirectoryWithDefaultOptions() throws IOException {
    // given
    final String uploadingFileName = "Aclass.java";
    final String uploadingFilePath = "che/eclipse/sample/" + uploadingFileName;
    final Path localPathToFolderToUpload =
        Paths.get(PROJECT_SOURCES.getPath()).resolve("src/main/java");

    // open upload file window
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT, TestMenuCommandsConstants.Project.UPLOAD_FOLDER);

    uploadDirectoryDialogPage.waitOnOpen();

    // when
    uploadDirectoryDialogPage.selectResourceToUpload(localPathToFolderToUpload);
    uploadDirectoryDialogPage.clickOnUploadButton();

    // then
    projectExplorer.quickRevealToItemWithJavaScript(
        format("%s/%s", PROJECT_NAME, uploadingFilePath));
  }
}
