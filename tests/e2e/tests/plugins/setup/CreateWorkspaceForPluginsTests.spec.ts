/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { DriverHelper } from '../../../utils/DriverHelper';
import { e2eContainer } from '../../../inversify.config';
import { CLASSES, TYPES } from '../../../inversify.types';
import { Ide } from '../../../pageobjects/ide/Ide';
import { PreferencesHandler } from '../../../utils/PreferencesHandler';
import { KubernetesPlugin } from '../../../pageobjects/ide/plugins/KubernetesPlugin';
import { ProjectTree } from '../../../pageobjects/ide/ProjectTree';
import { TestConstants } from '../../../TestConstants';
import { TimeoutConstants } from '../../../TimeoutConstants';
import { ITestWorkspaceUtil } from '../../../utils/workspace/ITestWorkspaceUtil';
import { che } from '@eclipse-che/api';


const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const preferencesHandler: PreferencesHandler = e2eContainer.get(CLASSES.PreferencesHandler);
const kubernetesPlugin: KubernetesPlugin = e2eContainer.get(CLASSES.KubernetesPlugin);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);

const devfileUrl: string = 'https://gist.githubusercontent.com/Ohrimenko1988/94d1a70ff94d4d4bc5f2e4678dc8d538/raw/353a2513ea9e2f61b6cb1e0a88be21efd35b353b/kubernetes-plugin-test.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const sampleName: string = 'nodejs-web-app';
const subRootFolder: string = 'app';
const vsKubernetesConfig = { 'vs-kubernetes.kubeconfig': '/projects/nodejs-web-app/config' };
const workspaceDevfile: che.workspace.devfile.Devfile = {
    "apiVersion": "1.0.0",
    "metadata": {
      "name": "nodejs-24lopytest"
    },
    "projects": [
      {
        "name": "nodejs-web-app",
        "source": {
          "location": "https://github.com/Ohrimenko1988/web-nodejs-sample.git",
          "type": "git"
        }
      }
    ],
    "components": [
      {
        "id": "che-incubator/typescript/latest",
        "memoryLimit": "512Mi",
        "type": "chePlugin"
      },
      {
        "id": "ms-vscode/node-debug2/latest",
        "preferences": {
          "debug.node.useV3": false
        },
        "type": "chePlugin"
      },
      {
        "type": "chePlugin",
        "id": "ms-kubernetes-tools/vscode-kubernetes-tools/latest"
      },
      {
        "mountSources": true,
        "endpoints": [
          {
            "name": "nodejs",
            "port": 3000
          }
        ],
        "memoryLimit": "512Mi",
        "type": "dockerimage",
        "image": "quay.io/eclipse/che-nodejs10-ubi:7.26.1",
        "alias": "nodejs"
      }
    ],
    "commands": [
      {
        "name": "download dependencies",
        "actions": [
          {
            "workdir": "${CHE_PROJECTS_ROOT}/nodejs-web-app/app",
            "type": "exec",
            "command": "npm install",
            "component": "nodejs"
          }
        ]
      },
      {
        "name": "run the web app",
        "actions": [
          {
            "workdir": "${CHE_PROJECTS_ROOT}/nodejs-web-app/app",
            "type": "exec",
            "command": "nodemon app.js",
            "component": "nodejs"
          }
        ]
      },
      {
        "name": "run the web app (debugging enabled)",
        "actions": [
          {
            "workdir": "${CHE_PROJECTS_ROOT}/nodejs-web-app/app",
            "type": "exec",
            "command": "nodemon --inspect app.js",
            "component": "nodejs"
          }
        ]
      },
      {
        "name": "stop the web app",
        "actions": [
          {
            "type": "exec",
            "command": "node_server_pids=$(pgrep -fx '.*nodemon (--inspect )?app.js' | tr \"\\\\n\" \" \") && echo \"Stopping node server with PIDs: ${node_server_pids}\" &&  kill -15 ${node_server_pids} &>/dev/null && echo 'Done.'",
            "component": "nodejs"
          }
        ]
      },
      {
        "name": "Attach remote debugger",
        "actions": [
          {
            "referenceContent": "{\n  \"version\": \"0.2.0\",\n  \"configurations\": [\n    {\n      \"type\": \"node\",\n      \"request\": \"attach\",\n      \"name\": \"Attach to Remote\",\n      \"address\": \"localhost\",\n      \"port\": 9229,\n      \"localRoot\": \"${workspaceFolder}\",\n      \"remoteRoot\": \"${workspaceFolder}\"\n    }\n  ]\n}\n",
            "type": "vscode-launch"
          }
        ]
      }
    ]
  }

suite(`The 'CreateWorkspaceForPluginsTests' test`, async () => {
    suite('Create workspace', async () => {
        test('Set kubeconfig path', async () => {
            await preferencesHandler.setVscodeKubernetesPluginConfig(vsKubernetesConfig);
        });

        test('Wait until created workspace is started', async () => {
            await ide.waitAndSwitchToIdeFrame();
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
            await projectTree.openProjectTreeContainer();
            await projectTree.waitProjectImported(sampleName, subRootFolder);
        });
    });

});
