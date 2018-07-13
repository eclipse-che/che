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
import static java.nio.file.Paths.get;

import com.google.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
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
  public static final String TEXT_TO_INSERT = NameGenerator.generate("", 10);

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
  @Inject private CodenvyEditor editor;
  @Inject private AskDialog askDialog;

  @BeforeClass
  public void setup() throws Exception {
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        get(PROJECT_SOURCES.toURI()),
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
    final String pathToUploadingFileInsideTheProject =
        format("%s/%s", PROJECT_NAME, uploadingFileName);
    final Path localPathToFileToUpload =
        get(PROJECT_SOURCES.getPath())
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

    // Check that uploading file doesn't overwrite existed one
    // when change the file
    projectExplorer.openItemByPath(pathToUploadingFileInsideTheProject);
    editor.typeTextIntoEditor(TEXT_TO_INSERT);

    // when re-upload the file
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT, TestMenuCommandsConstants.Project.UPLOAD_FILE);
    uploadFileDialogPage.waitOnOpen();
    uploadFileDialogPage.selectResourceToUpload(localPathToFileToUpload);
    uploadFileDialogPage.clickOnUploadButton();

    // then there is changes remained after uploading
    uploadFileDialogPage.waitOnClose();
    notificationPopup.waitExpectedMessageOnProgressPanelAndClosed(
        format("File '%s' has uploaded successfully", uploadingFileName));
    projectExplorer.waitVisibleItem(pathToUploadingFileInsideTheProject);
    projectExplorer.openItemByPath(pathToUploadingFileInsideTheProject);
    editor.waitTextIntoEditor(TEXT_TO_INSERT);
  }

  @Test
  public void shouldUploadFileWithOverwriting() throws IOException {
    // given
    final String uploadingFileName = "AppController.java";
    final String pathToUploadingFileInsideTheProject =
        format("%s/%s", PROJECT_NAME, uploadingFileName);
    final Path localPathToFileToUpload =
        get(PROJECT_SOURCES.getPath())
            .resolve("src/main/java/org/eclipse/qa/examples")
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

    // Check that uploading file overwrites existed one
    // when change the file
    projectExplorer.openItemByPath(pathToUploadingFileInsideTheProject);
    editor.typeTextIntoEditor(TEXT_TO_INSERT);

    // when re-upload the file
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT, TestMenuCommandsConstants.Project.UPLOAD_FILE);
    uploadFileDialogPage.waitOnOpen();
    uploadFileDialogPage.selectResourceToUpload(localPathToFileToUpload);
    uploadFileDialogPage.selectOverwriteIfFileExistsCheckbox();
    uploadFileDialogPage.clickOnUploadButton();

    // then there are no changes remained after uploading
    uploadFileDialogPage.waitOnClose();
    notificationPopup.waitExpectedMessageOnProgressPanelAndClosed(
        format("File '%s' has uploaded successfully", uploadingFileName));
    projectExplorer.waitVisibleItem(pathToUploadingFileInsideTheProject);
    projectExplorer.openItemByPath(pathToUploadingFileInsideTheProject);
    editor.waitTextNotPresentIntoEditor(TEXT_TO_INSERT);
  }

  @Test
  public void shouldUploadDirectoryWithDefaultOptions() throws IOException {
    // given
    final String uploadingFileName = "Aclass.java";
    final String uploadingFilePath = "sample/" + uploadingFileName;
    final String pathToUploadingFileInsideTheProject =
        format("%s/%s", PROJECT_NAME, uploadingFilePath);
    final Path localPathToFolderToUpload =
        get(PROJECT_SOURCES.getPath()).resolve("src/main/java/che/eclipse");

    // open upload directory window
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT, TestMenuCommandsConstants.Project.UPLOAD_FOLDER);
    uploadDirectoryDialogPage.waitOnOpen();

    // when
    uploadDirectoryDialogPage.selectResourceToUpload(localPathToFolderToUpload);
    uploadDirectoryDialogPage.clickOnUploadButton();

    // then
    projectExplorer.quickRevealToItemWithJavaScript(pathToUploadingFileInsideTheProject);
    projectExplorer.waitVisibleItem(pathToUploadingFileInsideTheProject);

    // Check that uploading directory doesn't overwrite existed one
    // when change the directory - remove uploading file
    projectExplorer.waitAndSelectItem(pathToUploadingFileInsideTheProject);
    menu.runAndWaitCommand(
        TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    projectExplorer.waitRemoveItemsByPath(pathToUploadingFileInsideTheProject);

    // when re-upload the directory
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT, TestMenuCommandsConstants.Project.UPLOAD_FOLDER);
    uploadDirectoryDialogPage.waitOnOpen();
    uploadDirectoryDialogPage.selectResourceToUpload(localPathToFolderToUpload);
    uploadDirectoryDialogPage.clickOnUploadButton();

    // then there is changes remained after uploading
    uploadFileDialogPage.waitOnClose();
    projectExplorer.quickRevealToItemWithJavaScript(
        get(pathToUploadingFileInsideTheProject).getParent().toString());
    projectExplorer.waitItemInvisibility(pathToUploadingFileInsideTheProject);
  }

  @Test
  public void shouldUploadDirectoryWithOverwriting() throws IOException {
    // given
    final String uploadingFileName = "AppController.java";
    final String uploadingFilePath = "qa/examples/" + uploadingFileName;
    final String pathToUploadingFileInsideTheProject =
        format("%s/%s", PROJECT_NAME, uploadingFilePath);
    final Path localPathToFolderToUpload =
        get(PROJECT_SOURCES.getPath()).resolve("src/main/java/org/eclipse");

    // open upload directory window
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT, TestMenuCommandsConstants.Project.UPLOAD_FOLDER);
    uploadDirectoryDialogPage.waitOnOpen();

    // when
    uploadDirectoryDialogPage.selectResourceToUpload(localPathToFolderToUpload);
    uploadDirectoryDialogPage.clickOnUploadButton();

    // then
    projectExplorer.quickRevealToItemWithJavaScript(pathToUploadingFileInsideTheProject);
    projectExplorer.waitVisibleItem(pathToUploadingFileInsideTheProject);

    // Check that uploading directory overwrites existed one
    // when change the directory - remove uploading file
    projectExplorer.waitAndSelectItem(pathToUploadingFileInsideTheProject);
    menu.runAndWaitCommand(
        TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    projectExplorer.waitRemoveItemsByPath(pathToUploadingFileInsideTheProject);

    // when re-upload the directory with overwriting
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT, TestMenuCommandsConstants.Project.UPLOAD_FOLDER);
    uploadDirectoryDialogPage.waitOnOpen();
    uploadDirectoryDialogPage.selectResourceToUpload(localPathToFolderToUpload);
    uploadDirectoryDialogPage.selectOverwriteIfFileExistsCheckbox();
    uploadDirectoryDialogPage.clickOnUploadButton();

    // then there are no changes remained after uploading
    uploadFileDialogPage.waitOnClose();
    projectExplorer.quickRevealToItemWithJavaScript(pathToUploadingFileInsideTheProject);
    projectExplorer.waitVisibleItem(pathToUploadingFileInsideTheProject);
  }

  @Test
  public void shouldUploadDirectoryDoNotSkippingRoot() throws IOException {
    // given
    final String uploadingFileName = "AppController.java";
    final String uploadingFilePath = "examples/" + uploadingFileName;
    final String pathToUploadingFileInsideTheProject =
        format("%s/%s", PROJECT_NAME, uploadingFilePath);
    final Path localPathToFolderToUpload =
        get(PROJECT_SOURCES.getPath()).resolve("src/main/java/org/eclipse");

    // open upload directory window
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT, TestMenuCommandsConstants.Project.UPLOAD_FOLDER);
    uploadDirectoryDialogPage.waitOnOpen();

    // when
    uploadDirectoryDialogPage.selectResourceToUpload(localPathToFolderToUpload);
    uploadDirectoryDialogPage.selectSkipRootFolderCheckbox();
    uploadDirectoryDialogPage.clickOnUploadButton();

    // then
    projectExplorer.quickRevealToItemWithJavaScript(pathToUploadingFileInsideTheProject);
    projectExplorer.waitVisibleItem(pathToUploadingFileInsideTheProject);
  }
}
