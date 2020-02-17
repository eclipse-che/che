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
import {CheWorkspace, WorkspaceStatus} from '../../../../components/api/workspace/che-workspace.factory';
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {ConfirmDialogService} from '../../../../components/service/confirm-dialog/confirm-dialog.service';
import {NamespaceSelectorSvc} from '../../create-workspace/ready-to-go-stacks/namespace-selector/namespace-selector.service';
import {WorkspaceDetailsService} from '../workspace-details.service';
import { CheKubernetesNamespace } from '../../../../components/api/che-kubernetes-namespace.factory';
import { CheDashboardConfigurationService } from '../../../../components/branding/che-dashboard-configuration.service';
import { TogglableFeature } from '../../../../components/branding/branding.constant';

const STARTING = WorkspaceStatus[WorkspaceStatus.STARTING];
const RUNNING = WorkspaceStatus[WorkspaceStatus.RUNNING];
const STOPPED = WorkspaceStatus[WorkspaceStatus.STOPPED];

/**
 * @ngdoc controller
 * @name workspaces.details.overview.controller:WorkspaceDetailsOverviewController
 * @description This class is handling the controller for details of workspace : section overview
 * @author Oleksii Orel
 */
export class WorkspaceDetailsOverviewController {

  static $inject = [
    '$location',
    '$q',
    '$route',
    '$scope',
    '$timeout',
    'cheDashboardConfigurationService',
    'cheKubernetesNamespace',
    'cheNotification',
    'cheWorkspace',
    'confirmDialogService',
    'namespaceSelectorSvc',
    'workspaceDetailsService',
  ];

  onChange: Function;

  /**
   * Displaying name of infrastructure namespace.
   */
  infrastructureNamespace: string;
  enabledKubernetesNamespaceSelector: boolean = false;

  private $location: ng.ILocationService;
  private $q: ng.IQService;
  private $route: ng.route.IRouteService;
  private $scope: ng.IScope;
  private $timeout: ng.ITimeoutService;
  private cheDashboardConfigurationService: CheDashboardConfigurationService;
  private cheKubernetesNamespace: CheKubernetesNamespace;
  private cheNotification: CheNotification;
  private cheWorkspace: CheWorkspace;
  private confirmDialogService: ConfirmDialogService;
  private namespaceSelectorSvc: NamespaceSelectorSvc;
  private workspaceDetailsService: WorkspaceDetailsService;

  private workspaceDetails: che.IWorkspace;
  private overviewForm: ng.IFormController;
  private namespaceId: string;
  private workspaceName: string;
  private name: string;
  private usedNamesList: Array<string>;
  private inputmodel: ng.INgModelController;
  private isLoading: boolean;
  private isEphemeralMode: boolean;
  private attributes: che.IWorkspaceConfigAttributes;
  private attributesCopy: che.IWorkspaceConfigAttributes;

  /**
   * Default constructor that is using resource
   */
  constructor(
    $location: ng.ILocationService,
    $q: ng.IQService,
    $route: ng.route.IRouteService,
    $scope: ng.IScope,
    $timeout: ng.ITimeoutService,
    cheDashboardConfigurationService: CheDashboardConfigurationService,
    cheKubernetesNamespace: CheKubernetesNamespace,
    cheNotification: CheNotification,
    cheWorkspace: CheWorkspace,
    confirmDialogService: ConfirmDialogService,
    namespaceSelectorSvc: NamespaceSelectorSvc,
    workspaceDetailsService: WorkspaceDetailsService,
  ) {
    this.$location = $location;
    this.$q = $q;
    this.$route = $route;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.cheDashboardConfigurationService = cheDashboardConfigurationService;
    this.cheKubernetesNamespace = cheKubernetesNamespace;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;
    this.confirmDialogService = confirmDialogService;
    this.namespaceSelectorSvc = namespaceSelectorSvc;
    this.workspaceDetailsService = workspaceDetailsService;
  }

  $onInit(): void {
    const routeParams = this.$route.current.params;
    this.namespaceId = routeParams.namespace;
    this.workspaceName = routeParams.workspaceName;

    const deRegistrationFn = this.$scope.$watch(() => {
      return this.workspaceDetails;
    }, (workspace: che.IWorkspace) => {
      if (!workspace) {
        return;
      }
      this.init();
    }, true);

    this.$scope.$on('$destroy', () => {
      deRegistrationFn();
    });

    this.enabledKubernetesNamespaceSelector = this.cheDashboardConfigurationService.enabledFeature(TogglableFeature.KUBERNETES_NAMESPACE_SELECTOR);

    this.init();
  }

  init(): void {
    this.attributes = this.cheWorkspace.getWorkspaceDataManager().getAttributes(this.workspaceDetails);
    this.name = this.cheWorkspace.getWorkspaceDataManager().getName(this.workspaceDetails);
    this.isEphemeralMode = this.attributes && this.attributes.persistVolumes ? !JSON.parse(this.attributes.persistVolumes) : false;
    this.attributesCopy = angular.copy(this.cheWorkspace.getWorkspaceDataManager().getAttributes(this.workspaceDetails));

    this.updateInfrastructureNamespace();
  }

  /**
   * Returns namespace by its ID
   *
   * @param {string} namespaceId
   * @return {INamespace|{label: string, location: string}}
   */
  getNamespace(namespaceId: string): che.INamespace | { label: string, location: string } {
    const namespaces = this.getNamespaces();
    if (!namespaces || namespaces.length === 0) {
      return {label: '', location: ''};
    }
    return this.getNamespaces().find((namespace: any) => {
      return namespace.id === namespaceId;
    });
  }

  /**
   * Returns namespace's label
   *
   * @param {string} namespaceId
   * @return {string}
   */
  getNamespaceLabel(namespaceId: string): string {
    let namespace = this.getNamespace(namespaceId);
    if (namespace) {
      return namespace.label;
    } else {
      return namespaceId;
    }
  }

  /**
   * Fills in list of workspace's name in current namespace,
   * and triggers validation of entered workspace's name
   */
  fillInListOfUsedNames(): void {
    this.isLoading = true;
    const defer = this.$q.defer();
    let namespace = this.namespaceSelectorSvc.getNamespaceById(this.namespaceId);
    if (namespace && namespace.label) {
      this.namespaceSelectorSvc.onNamespaceChanged(namespace.label);
      defer.resolve();
    } else {
      this.namespaceSelectorSvc.fetchNamespaces().then(() => {
        namespace = this.namespaceSelectorSvc.getNamespaceById(this.namespaceId);
        if (namespace && namespace.label) {
          this.namespaceSelectorSvc.onNamespaceChanged(namespace.label);
          defer.resolve();
        }
        // set default
        this.namespaceId = this.namespaceSelectorSvc.getNamespaceId();
        defer.reject();
      }, (error: any) => {
        defer.reject(error);
      });
    }
    defer.promise.then(() => {
      return this.getOrFetchWorkspacesByNamespace();
    }).catch(() => {
      return this.getOrFetchWorkspaces();
    }).then((workspaces: Array<che.IWorkspace>) => {
      this.usedNamesList = this.buildInListOfUsedNames(workspaces);
      this.reValidateName();
      this.isLoading = false;
    });
  }

  /**
   * Triggers form validation.
   */
  reValidateName(): void {
    if (!this.overviewForm) {
      return;
    }
    const inputName = 'name';
    this.inputmodel = this.overviewForm[inputName] as ng.INgModelController;
    if (!this.inputmodel || !angular.isFunction(this.inputmodel.$validate)) {
      return;
    }
    this.inputmodel.$validate();
  }

  /**
   * Returns promise for getting list of workspaces owned by user
   *
   * @return {ng.IPromise<any>}
   */
  getOrFetchWorkspaces(): ng.IPromise<any> {
    const defer = this.$q.defer();
    const workspacesList = this.cheWorkspace.getWorkspaces();
    if (workspacesList.length) {
      defer.resolve(workspacesList);
    } else {
      this.cheWorkspace.fetchWorkspaces().finally(() => {
        defer.resolve(this.cheWorkspace.getWorkspaces());
      });
    }

    return defer.promise;
  }

  /**
   * Filters list of workspaces by current namespace and
   * builds list of names for current namespace.
   *
   * @param {Array<che.IWorkspace>} workspaces list of workspaces
   * @return {Array<string>}
   */
  buildInListOfUsedNames(workspaces: Array<che.IWorkspace>): Array<string> {
    return workspaces.filter((workspace: che.IWorkspace) => {
      return workspace.namespace === this.namespaceId && this.cheWorkspace.getWorkspaceDataManager().getName(workspace) !== this.workspaceName;
    }).map((workspace: che.IWorkspace) => {
      return this.cheWorkspace.getWorkspaceDataManager().getName(workspace);
    });
  }

  /**
   * Returns promise for getting list of workspaces by namespace.
   *
   * @return {ng.IPromise<any>}
   */
  getOrFetchWorkspacesByNamespace(): ng.IPromise<any> {
    const defer = this.$q.defer();
    if (!this.namespaceId) {
      defer.reject([]);
      return defer.promise;
    }
    const workspacesByNamespaceList = this.cheWorkspace.getWorkspacesByNamespace(this.namespaceId) || [];
    if (workspacesByNamespaceList.length) {
      defer.resolve(workspacesByNamespaceList);
    } else {
      this.cheWorkspace.fetchWorkspacesByNamespace(this.namespaceId).then(() => {
        defer.resolve(this.cheWorkspace.getWorkspacesByNamespace(this.namespaceId) || []);
      }, (error: any) => {
        defer.reject(error);
      });
    }

    return defer.promise;
  }

  /**
   * Returns <code>false</code> if workspace's name is not unique in the namespace.
   * Only member with 'manageWorkspaces' permission can definitely know whether
   * name is unique or not.
   *
   * @param {string} name workspace's name
   */
  isNameUnique(name: string): boolean {
    return !angular.isArray(this.usedNamesList) || this.usedNamesList.indexOf(name) === -1;
  }

  /**
   * Returns current status of workspace.
   *
   * @returns {string}
   */
  getWorkspaceStatus(): string {
    const unknown = 'unknown';
    if (!this.workspaceDetails) {
      return unknown;
    }
    const workspace = this.cheWorkspace.getWorkspaceById(this.workspaceDetails.id);
    return workspace ? workspace.status : unknown;
  }

  /**
   * Removes current workspace.
   */
  deleteWorkspace(): void {
    const content = 'Would you like to delete workspace \'' + this.cheWorkspace.getWorkspaceDataManager().getName(this.workspaceDetails) + '\'?';
    this.confirmDialogService.showConfirmDialog('Delete workspace', content, { resolve: 'Delete' }).then(() => {
      if ([RUNNING, STARTING].indexOf(this.getWorkspaceStatus()) !== -1) {
        this.cheWorkspace.stopWorkspace(this.workspaceDetails.id);
      }
      this.cheWorkspace.fetchStatusChange(this.workspaceDetails.id, STOPPED).then(() => {
        this.cheWorkspace.deleteWorkspace(this.workspaceDetails.id).then(() => {
          this.$location.path('/workspaces').search({});
        }, (error: any) => {
          this.cheNotification.showError('Delete workspace failed.', error);
        });
      });
    });
  }

  /**
   * Track the changes in ephemeral mode input.
   */
  onEphemeralModeChange(): void {
    if (this.isEphemeralMode) {
      this.attributes = this.attributes || {};
      this.attributes.persistVolumes = 'false';
    } else {
      if (!this.attributesCopy) {
        this.attributes = undefined;
      } else {
        if ((this.attributesCopy.persistVolumes as string) === 'true') {
          (this.attributes.persistVolumes as string) = 'true';
        } else {
          delete this.attributes.persistVolumes;
          if (Object.keys(this.attributes).length === 0) {
            this.attributes = undefined;
          }
        }
      }
    }
    this.cheWorkspace.getWorkspaceDataManager().setAttributes(this.workspaceDetails, this.attributes);
    this.onChange();
  }

  /**
   * Callback on name change.
   */
  onNameChange() {
    this.$timeout(() => {
      this.cheWorkspace.getWorkspaceDataManager().setName(this.workspaceDetails, this.name);
      this.onChange();
    });
  }

  /**
   * Callback when Team button is clicked in Edit mode.
   * Redirects to billing details or team details.
   *
   * @param {string} namespaceId
   */
  namespaceOnClick(namespaceId: string): void {
    const namespace = this.getNamespace(namespaceId);
    if (!namespace) {
      return;
    }
    this.$location.path(namespace.location);
  }

  /**
   * Returns workspace details section.
   *
   * @returns {*}
   */
  getSections(): any {
    return this.workspaceDetailsService.getSections();
  }

  /**
   * Returns list of namespaces.
   *
   * @return {Array<che.INamespace>}
   */
  getNamespaces(): Array<che.INamespace> {
    return this.namespaceSelectorSvc.getNamespaces();
  }

  /**
   * Returns the namespace link.
   *
   * @return {string}
   */
  getNamespaceLink(): string {
    if (!this.namespaceId) {
      return null;
    }
    const namespace = this.getNamespace(this.namespaceId);
    if (!namespace) {
      return null;
    }
    return namespace.location;
  }

  /**
   * Returns namespaces empty message if set.
   *
   * @returns {string}
   */
  getNamespaceEmptyMessage(): string {
    return this.namespaceSelectorSvc.getNamespaceEmptyMessage();
  }

  /**
   * Returns namespaces caption.
   *
   * @returns {string}
   */
  getNamespaceCaption(): string {
    return this.namespaceSelectorSvc.getNamespaceCaption();
  }

  private updateInfrastructureNamespace() {
    const namespaceId = this.workspaceDetails.attributes.infrastructureNamespace;
    this.infrastructureNamespace = namespaceId;
    return this.cheKubernetesNamespace.fetchKubernetesNamespace().then(namespaces => {
      const currentNamespace = namespaces.find(namespace => namespace.name === namespaceId);
      if (currentNamespace) {
        this.infrastructureNamespace = currentNamespace.attributes.displayName || currentNamespace.name;
      }
    });
  }

}
