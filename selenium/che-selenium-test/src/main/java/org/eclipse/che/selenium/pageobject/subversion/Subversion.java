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
package org.eclipse.che.selenium.pageobject.subversion;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.openqa.selenium.support.PageFactory;

/**
 * @author Aleksandr Shmaraev
 * @author Andrey Chizhikov
 */
@Singleton
public class Subversion {

  private final SvnStatusBar svnStatusBar;
  private final SvnViewLog svnViewLog;
  private final SvnUpdateToRevision svnUpdateToRevision;
  private final SvnCommit svnCommit;
  private final SvnResolve svnResolve;
  private final SvnCopy svnCopy;
  private final SvnMove svnMove;
  private final SvnMerge svnMerge;
  private final SvnExport svnExport;
  private final SvnRevert svnRevert;
  private final SvnGetLock svnGetLock;
  private final SvnReleaseLock svnReleaseLock;
  private final SvnProperties svnProperties;
  private final SvnAuthentication svnAuthentication;
  private final SeleniumWebDriver seleniumWebDriver;
  private final Menu menu;
  private final Loader loader;
  private final ImportProjectFromLocation importProjectFromLocation;

  @Inject
  public Subversion(
      SvnStatusBar svnStatusBar,
      SvnViewLog svnViewLog,
      SvnUpdateToRevision svnUpdateToRevision,
      SvnCommit svnCommit,
      SvnResolve svnResolve,
      SvnCopy svnCopy,
      SvnMove svnMove,
      SvnMerge svnMerge,
      SvnExport svnExport,
      SvnRevert svnRevert,
      SvnGetLock svnGetLock,
      SvnReleaseLock svnReleaseLock,
      SvnProperties svnProperties,
      SvnAuthentication svnAuthentication,
      SeleniumWebDriver seleniumWebDriver,
      Menu menu,
      Loader loader,
      ImportProjectFromLocation importProjectFromLocation) {
    this.svnStatusBar = svnStatusBar;
    this.svnViewLog = svnViewLog;
    this.svnUpdateToRevision = svnUpdateToRevision;
    this.svnCommit = svnCommit;
    this.svnResolve = svnResolve;
    this.svnCopy = svnCopy;
    this.svnMove = svnMove;
    this.svnMerge = svnMerge;
    this.svnExport = svnExport;
    this.svnRevert = svnRevert;
    this.svnGetLock = svnGetLock;
    this.svnReleaseLock = svnReleaseLock;
    this.svnProperties = svnProperties;
    this.svnAuthentication = svnAuthentication;
    this.seleniumWebDriver = seleniumWebDriver;
    this.menu = menu;
    this.loader = loader;
    this.importProjectFromLocation = importProjectFromLocation;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /** @return last revision from svn log */
  public String getLatestRevisionSvnLog() throws Exception {
    menu.runCommand(
        TestMenuCommandsConstants.Subversion.SUBVERSION,
        TestMenuCommandsConstants.Subversion.SVN_VIEW_LOG);
    loader.waitOnClosed();
    svnViewLog.waitSvnLogFormOpened();
    String latestRevision = svnViewLog.getLatestRevision();
    svnViewLog.clickSvnLogFormCancel();
    svnViewLog.waitSvnLogFormClosed();
    return latestRevision;
  }

  /** wait until svn update to revision form opens */
  public void waitSvnUpdateToRevisionFormOpened() {
    svnUpdateToRevision.waitMainFormIsOpen();
  }

  public void waitSvnUpdateToRevisionFormClosed() {
    svnUpdateToRevision.waitMainFormIsClosed();
  }

  /** select revision in svn update revision */
  public void selectSvnUpdateToRevision() {
    svnUpdateToRevision.selectRevision();
  }

  /**
   * type reversion name
   *
   * @param revision expected reversion name
   */
  public void typeSvnUpdateToRevisionName(String revision) {
    svnUpdateToRevision.typeTextRevision(revision);
  }

  public void clickSvnUpdateToRevisionButtonUpdate() {
    svnUpdateToRevision.clickOnUpdateBtn();
    waitSvnUpdateToRevisionFormClosed();
  }

  /** wait while svn commit form opened */
  public void waitSvnCommitFormOpened() {
    svnCommit.waitMainFormOpened();
  }

  public void waitSvnCommitFormClosed() {
    svnCommit.waitMainFormClosed();
  }

  /**
   * type commit message
   *
   * @param message commit title
   */
  public void typeSvnCommitMessage(String message) {
    svnCommit.typeCommitMess(message);
  }

  /**
   * click on the 'Diff' button in the 'Commit...' form
   *
   * @param fileName is the expected filename
   * @param folderName is the expected folder name
   */
  public void clickSvnCommitDiffButton(String fileName, String folderName) {
    svnCommit.waitMainFormOpened();
    svnCommit.clickDiffButton(fileName, folderName);
  }

  /** click on the 'Closed' button in the diff view of the 'Commit...' form */
  public void clickSvnCommitClosedButtonDiffView() throws InterruptedException {
    svnCommit.clickCloseDiffViewBtn();
  }

  /**
   * wait the text into 'Commit...' diff view
   *
   * @param expContent is the expected text
   */
  public void waitTextDiffView(final String expContent) {
    svnCommit.waitTextDiffView(expContent);
  }

  public void clickSvnCommitButtonCommit() {
    svnCommit.clickCommitBtn();
    waitSvnCommitFormClosed();
  }

  public void clickSvnCommiCancelButtont() {
    svnCommit.clickCancelBtn();
    waitSvnCommitFormClosed();
  }

  /** wait the svn resolve form is opened */
  public void waitSvnResolveFormOpened() {
    svnResolve.waitResolveFormOpened();
  }

  public void waitSvnResolveFormClosed() {
    svnResolve.waitResolveFormClosed();
  }

  /**
   * enter the resolve type
   *
   * @param type what type of resolve used
   */
  public void selectSvnResolveType(String type) {
    svnResolve.selectResolveType(type);
  }

  /** click svn resolve button */
  public void clickSvnResolveConfirm() {
    svnResolve.clickResolve();
  }

  /** wait the svn copy form is opened */
  public void waitSvnCopyFormOpened() {
    svnCopy.waitCopyFormOpened();
  }

  /** wait the svn copy form is closed */
  public void waitSvnCopyFormClosed() {
    svnCopy.waitCopyFormClosed();
  }

  /**
   * open item in svn project file tree
   *
   * @param path path to item in project file tree
   */
  public void openSvnCopyItemByPath(String path) throws Exception {
    svnCopy.openItemByPath(path);
  }

  /** click on the copy button in the svn copy */
  public void clickSvnCopyCopyButton() {
    svnCopy.clickCopyButton();
    waitSvnCopyFormClosed();
  }

  /** wait the svn move form is opened */
  public void waitSvnMoveFormOpened() {
    svnMove.waitMoveFormOpened();
  }

  /** wait the svn move form is closed */
  public void waitSvnMoveFormClosed() {
    svnMove.waitMoveFormClose();
  }

  /**
   * open item in svn project file tree
   *
   * @param path path to item in project file tree
   */
  public void openSvnMoveItemByPath(String path) throws Exception {
    svnMove.openItemByPath(path);
  }

  /** click on the move button in the svn move */
  public void clickSvnMoveButtonMove() {
    svnMove.clickMoveButton();
  }

  /** wait the svn merge form is opened */
  public void waitSvnMergeFormOpened() {
    svnMerge.waitMainFormOpened();
  }

  /** wait the svn merge form is closed */
  public void waitSvnMergeFormClosed() {
    svnMerge.waitMainFormClosed();
  }

  /** click on the URL checkbox in the svn merge */
  public void clickSvnMergeUrlCheckbox() {
    svnMerge.clickUrlCheckbox();
  }

  /**
   * type the source URL in the svn merge
   *
   * @param sourceUrl is the specified source URL
   */
  public void typeSvnMergeSourceUrl(String sourceUrl) {
    svnMerge.typeTextUrlBox(sourceUrl);
  }

  /** click on the merge button in the svn merge */
  public void clickSvnMergeButtonMerge() {
    svnMerge.clickMergeButton();
  }

  /**
   * wait item from project tree
   *
   * @param path item path in the project tree
   */
  public void waitSvnMergeItemByPath(String path) throws Exception {
    svnMerge.waitItem(path);
  }

  /**
   * select item from project tree
   *
   * @param path item path in the project tree
   */
  public void selectsvnMergeItemByPath(String path) throws Exception {
    svnMerge.selectItem(path);
  }

  /**
   * Note: Import project from private Subversion repository
   *
   * <p>wait main form, select the 'SUBVERSION' field type url and name of importing project type
   * user name and password
   *
   * @param uri is user uri into URL: field
   * @param nameOfProject is name of project into name: field
   * @param user is the user name into 'User Name' field
   * @param password is the password into 'Password' field
   */
  public void waitAndTypeImporterAsSvnInfo(
      String uri, String nameOfProject, String user, String password) throws InterruptedException {
    importProjectFromLocation.waitMainForm();
    importProjectFromLocation.selectSvnSourceField();
    importProjectFromLocation.typeURi(uri);
    importProjectFromLocation.typePathOfSvnRepoUrl("/");
    importProjectFromLocation.typeProjectName(nameOfProject);
    importProjectFromLocation.clickImportBtnWithoutWait();
    svnLogin(user, password);

    importProjectFromLocation.waitMainFormIsClosed();
    loader.waitOnClosed();
  }

  /** wait the svn export form is opened */
  public void waitSvnExportFormOpened() {
    svnExport.waitMainFormOpened();
  }

  /** wait the svn export form is closed */
  public void waitSvnExportFormClosed() {
    svnExport.waitMainFormClosed();
  }

  /** click on the 'Revision' checkbox in the svn export */
  public void clickSvnExportRevisionCheckbox() {
    svnExport.clickRevisionCheckbox();
  }

  /**
   * type the text in the 'Revision:' box in svn export
   *
   * @param text is the expected text revision
   */
  public void typeSvnExportTextRevision(String text) {
    svnExport.typeTextRevision(text);
  }

  /** click on the export button in the svn export */
  public void clickSvnExportButtonExport() {
    svnExport.clickExportBtn();
  }

  /** wait svn revert form opened */
  public void waitSvnRevertFormOpened() {
    svnRevert.waitRevertFormOpened();
  }

  /** wait svn revert form closed */
  public void waitSvnRevertFormClosed() {
    svnRevert.waitRevertFormClosed();
  }

  /** click on svn revert accept */
  public void clickSvnRevertOk() {
    svnRevert.clickRevertOk();
  }

  /** wait the svn get lock form is opened */
  public void waitSvnGetLockFormOpened() {
    svnGetLock.waitGetLockFormOpened();
  }

  /** click on lock button in svn get lock */
  public void clickSvnGetLock() {
    svnGetLock.clickLock();
  }

  /** wait the svn release lock form is opened */
  public void waitSvnReleaseLockFormOpened() {
    svnReleaseLock.waitReleaseLockFormOpened();
  }

  /** click on force unlock button in svn release lock */
  public void clickSvnReleaseLockForceUnlock() {
    svnReleaseLock.clickForceUnlock();
  }

  /** click on unlock button in svn release lock */
  public void clickSvnReleaseLockUnlock() {
    svnReleaseLock.clickUnlock();
  }

  /** wait the svn property form is opened */
  public void waitSvnPropertiesFormOpened() {
    svnProperties.waitPropertyFormOpened();
  }

  public void waitSvnPropertiesFormClosed() {
    svnProperties.waitPropertyFormClosed();
  }

  /**
   * type svn property
   *
   * @param name svn property name
   */
  public void typeSvnPropertiesPropertyName(String name) {
    svnProperties.typeProperty(name);
  }

  /**
   * set svn property value
   *
   * @param value property value
   */
  public void setSvnPropertiesValue(String value) {
    svnProperties.setPropertyValue(value);
  }

  /** click ok svn properties */
  public void clickSvnApplyProperties() {
    svnProperties.clickOkButton();
  }

  /** Wait the SVN status bar is opened */
  public void waitSvnStatusBarInfoPanelOpened() {

    loader.waitOnClosed();
    svnStatusBar.waitSvnStatusBarInfoPanelOpened();
  }

  /** wait opening svn info panel with expected message */
  public void waitSvnInfoPanelWithMessage(String message) throws InterruptedException {
    loader.waitOnClosed();
    svnStatusBar.waitMessageIntoSvnInfoPanel(message);
  }

  /** returned all message from svn status bar */
  public String getAllMessageFromSvnStatusBar() {
    return svnStatusBar.getAllMessageFromStatusBar();
  }

  /**
   * Enter name and password to 'SVN Authentication' form
   *
   * @param userName svn user name
   * @param password svn user password
   */
  public void svnLogin(String userName, String password) {
    svnAuthentication.svnLogin(userName, password);
  }
}
