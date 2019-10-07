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
import {CheWorkspace} from '../../../components/api/workspace/che-workspace.factory';
import {DevfileRegistry, IDevfileMetaData} from '../../../components/api/devfile-registry.factory';

const DEFAULT_COLUMN = 'displayName';

/**
 * @ngdoc controller
 * @name stacks.list.controller:ListStacksCtrl
 * @description This class is handling the controller for listing the stacks
 * @author Ann Shumilova
 * @author Oleksii Orel
 */
export class ListStacksController {

  static $inject = ['$scope', 'cheWorkspace', '$location', 'devfileRegistry', 'cheListHelperFactory'];

  private $location: ng.ILocationService;

  private cheWorkspace: CheWorkspace;
  private devfileRegistry: DevfileRegistry;
  private cheListHelper: che.widget.ICheListHelper;

  private orderBy: string;
  private searchBy: string;
  private searchStr: string;
  private pluginRegistryUrl: string;

  private isLoading: boolean;

  /**
   * Default constructor that is using resource
   */
  constructor($scope: ng.IScope,
               cheWorkspace: CheWorkspace,
               $location: ng.ILocationService,
               devfileRegistry: DevfileRegistry,
               cheListHelperFactory: che.widget.ICheListHelperFactory) {
    this.$location = $location;
    this.cheWorkspace = cheWorkspace;
    this.devfileRegistry = devfileRegistry;

    const helperId = 'devfiles-meta-list';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.orderBy = DEFAULT_COLUMN;

    // TODO remove this after cheListHelper improvement
    this.searchBy = 'tmpFilterColumn';

    this.loadDevfiles();
  }

  $onInit(): void {
    // this method won't be called here
    // place all initialization code in constructor
  }

  loadDevfiles(): void {
    this.isLoading = true;
    this.pluginRegistryUrl = this.cheWorkspace.getWorkspaceSettings().cheWorkspaceDevfileRegistryUrl;
    this.devfileRegistry.fetchDevfiles(this.pluginRegistryUrl).then((data: Array<IDevfileMetaData>) => {
      const devfileMetaDatas = data.map((devfileMetaData: IDevfileMetaData) => {

        // TODO remove this after cheListHelper improvement
        devfileMetaData[this.searchBy]= `${devfileMetaData.displayName} ${devfileMetaData.description} ${devfileMetaData.globalMemoryLimit}`;

        return devfileMetaData;
      });
      this.cheListHelper.setList(devfileMetaDatas, DEFAULT_COLUMN);
    }, (error: any) => {
      console.log('Failed to load devfiles meta list', error);
    }).finally(() => {
      this.isLoading = false;
    });
  }

  onSearchChanged(searchStr: string): void {
    this.searchStr = searchStr;
    this.updateFilters();
  }

  onDevfileSelected(devfile: IDevfileMetaData): void {
    if (devfile.links && devfile.links.self) {
      this.$location.path(`/stack/${this.devfileRegistry.selfLinkToDevfileId(devfile.links.self)}`).search({});
    }
  }

  updateFilters(): void {
    this.cheListHelper.clearFilters();
    const filter: {[searchBy: string]: string} =  {};
    if (this.searchStr) {
      filter[this.searchBy] = this.searchStr;
    }
    this.cheListHelper.applyFilter(this.searchBy, filter);
  }
}
