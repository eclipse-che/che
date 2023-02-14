/*********************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { e2eContainer } from '../../../inversify.config';
import { Workbench, ViewItem, TreeItem } from 'monaco-page-objects';
import { CLASSES } from '../../../inversify.types';
import { WorkspaceHandlingTests } from '../../../testsLibrary/WorkspaceHandlingTests';
import { Logger } from '../../../utils/Logger';
import CheReporter from '../../../driver/CheReporter';
import { ProjectAndFileTestsCheCode } from '../../../testsLibrary/che-code/ProjectAndFileTestsCheCode';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTestsCheCode = e2eContainer.get(CLASSES.ProjectAndFileTestsCheCode);

const stackName: string = 'Quarkus REST API';
const sectionTitle: string = 'quarkus-quickstarts';
const projectName: string = 'getting-started';

suite(`Happy Path test`, async () => {
    suite(`Create ${stackName} workspace`, async () => {
        workspaceHandlingTests.createAndOpenWorkspace(stackName);
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
        test('Register running workspace', async () => {
            CheReporter.registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
        test('Wait workspace readiness', async() => {
            const workbench: Workbench = await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();

            await projectAndFileTests.waitAndConfirmTrustAuthorsDialogBox();

            const rootProjectViewItem: ViewItem = await projectAndFileTests.waitForRootProjectPresence(workbench, sectionTitle, projectName);
            Logger.debug('Found root project.');
            if (rootProjectViewItem instanceof TreeItem) {
                Logger.debug(`Root project label: ${await rootProjectViewItem.getLabel()}`);
            }
        });
    });

    suite('Stopping and deleting the workspace', async () => {
        test(`Stop and remove workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
    });
});
