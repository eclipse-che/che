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
import {IPackage, ISearchResults, NpmRegistry} from '../../../../../components/api/npm-registry.factory';
import {IEnvironmentManagerMachine} from '../../../../../components/api/environment/environment-manager-machine';
import {EnvironmentManager} from '../../../../../components/api/environment/environment-manager';

const THEIA_PLUGINS = 'THEIA_EXTENSIONS';
const CUSTOM_PLUGIN_KEYWORD = 'custom-plugin';
const PLUGIN_SEPARATOR = ',';
const PLUGIN_DESCRIPTION_SEPARATOR = ':';
const DEFAULT_PLUGINS =  ['@theia/typescript', '@theia/navigator', '@theia/terminal', '@theia/outline-view',
  '@theia/preferences', '@theia/git', '@theia/file-search', '@theia/markers', '@theia/extension-manager'];

/**
 * @ngdoc controller
 * @name workspaces.details.tools.controller:WorkspaceDetailsToolsIdeController
 * @description This class is handling the controller for details of workspace ide tool.
 * @author Ann Shumilova
 */
export class WorkspaceToolsIdeController {
  static $inject = ['npmRegistry', 'lodash', 'cheListHelperFactory', '$scope'];
  npmRegistry: NpmRegistry;
  lodash: any;

  packageOrderBy = 'name';
  packages: Array<IPackage>;
  packagesSummary: ISearchResults;
  packagesFilter: any;
  environmentVariables: { [envVarName: string]: string } = {};
  plugins: Array<string>;
  machine: IEnvironmentManagerMachine;
  environmentManager: EnvironmentManager;
  onChange: Function;

  private cheListHelper: che.widget.ICheListHelper;


  /**
   * Default constructor that is using resource
   */
  constructor(npmRegistry: NpmRegistry, lodash: any, cheListHelperFactory: che.widget.ICheListHelperFactory,
              $scope: ng.IScope) {
    this.npmRegistry = npmRegistry;
    this.lodash = lodash;

    const helperId = 'workspace-tools-ide';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);

    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.packagesFilter = {name: ''};

    this.fetchNPMPackages();

    const deRegistrationFn = $scope.$watch(() => {
      return this.machine;
    }, (machine: IEnvironmentManagerMachine) => {
      if (!this.packages) {
        return;
      }
      this.updatePackages();
    }, true);

    $scope.$on('$destroy', () => {
      deRegistrationFn();
    });
  }

  addPackage(name: string, location: string): void {
    this.plugins.push(name + PLUGIN_DESCRIPTION_SEPARATOR + location);
    this.environmentVariables[THEIA_PLUGINS] = this.plugins.join(PLUGIN_SEPARATOR);
    this.environmentManager.setEnvVariables(this.machine, this.environmentVariables);
    this.onChange();
  };

  /**
   * Fetches the list of NPM packages.
   */
  fetchNPMPackages(): void {
    this.npmRegistry.search('keywords:theia-extension').then((data: ISearchResults) => {
      this.packagesSummary = data;
      this.packages = this.lodash.pluck(this.packagesSummary.results, 'package');
      this.updatePackages();
    });
  }

  /**
   * Callback when name is changed.
   *
   * @param str {string} a string to filter projects names
   */
  onSearchChanged(str: string): void {
    this.packagesFilter.name = str;
    this.cheListHelper.applyFilter('name', this.packagesFilter);
  }

  /**
   * Update package information based on UI changes.
   *
   * @param {IPackage} _package
   */
  updatePackage(_package: IPackage): void {
    let name = _package.keywords.indexOf(CUSTOM_PLUGIN_KEYWORD) >= 0 ? _package.name + PLUGIN_DESCRIPTION_SEPARATOR
      + _package.description : _package.name;

    if (_package.isEnabled) {
      this.plugins.push(name);
    } else {
      this.plugins.splice(this.plugins.indexOf(name), 1);
    }

    this.environmentVariables[THEIA_PLUGINS] = this.plugins.join(PLUGIN_SEPARATOR);
    this.environmentManager.setEnvVariables(this.machine, this.environmentVariables);
    this.onChange();
  }

  /**
   * Update the state of packages.
   */
  private updatePackages(): void {
    this.environmentVariables = this.environmentManager.getEnvVariables(this.machine);
    this.plugins = this.machine && this.environmentVariables[THEIA_PLUGINS] ? this.environmentVariables[THEIA_PLUGINS]
      .split(PLUGIN_SEPARATOR) : [];
    this.packages.forEach((_package: IPackage) => {
      let name = _package.keywords.indexOf(CUSTOM_PLUGIN_KEYWORD) >= 0 ? _package.name + PLUGIN_DESCRIPTION_SEPARATOR
        + _package.description : _package.name;
      _package.isEnabled = this.isPackageEnabled(name);
    });

    this.plugins.forEach((plugin: string) => {
      if (plugin.indexOf(PLUGIN_DESCRIPTION_SEPARATOR) > 0) {
        let name = plugin.substring(0, plugin.indexOf(PLUGIN_DESCRIPTION_SEPARATOR));
        let url = plugin.substring(plugin.indexOf(PLUGIN_DESCRIPTION_SEPARATOR) + 1);
        let p = this.packages.find((p: IPackage) => p.name === name);
        if (!p) {
          p = {name : name, isEnabled: true, description: url, version: '-', keywords: [CUSTOM_PLUGIN_KEYWORD]};
          this.packages.push(p);
        }
      }
    });

    this.cheListHelper.setList(this.packages, 'name');
  }

  /**
   * Checks whether package is enabled.
   *
   * @param {string} name package's name
   * @returns {boolean} <code>true</code> true if enabled
   */
  private isPackageEnabled(name: string): boolean {
    return this.plugins.indexOf(name) >= 0 || this.isDefaultPackage(name);
  }

  /**
   * Is package default.
   *
   * @param {string} name
   * @returns {boolean}
   */
  private isDefaultPackage(name: string): boolean {
    return DEFAULT_PLUGINS.indexOf(name) >= 0;
  }
}
