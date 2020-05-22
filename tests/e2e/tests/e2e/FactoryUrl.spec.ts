// /*********************************************************************
//  * Copyright (c) 2019 Red Hat, Inc.
//  *
//  * This program and the accompanying materials are made
//  * available under the terms of the Eclipse Public License 2.0
//  * which is available at https://www.eclipse.org/legal/epl-2.0/
//  *
//  * SPDX-License-Identifier: EPL-2.0
//  **********************************************************************/

import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';
import { DriverHelper } from '../../utils/DriverHelper';
import { Ide } from '../../pageobjects/ide/Ide';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { error } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { WorkspaceNameHandler } from '../..';
import * as workspaceHandling from '../../testsLibrary/WorksapceHandlingTests';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide : Ide = e2eContainer.get(CLASSES.Ide);
const projectTree : ProjectTree = e2eContainer.get(CLASSES.ProjectTree);

// the suite expect user to be logged in
suite('Workspace creation via factory url', async () => {

    let factoryUrl : string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=https://gist.githubusercontent.com/Katka92/fe747fa93b8275abb2fb9f3803de1f0f/raw/33c811ac1fe03715bb51fdd6f84acc6211ba405e/load-test-devfile.yaml`;

    test('Open factory URL', async () => {
        driverHelper.navigateToUrl(factoryUrl);
    });

    test('Wait workspace readyness', async () => {
        try {
            await ide.waitAndSwitchToIdeFrame();
        } catch (err) {
            if (err instanceof error.StaleElementReferenceError) {
                Logger.warn('StaleElementException occured during waiting for IDE. Sleeping for 2 secs and retrying.');
                driverHelper.wait(2000);
                await ide.waitAndSwitchToIdeFrame();
            }
        }
        await ide.waitPreloaderVisible();
        await ide.waitPreloaderAbsent();
        await ide.waitIde();
        await projectTree.openProjectTreeContainer();
    });

    suite ('Stopping and deleting the workspace', async () => {
        let workspaceName = 'not defined';
        suiteSetup( async () => {
            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
        });
        test (`Stop worksapce`, async () => {
            await workspaceHandling.stopWorkspace(workspaceName);
        });
        test (`Remove workspace`, async () => {
            await workspaceHandling.removeWorkspace(workspaceName);
        });
    });


});
