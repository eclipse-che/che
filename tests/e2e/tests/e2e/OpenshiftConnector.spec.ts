/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { Key } from 'selenium-webdriver';
import { e2eContainer } from '../../inversify.config';
import { CLASSES, TYPES } from '../../inversify.types';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { Ide } from '../../pageobjects/ide/Ide';
import { Buttons, Locations, OpenDialogWidget } from '../../pageobjects/ide/OpenDialogWidget';
import { OpenshiftAppExplorerToolbar, OpenshiftContextMenuItems, OpenshiftPlugin } from '../../pageobjects/ide/OpenshiftPlugin';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { QuickOpenContainer } from '../../pageobjects/ide/QuickOpenContainer';
import { Terminal } from '../../pageobjects/ide/Terminal';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import { ICheLoginPage } from '../../pageobjects/login/ICheLoginPage';
import { TestConstants } from '../../TestConstants';
import { DriverHelper } from '../../utils/DriverHelper';
import { PreferencesHandler, TerminalRendererType } from '../../utils/PreferencesHandler';
import { TestWorkspaceUtil } from '../../utils/workspace/TestWorkspaceUtil';
import { TimeoutConstants } from '../../TimeoutConstants';


const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const loginPage: ICheLoginPage = e2eContainer.get<ICheLoginPage>(TYPES.CheLogin);
const testWorkspaceUtils: TestWorkspaceUtil = e2eContainer.get<TestWorkspaceUtil>(TYPES.WorkspaceUtil);
const openshiftPlugin: OpenshiftPlugin = e2eContainer.get(CLASSES.OpenshiftPlugin);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const quickOpenContainer: QuickOpenContainer = e2eContainer.get(CLASSES.QuickOpenContainer);
const openDialogWidget: OpenDialogWidget = e2eContainer.get(CLASSES.OpenDialogWidget);
const preferencesHalder: PreferencesHandler = e2eContainer.get(CLASSES.PreferencesHandler);
const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const projectName: string = 'node-js';
const selectSugestionSuffix: string = '(Press \'Enter\' to confirm your input or \'Escape\' to cancel)';

suite('Openshift connector user story', async () => {
  const workspacePrefixUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/dashboard/#/ide/${TestConstants.TS_SELENIUM_USERNAME}/`;
  let wsName: string;

  suiteSetup(async function () {
    preferencesHalder.setTerminalType(TerminalRendererType.dom);
    const wsConfig = await testWorkspaceUtils.getBaseDevfile();
    wsName = wsConfig.metadata!.name!;
    wsConfig.projects = [
      {
        'name': 'node-js',
        'source': {
          'location': 'https://github.com/maxura/nodejs-hello-world.git',
          'type': 'git'
        }
      }
    ],

      wsConfig.components = [
        {
          'id': 'redhat/vscode-openshift-connector/latest',
          'type': 'chePlugin'
        }
      ];

    await testWorkspaceUtils.createWsFromDevFile(wsConfig);
  });

  test('Login into workspace and open plugin', async () => {
    await driverHelper.navigateToUrl(workspacePrefixUrl + wsName);
    await loginPage.login();
    await ide.waitWorkspaceAndIde();
    await projectTree.openProjectTreeContainer(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
    await projectTree.waitProjectImported(projectName, 'index.js');
    await dashboard.waitDisappearanceNavigationMenu();
    await openshiftPlugin.clickOnOpenshiftToollBarIcon();
    await openshiftPlugin.waitOpenshiftConnectorTree();
  });

  test('Login into current cluster', async () => {
    const provideAuthenticationSuffix: string = `for basic authentication to the API server ${selectSugestionSuffix}`;
    const loginIntoClusterMessage: string = 'You are already logged in the cluster. Do you want to login to a different cluster?';
    const openshiftIP: string = await openshiftPlugin.getClusterIP();

    await openshiftPlugin.clickOnOpenshiftConnectorTree();
    await openshiftPlugin.clickOnApplicationToolbarItem(OpenshiftAppExplorerToolbar.LogIntoCluster);
    await ide.clickOnNotificationButton(loginIntoClusterMessage, 'Yes');
    await quickOpenContainer.clickOnContainerItem('Credentials');
    await quickOpenContainer.clickOnContainerItem(`https://${openshiftIP}`);
    await quickOpenContainer.clickOnContainerItem('$(plus) Add new user...');
    await quickOpenContainer.typeAndSelectSuggestion(TestConstants.TS_TEST_OPENSHIFT_PLUGIN_USERNAME, `Provide Username ${provideAuthenticationSuffix}`);
    await quickOpenContainer.typeAndSelectSuggestion(TestConstants.TS_TEST_OPENSHIFT_PLUGIN_PASSWORD, `Provide Password ${provideAuthenticationSuffix}`);
    await openshiftPlugin.waitItemInTree(TestConstants.TS_TEST_OPENSHIFT_PLUGIN_PROJECT, TimeoutConstants.TS_WAIT_OPENSHIFT_CONNECTOR_TREE_TIMEOUT);

    // workaround for openshift-connector v0.1.5 related to known issue https://github.com/redhat-developer/vscode-openshift-tools/issues/1636
    /*await topMenu.selectOption('View', 'Find Command...');
    await quickOpenContainer.typeAndSelectSuggestion('OpenShift: Set Active Project', 'OpenShift: Set Active Project');
    await quickOpenContainer.typeAndSelectSuggestion(TestConstants.TS_TEST_OPENSHIFT_PLUGIN_PROJECT, TestConstants.TS_TEST_OPENSHIFT_PLUGIN_PROJECT);
    await openshiftPlugin.clickOnItemInTree(openshiftIP);
    await openshiftPlugin.waitItemInTree(TestConstants.TS_TEST_OPENSHIFT_PLUGIN_PROJECT);*/
  });

  test('Create new component with application', async () => {
    await topMenu.selectOption('View', 'Find Command...');
    await quickOpenContainer.typeAndSelectSuggestion('OpenShift: New Component', 'OpenShift: New Component from local folder');
    await quickOpenContainer.typeAndSelectSuggestion(TestConstants.TS_TEST_OPENSHIFT_PLUGIN_PROJECT, TestConstants.TS_TEST_OPENSHIFT_PLUGIN_PROJECT);
    await quickOpenContainer.clickOnContainerItem('$(plus) Create new Application...');
    await quickOpenContainer.typeAndSelectSuggestion('node-js-app', `Provide Application name ${selectSugestionSuffix}` );
    await quickOpenContainer.clickOnContainerItem('$(plus) Add new context folder.');
    await openDialogWidget.selectLocationAndAddContextFolder(Locations.Root, `projects/${projectName}`, Buttons.AddContext);
    await quickOpenContainer.typeAndSelectSuggestion('component-node-js', `Provide Component name ${selectSugestionSuffix}`);

    await quickOpenContainer.clickOnContainerItem('nodejs');
    await quickOpenContainer.clickOnContainerItem('14-ubi7');

    await driverHelper.wait(TimeoutConstants.TS_PROJECT_TREE_TIMEOUT);
    await openshiftPlugin.waitItemInTree(TestConstants.TS_TEST_OPENSHIFT_PLUGIN_PROJECT);
    await openshiftPlugin.waitItemInTree('node-js-app');
    await openshiftPlugin.waitItemInTree('component-node-js');
  });

  test('Push new component', async () => {
    driverHelper.getDriver().switchTo().activeElement().sendKeys(Key.F1);
    await quickOpenContainer.typeAndSelectSuggestion(OpenshiftContextMenuItems.Push, 'OpenShift: Push Component');
    await quickOpenContainer.typeAndSelectSuggestion(TestConstants.TS_TEST_OPENSHIFT_PLUGIN_PROJECT, TestConstants.TS_TEST_OPENSHIFT_PLUGIN_PROJECT);
    await quickOpenContainer.clickOnContainerItem('node-js-app');
    await quickOpenContainer.clickOnContainerItem('component-node-js');
    await terminal.selectTabByPrefixAndWaitText('OpenShift: Push', 'Changes successfully pushed to component', 240000);
  });

  suite('Cleanup', async () => {
    test('Remove test workspace', async () => {
        await testWorkspaceUtils.cleanUpAllWorkspaces();
    });
});

});
