import { che } from '@eclipse-che/api';

export const COMMON_PLUGIN_TESTS_WORKSPACE_NAME: string = 'common-workspace-plugins';

export const COMMON_PLUGIN_TESTS_DEVFILE: che.workspace.devfile.Devfile = {
    'apiVersion': '1.0.0',
    'metadata': {
      'name': 'common-workspace-plugins'
    },
    'projects': [
      {
        'name': 'nodejs-web-app',
        'source': {
          'location': 'https://github.com/Ohrimenko1988/web-nodejs-sample.git',
          'type': 'git'
        }
      }
    ],
    'components': [
      {
        'id': 'redhat/vscode-xml/latest',
        'type': 'chePlugin'
      },
      {
        'type': 'chePlugin',
        'reference': 'https://che-plugin-registry-main.surge.sh/v3/plugins/errata-ai/vale-server/latest/meta.yaml',
        'alias': 'vale-server',
        'preferences': {
          'vale.core.useCLI': true
        }
      },
      {
        'id': 'timonwong/shellcheck/latest',
        'type': 'chePlugin'
      },
      {
        'id': 'redhat/vscode-yaml/latest',
        'type': 'chePlugin'
      },
      {
        'type': 'chePlugin',
        'id': 'ms-kubernetes-tools/vscode-kubernetes-tools/latest'
      }
    ]
  };
