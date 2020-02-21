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
import {CreateWorkspaceSvc} from '../../workspaces/create-workspace/create-workspace.service';
import {CheWorkspace} from '../../../components/api/workspace/che-workspace.factory';
import {DevfileRegistry, IDevfileMetaData} from '../../../components/api/devfile-registry.factory';
import {
  ICheButtonDropdownMainAction,
  ICheButtonDropdownOtherAction
} from '../../../components/widget/button-dropdown/che-button-dropdown.directive';
import {CheNotification} from '../../../components/notification/che-notification.factory';


/**
 * @ngdoc controller
 * @name template.list.controller:TemplateListController
 * @description This class is handling the controller for a template list
 * @author Oleksii Orel
 */
export class TemplateListController {

  static $inject = [
    '$q',
    'cheWorkspace',
    'devfileRegistry',
    'createWorkspaceSvc',
    '$filter',
    '$log',
    'cheNotification'];

  ephemeralMode: boolean;

  private $q: ng.IQService;
  private $log: ng.ILogService;
  private $filter: ng.IFilterService;
  private cheNotification: CheNotification;
  private devfileRegistry: DevfileRegistry;
  private createWorkspaceSvc: CreateWorkspaceSvc;
  /**
   * Create button config.
   */
  private createButtonConfig: {
    mainAction: ICheButtonDropdownMainAction,
    otherActions: Array<ICheButtonDropdownOtherAction>
  };

  private isLoading: boolean;
  private isCreating: boolean;
  private devfileRegistryUrl: string;
  private selectedDevfile: IDevfileMetaData | undefined;

  private searchValue: string = '';
  private devfiles: Array<IDevfileMetaData> = [];
  private filteredDevfiles: Array<IDevfileMetaData> = [];

  /**
   * Default constructor that is using resource
   */
  constructor($q: ng.IQService,
              cheWorkspace: CheWorkspace,
              devfileRegistry: DevfileRegistry,
              createWorkspaceSvc: CreateWorkspaceSvc,
              $filter: ng.IFilterService,
              $log: ng.ILogService,
              cheNotification: CheNotification) {
    this.$q = $q;
    this.$log = $log;
    this.$filter = $filter;
    this.cheNotification = cheNotification;
    this.devfileRegistry = devfileRegistry;
    this.createWorkspaceSvc = createWorkspaceSvc;

    this.createButtonConfig = {
      mainAction: {
        title: 'Create & Open',
        type: 'button',
        action: () => {
          this.isCreating = true;
          this.createWorkspace().then((workspace: che.IWorkspace) => {
            this.createWorkspaceSvc.redirectToIDE(workspace);
          }).catch(() => this.isCreating = false);
        }
      },
      otherActions: [{
        title: 'Create & Proceed Editing',
        type: 'button',
        action: () => {
          this.isCreating = true;
          this.createWorkspace().then((workspace: che.IWorkspace) => {
            this.createWorkspaceSvc.redirectToDetails(workspace);
          }).catch(() => this.isCreating = false);
        },
        orderNumber: 1
      }]
    };

    cheWorkspace.fetchWorkspaceSettings().then(() => {
      const workspaceSettings = cheWorkspace.getWorkspaceSettings();
      this.devfileRegistryUrl = workspaceSettings && workspaceSettings.cheWorkspaceDevfileRegistryUrl;
      this.ephemeralMode = workspaceSettings['che.workspace.persist_volumes.default'] === 'false';
      this.init();
    });
  }

  private init(): void {
    if (!this.devfileRegistryUrl) {
      const message = 'Failed to load the devfile registry URL.';
      this.cheNotification.showError(message);
      this.$log.error(message);
      return;
    }
    this.isLoading = true;
    this.devfileRegistry.fetchDevfiles(this.devfileRegistryUrl).then((devfiles: Array<IDevfileMetaData>) => {
      this.devfiles = devfiles.map(devfile => {
        if (!devfile.icon.startsWith('http')) {
          devfile.icon = this.devfileRegistryUrl + devfile.icon;
        }
        return devfile;
      });
      this.applyFilter();
    }, (error: any) => {
      const message = 'Failed to load devfiles meta list.';
      this.cheNotification.showError(message);
      this.$log.error(message, error);
    }).finally(() => {
      this.isLoading = false;
    });
  }

  private createWorkspace(): ng.IPromise<che.IWorkspace> {
    if (!this.selectedDevfile || !this.selectedDevfile.links || !this.selectedDevfile.links.self) {
      return this.$q.reject({data: {message: 'There is no selected Template.'}});
    }
    const selfLink = this.selectedDevfile.links.self;
    return this.devfileRegistry.fetchDevfile(this.devfileRegistryUrl, selfLink).then(() => {
      const devfile = this.devfileRegistry.getDevfile(this.devfileRegistryUrl, selfLink);
      if (this.ephemeralMode) {
        if (!devfile.attributes) {
          devfile.attributes = {};
        }
        devfile.attributes.persistVolumes = 'false';
      }
      const attributes = {stackName: this.selectedDevfile.displayName};
      return this.createWorkspaceSvc.createWorkspaceFromDevfile(undefined, devfile, attributes, true);
    });
  }

  applyFilter(): void {
    if (!this.searchValue) {
      this.clearFilter();
      return;
    }
    const value = this.searchValue.toLocaleLowerCase();
    this.filteredDevfiles = this.$filter('filter')(this.devfiles, devfile => {
      return devfile.displayName.toLowerCase().includes(value) || devfile.description.toLowerCase().includes(value);
    });
    if (this.filteredDevfiles.findIndex(devfile => devfile === this.selectedDevfile) === -1) {
      this.selectedDevfile = undefined;
    }
  }

  clearFilter(): void {
    if (this.searchValue) {
      this.searchValue = '';
    }
    this.filteredDevfiles = this.devfiles;
  }

  onSelect(devfile: IDevfileMetaData): void {
    this.selectedDevfile = devfile;
  }

  isCreateButtonDisabled(): boolean {
    return this.isCreating || !this.selectedDevfile || !this.selectedDevfile.links || !this.selectedDevfile.links.self;
  }
}
