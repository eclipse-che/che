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
import {CheWorkspace} from '../../../../../components/api/workspace/che-workspace.factory';
import {DevfileRegistry, IDevfileMetaData} from '../../../../../components/api/devfile-registry.factory';
import {ImgSrcOnloadResult} from '../../../../../components/attribute/img-src/img-src.directive';

/**
 * @description This class is handling the controller of devfile selector.
 * @author Ann Shumilova
 */
export class DevfileSelectorController {

  static $inject = ['devfileRegistry', 'cheWorkspace'];

  devfiles: Array<IDevfileMetaData>;
  devfileOrderBy: string;
  onDevfileSelect: Function;
  selectedDevfile: any;
  stackName: string;
  iconsLoaded: {
    iconUrl?: boolean;
  } = {};

  private devfileRegistry: DevfileRegistry;
  private cheWorkspace: CheWorkspace;

  /**
   * Default constructor that is using resource injection
   */
  constructor(devfileRegistry: DevfileRegistry, cheWorkspace: CheWorkspace) {
    this.devfileRegistry = devfileRegistry;
    this.cheWorkspace = cheWorkspace;
    this.devfileOrderBy = 'displayName';
  }

  $onInit(): void {
    this.loadDevfiles();
  }

  loadDevfiles(): void {
    let location = this.cheWorkspace.getWorkspaceSettings().cheWorkspaceDevfileRegistryUrl;
    this.devfileRegistry.fetchDevfiles(location).then((data: Array<IDevfileMetaData>) => {
      this.devfiles = data;

      if (this.devfiles && this.devfiles.length > 0) {
        this.devfileOnClick(this.devfiles[0]);
      }
    });
  }

  devfileOnClick(devfile: any): void {
    this.selectedDevfile = devfile;
    this.stackName = devfile.displayName;

    let location = this.cheWorkspace.getWorkspaceSettings().cheWorkspaceDevfileRegistryUrl;

    let devfileContent = this.devfileRegistry.getDevfile(location, devfile.links.self);
    if (devfileContent) {
      this.onDevfileSelect({devfile: devfileContent});
    } else {
      this.devfileRegistry.fetchDevfile(location, devfile.links.self).then((devfileContent: che.IWorkspaceDevfile) => {
        this.onDevfileSelect({devfile: devfileContent});
      });
    }
  }

  iconOnLoad(iconUrl: string, result: ImgSrcOnloadResult): void {
    this.iconsLoaded[iconUrl] = result.loaded;
  }

  showIcon(iconUrl: string): boolean {
    return !!this.iconsLoaded[iconUrl];
  }

}
