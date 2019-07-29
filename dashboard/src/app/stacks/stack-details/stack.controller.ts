/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {IDevfileMetaData} from '../../../components/api/devfile-registry.factory';

export  interface IStackInitData {
  devfileMetaData: IDevfileMetaData;
  devfileContent: che.IWorkspaceDevfile;
}

/**
 * Controller for stack management.
 *
 * @author Ann Shumilova
 * @author Oleksii Orel
 */
export class StackController {

  static $inject = ['initData'];

  private devfileMetaData: IDevfileMetaData;
  private devfileYaml: string;
  private devfileName: string;

  /**
   * Default constructor that is using resource injection
   */
  constructor(initData: IStackInitData) {
    this.devfileMetaData = initData.devfileMetaData;

    this.devfileYaml = jsyaml.dump(initData.devfileContent, {'indent': 1});
    this.devfileName = initData.devfileContent.metadata.name;
  }
}
