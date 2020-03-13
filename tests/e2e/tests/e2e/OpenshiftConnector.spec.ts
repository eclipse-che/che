/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { test } from 'mocha';
import { e2eContainer } from '../../inversify.config';
import { CLASSES, TYPES } from '../../inversify.types';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { Ide } from '../../pageobjects/ide/Ide';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { ICheLoginPage } from '../../pageobjects/login/ICheLoginPage';
import { TestConstants } from '../../TestConstants';
import { DriverHelper } from '../../utils/DriverHelper';
import { TestWorkspaceUtil } from '../../utils/workspace/TestWorkspaceUtil';
import { OpenshiftPlugin, OpenshiftAppExplorerToolbar, OpenshiftContextMenuItems } from '../../pageobjects/ide/OpenshiftPlugin';
import { QuickOpenContainer } from '../../pageobjects/ide/QuickOpenContainer';
import { OpenDialogWidget, Locations, Buttons } from '../../pageobjects/ide/OpenDialogWidget';
import { Terminal } from '../../pageobjects/ide/Terminal';
import { PreferencesHandler, TerminalRendererType } from '../../utils/PreferencesHandler';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const loginPage: ICheLoginPage = e2eContainer.get<ICheLoginPage>(TYPES.CheLogin);
const testWorkspaceUtils: TestWorkspaceUtil = e2eContainer.get<TestWorkspaceUtil>(TYPES.WorkspaceUtil);
const openshiftPlugin: OpenshiftPlugin = e2eContainer.get(CLASSES.OpenshiftPlugin);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const namespace: string = TestConstants.TS_SELENIUM_USERNAME;
const quickOpenContainer: QuickOpenContainer = e2eContainer.get(CLASSES.QuickOpenContainer);
const openDialogWidget: OpenDialogWidget = e2eContainer.get(CLASSES.OpenDialogWidget);
const preferencesHalder: PreferencesHandler = e2eContainer.get(CLASSES.PreferencesHandler);
const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
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

  test('Login into waorkspace and open plugin', async () => {
    await driverHelper.navigateToUrl(workspacePrefixUrl + wsName);
    await loginPage.login();
    await ide.waitWorkspaceAndIde(namespace, wsName);
    await projectTree.openProjectTreeContainer();
    await projectTree.waitProjectImported(projectName, 'index.js');
    await dashboard.waitDisappearanceNavigationMenu();
    await openshiftPlugin.clickOnOpenshiftToollBarIcon();
    await openshiftPlugin.waitOpenshiftConnectorTree();

  });


  test('Login into current cluster', async () => {
    const provideAuthenticationSuffix: string = `for basic authentication to the API server ${selectSugestionSuffix}`;
    const loginIntoClusterMessage: string = 'You are already logged in the cluster. Do you want to login to a different cluster?';
    const openshiftIP: string = await openshiftPlugin.getClusterIP();
    await openshiftPlugin.clickOnApplicationToolbarItem(OpenshiftAppExplorerToolbar.LogIntoCluster);
    await ide.clickOnNotificationButton(loginIntoClusterMessage, 'Yes');
    await quickOpenContainer.clickOnContainerItem('Credentials');
    await quickOpenContainer.clickOnContainerItem(`https://${openshiftIP}`);
    await quickOpenContainer.clickOnContainerItem('$(plus) Add new user...');
    await quickOpenContainer.typeAndSelectSuggestion('developer', `Provide Username ${provideAuthenticationSuffix}`);
    await quickOpenContainer.typeAndSelectSuggestion('123', `Provide Password ${provideAuthenticationSuffix}`);
  });

  test('Create new component with application', async () => {
    await openshiftPlugin.invokeContextMenuCommandOnItem('myproject', OpenshiftContextMenuItems.NewComponent);
    await quickOpenContainer.clickOnContainerItem('$(plus) Create new Application...');
    await quickOpenContainer.typeAndSelectSuggestion('node-js-app', `Provide Application name ${selectSugestionSuffix}` );
    await quickOpenContainer.clickOnContainerItem('Workspace Directory');
    await quickOpenContainer.clickOnContainerItem('$(plus) Add new context folder.');
    await openDialogWidget.selectLocationAndAddContextFolder(Locations.Root, `projects/${projectName}`, Buttons.AddContext);
    await quickOpenContainer.typeAndSelectSuggestion('component-node-js', `Provide Component name ${selectSugestionSuffix}`);
    await quickOpenContainer.clickOnContainerItem('nodejs');
    await quickOpenContainer.clickOnContainerItem('latest');
    await openshiftPlugin.clickOnItemInTree('myproject');
    await openshiftPlugin.clickOnItemInTree('node-js-app');
    await openshiftPlugin.clickOnItemInTree('component-node-js');
  });

  test('Push new component', async () => {
    await openshiftPlugin.invokeContextMenuCommandOnItem('component-node-js', OpenshiftContextMenuItems.Push);
    await terminal.waitText('OpenShift', 'Changes successfully pushed to component', TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT);
  });

});




