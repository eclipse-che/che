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
import {IPlugin, PluginRegistry} from '../../../../components/api/plugin-registry.factory';
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {CheWorkspace} from '../../../../components/api/workspace/che-workspace.factory';

const PLUGIN_SEPARATOR = ',';
const PLUGIN_VERSION_SEPARATOR = ':';
const EDITOR_TYPE = 'Che Editor';

/**
 * @ngdoc controller
 * @name workspaces.details.plugins.controller:WorkspacePluginsController
 * @description This class is handling the controller for details of workspace : section plugin
 * @author Ann Shumilova
 */
export class WorkspacePluginsController {
  static $inject = ['pluginRegistry', 'cheListHelperFactory', '$scope', 'cheNotification', 'cheWorkspace'];

  workspace: che.IWorkspace;
  pluginRegistryLocation: string;

  pluginRegistry: PluginRegistry;
  cheNotification: CheNotification;
  cheWorkspace: CheWorkspace;
  onChange: Function;
  isLoading: boolean;

  pluginOrderBy = 'displayName';
  plugins: Array<IPlugin> = [];
  selectedPlugins: Array<string> = [];

  pluginFilter: any;

  private cheListHelper: che.widget.ICheListHelper;

  /**
   * Default constructor that is using resource
   */
  constructor(pluginRegistry: PluginRegistry, cheListHelperFactory: che.widget.ICheListHelperFactory, $scope: ng.IScope,
              cheNotification: CheNotification, cheWorkspace: CheWorkspace) {
    this.pluginRegistry = pluginRegistry;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;

    const helperId = 'workspace-plugins';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);

    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.pluginFilter = { displayName: '' };

    const deRegistrationFn = $scope.$watch(() => {
      return this.workspace;
    }, (workspace: che.IWorkspace) => {
      if (!workspace) {
        return;
      }
      this.updatePlugins();
    }, true);

    $scope.$on('$destroy', () => {
      deRegistrationFn();
    });

    this.loadPlugins();
  }

  /**
   * Loads the list of plugins from registry.
   */
  loadPlugins(): void {
    this.plugins = [];
    this.isLoading = true;
    this.pluginRegistry.fetchPlugins(this.pluginRegistryLocation).then((result: Array<IPlugin>) => {
      this.isLoading = false;
      result.forEach((item: IPlugin) => {
        if (item.type !== EDITOR_TYPE) {
          this.plugins.push(item);
        }
      });

      this.updatePlugins();
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to load plugins.');
    });
  }

  /**
   * Callback when name is changed.
   *
   * @param str {string} a string to filter projects names
   */
  onSearchChanged(str: string): void {
    this.pluginFilter.displayName = str;
    this.cheListHelper.applyFilter('displayName', this.pluginFilter);
  }

  /**
   * Update plugin information based on UI changes.
   *
   * @param {IPlugin} plugin
   */
  updatePlugin(plugin: IPlugin): void {
    if (plugin.type !== EDITOR_TYPE) {
      if (plugin.isEnabled) {
        this.selectedPlugins.push(plugin.id);
      } else {
        this.selectedPlugins.splice(this.selectedPlugins.indexOf(plugin.id), 1);
      }
      this.cheWorkspace.getWorkspaceDataManager().setPlugins(this.workspace, this.selectedPlugins);
    }

    this.onChange();
  }

  /**
   * Update the state of plugins.
   */
  private updatePlugins(): void {
    this.selectedPlugins = this.cheWorkspace.getWorkspaceDataManager().getPlugins(this.workspace);
    // check each plugin's enabled state:
    this.plugins.forEach((plugin: IPlugin) => {
      plugin.isEnabled = this.isPluginEnabled(plugin);
    });
    this.cheListHelper.setList(this.plugins, 'displayName');
  }

  /**
   *
   * @param {IPlugin} plugin
   * @returns {boolean} the plugin's enabled state
   */
  private isPluginEnabled(plugin: IPlugin): boolean {
    return this.selectedPlugins.indexOf(plugin.id) >= 0
  }
}
