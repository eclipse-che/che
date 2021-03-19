import { che } from "@eclipse-che/api";

export const COMMON_PLUGIN_TESTS_WORKSPACE_NAME: string = 'common-workspace-plugins';

export const COMMON_PLUGIN_TESTS_DEVFILE: che.workspace.devfile.Devfile = {
    "apiVersion": "1.0.0",
    "metadata": {
        "name": "common-workspace-plugins"
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
