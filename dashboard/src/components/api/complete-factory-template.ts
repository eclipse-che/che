/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
/**
 * This is complete factory templates.
 *
 * @author Oleksii Orel
 */
export class CheFactoryTemplates {

  static get MINIMAL(): string {
    return JSON.stringify({
      'v': '4.0',
      'workspace': {
        'projects': [
          {
            'name': 'Spring',
            'attributes': {
              'languageVersion': [
                '1.6'
              ],
              'language': [
                'java'
              ]
            },
            'type': 'maven',
            'source': {
              'location': 'https://github.com/codenvy-templates/web-spring-java-simple.git',
              'type': 'git',
              'parameters': {
                'keepVcs': 'false',
                'branch': '3.1.0'
              }
            },
            'modules': [],
            'path': '/Spring',
            'mixins': [
              'git'
            ],
            'problems': []
          }
        ],
        'defaultEnv': 'wss',
        'name': 'wss',
        'environments': {
          'wss': {
            'machines': {
              'dev-machine': {
                'installers': [
                  'org.eclipse.che.terminal', 'org.eclipse.che.ws-agent', 'org.eclipse.che.ssh'
                ],
                'servers': {},
                'attributes': {
                  'memoryLimitBytes': '2147483648'
                }
              }
            },
            'recipe': {
              'content': 'codenvy/ubuntu_jdk8',
              'type': 'dockerimage'
            }
          }
        }
      }
    });
  }

  static get COMPLETE(): string {
    return JSON.stringify({
      'v': '4.0',
      'workspace': {
        'commands': [],
        'projects': [
          {
            'name': 'Spring',
            'attributes': {
              'languageVersion': [
                '1.6'
              ],
              'language': [
                'java'
              ]
            },
            'type': 'maven',
            'source': {
              'location': 'https://github.com/codenvy-templates/web-spring-java-simple.git',
              'type': 'git',
              'parameters': {
                'keepVcs': 'false',
                'branch': '3.1.0'
              }
            },
            'modules': [],
            'path': '/Spring',
            'mixins': [
              'git'
            ],
            'problems': []
          }
        ],
        'defaultEnv': 'wss',
        'name': 'wss',
        'attributes': {},
        'environments': {
          'wss': {
            'machines': {
              'dev-machine': {
                'installers': [
                  'org.eclipse.che.terminal',
                  'org.eclipse.che.ws-agent',
                  'org.eclipse.che.ssh'
                ],
                'servers': {},
                'attributes': {
                  'memoryLimitBytes': '2147483648'
                }
              }
            },
            'recipe': {
              'content': 'codenvy/ubuntu_jdk8',
              'type': 'dockerimage'
            }
          }
        }
      },
      'name': 'My Complete Template',
      'creator': {
        'name': 'Codenvy Factory',
        'email': 'factories@codenvy.com'
      }
    });
  }

}
