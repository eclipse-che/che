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
import {CheWorkspace} from '../../../../components/api/workspace/che-workspace.factory';
import {DevfileRegistry, IDevfileMetaData} from '../../../../components/api/devfile-registry.factory';

/**
 * @description This class is handling the controller of devfile selector.
 * @author Ann Shumilova
 */
export class DevfileSelectorController {

  static $inject = ['devfileRegistry', 'cheWorkspace'];

  private devfileRegistry: DevfileRegistry;
  private cheWorkspace: CheWorkspace;
  private devfiles: Array<IDevfileMetaData>;
  onDevfileSelect: Function;
  selectedDevfile: any;

  /**
   * Default constructor that is using resource injection
   */
  constructor(devfileRegistry: DevfileRegistry, cheWorkspace: CheWorkspace) {
    this.devfileRegistry = devfileRegistry;
    this.cheWorkspace = cheWorkspace;
    this.devfiles = [];
    this.loadDevfiles();
  }

  loadDevfiles(): void {
    const location = this.cheWorkspace.getWorkspaceSettings().cheWorkspaceDevfileRegistryUrl;
    const urls = location.split(" ");

    let promises = [];
    
    for (const url of urls) {
      promises.push(this.devfileRegistry.fetchDevfiles(url).then((data: Array<IDevfileMetaData>) => {
        if (data && data.length > 0) {
          data.forEach((devfile)=> {
            // Set the origin url as the location
            devfile.location = url;
            this.devfiles.push(devfile);
          });
        }
      }));
    }
    Promise.all(promises).then(() => {
      if (this.devfiles && this.devfiles.length > 0) {
        this.devfileOnClick(this.devfiles[0]);
      }
    });
  }

  devfileOnClick(devfile: any): void {
    this.selectedDevfile = devfile;

    let devfileContent = this.devfileRegistry.getDevfile(devfile.location, devfile.links.self);
    if (devfileContent) {
      this.onDevfileSelect({devfile: devfileContent});
    } else {
      this.devfileRegistry.fetchDevfile(devfile.location, devfile.links.self).then((devfileContent: che.IWorkspaceDevfile) => {
        this.onDevfileSelect({devfile: devfileContent});
      });
    }
  }
}
