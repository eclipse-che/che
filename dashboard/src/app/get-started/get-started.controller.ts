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

import { CreateWorkspaceSvc } from '../workspaces/create-workspace/create-workspace.service';
import { CheWorkspace } from '../../components/api/workspace/che-workspace.factory';
import { DevfileRegistry, IDevfileMetaData } from '../../components/api/devfile-registry.factory';
import { CheNotification } from '../../components/notification/che-notification.factory';
import { IChePfSecondaryButtonProperties } from '../../components/che-pf-widget/button/che-pf-secondary-button.directive';
import { IGetStartedToolbarBindingProperties } from './toolbar/get-started-toolbar.component';


/**
 * @ngdoc controller
 * @name get.started.controller:GetStartedController
 * @description This class is handling the controller for the Get Started page with template list
 * @author Oleksii Orel
 * @author Oleksii Kurinnyi
 */
export class GetStartedController {

  static $inject = [
    '$filter',
    '$log',
    '$q',
    'cheNotification',
    'cheWorkspace',
    'createWorkspaceSvc',
    'devfileRegistry',
    '$location'
  ];

  toolbarProps: IGetStartedToolbarBindingProperties;
  createButton: IChePfSecondaryButtonProperties;
  filteredDevfiles: Array<IDevfileMetaData> = [];

  $filter: ng.IFilterService;
  $log: ng.ILogService;
  $q: ng.IQService;
  cheNotification: CheNotification;
  createWorkspaceSvc: CreateWorkspaceSvc;
  devfileRegistry: DevfileRegistry;

  private isLoading: boolean = false;
  private isCreating: boolean = false;
  private devfileRegistryUrl: string;

  private devfiles: Array<IDevfileMetaData> = [];
  private ephemeralMode: boolean;

  /**
   * Default constructor that is using resource
   */
  constructor(
    $filter: ng.IFilterService,
    $log: ng.ILogService,
    $q: ng.IQService,
    cheNotification: CheNotification,
    cheWorkspace: CheWorkspace,
    createWorkspaceSvc: CreateWorkspaceSvc,
    devfileRegistry: DevfileRegistry,
    $location: ng.ILocationService
  ) {
    this.$filter = $filter;
    this.$log = $log;
    this.$q = $q;
    this.cheNotification = cheNotification;
    this.createWorkspaceSvc = createWorkspaceSvc;
    this.devfileRegistry = devfileRegistry;

    this.toolbarProps = {
      devfiles: [],
      ephemeralMode: false,
      onFilterChange: filtered => this.onFilterChange(filtered),
      onEphemeralModeChange: mode => this.onEphemeralModeChange(mode),
    };
    this.createButton = {
      title: 'Create a Custom Workspace',
      onClick: () => $location.path('/create-workspace').search({tab: 'IMPORT_DEVFILE'}),
    };

    this.isLoading = true;
    cheWorkspace.fetchWorkspaceSettings().then(() => {
      const workspaceSettings = cheWorkspace.getWorkspaceSettings();
      this.devfileRegistryUrl = workspaceSettings && workspaceSettings.cheWorkspaceDevfileRegistryUrl;
      this.ephemeralMode = workspaceSettings['che.workspace.persist_volumes.default'] === 'false';
      this.toolbarProps.ephemeralMode = this.ephemeralMode;
      return this.init();
    }).finally(() => {
      this.isLoading = false;
    });
  }

  isCreateButtonDisabled(): boolean {
    return this.isCreating;
  }

  onFilterChange(filteredDevfiles: IDevfileMetaData[]): void {
    this.filteredDevfiles = filteredDevfiles;
  }

  onEphemeralModeChange(mode: boolean): void {
    this.ephemeralMode = mode;
  }

  createWorkspace(devfileMetaData: IDevfileMetaData): void {
    if (this.isCreating) {
      return;
    }
    if (!devfileMetaData || !devfileMetaData.links || !devfileMetaData.links.self) {
      const message = 'There is no selected Template.';
      this.cheNotification.showError(message);
      this.$log.error(message);
      return;
    }
    this.isCreating = true;
    const selfLink = devfileMetaData.links.self;
    this.devfileRegistry.fetchDevfile(this.devfileRegistryUrl, selfLink)
      .then(() => {
        const devfile = this.devfileRegistry.getDevfile(this.devfileRegistryUrl, selfLink);
        if (this.ephemeralMode) {
          if (!devfile.attributes) {
            devfile.attributes = {};
          }
          devfile.attributes.persistVolumes = 'false';
        }
        const attributes = {stackName: devfileMetaData.displayName};
        return this.createWorkspaceSvc.createWorkspaceFromDevfile(undefined, devfile, attributes, true);
      })
      .then(workspace => {
        return this.createWorkspaceSvc.redirectToIDE(workspace);
      })
      .finally(() => {
        this.isCreating = false;
      });
  }

  private init(): ng.IPromise<void> {
    if (!this.devfileRegistryUrl) {
      const message = 'Failed to load the devfile registry URL.';
      this.cheNotification.showError(message);
      this.$log.error(message);
      return;
    }
    return this.devfileRegistry.fetchDevfiles(this.devfileRegistryUrl).then((devfiles: Array<IDevfileMetaData>) => {
      this.devfiles = devfiles.map(devfile => {
        if (!devfile.icon.startsWith('http')) {
          devfile.icon = this.devfileRegistryUrl + devfile.icon;
        }
        return devfile;
      });
      this.toolbarProps.devfiles = this.devfiles;
    }, (error: any) => {
      const message = 'Failed to load devfiles meta list.';
      this.cheNotification.showError(message);
      this.$log.error(message, error);
    });
  }

}
