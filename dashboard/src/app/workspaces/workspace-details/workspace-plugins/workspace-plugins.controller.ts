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
import {IPlugin, IPluginRow, PluginRegistry} from '../../../../components/api/plugin-registry.factory';
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {CheWorkspace} from '../../../../components/api/workspace/che-workspace.factory';

/**
 * @ngdoc controller
 * @name workspaces.details.plugins.controller:WorkspacePluginsController
 * @description This class is handling the controller for details of workspace : section plugin
 * @author Ann Shumilova
 */
export class WorkspacePluginsController {
  static $inject = ['pluginRegistry', 'cheListHelperFactory', '$scope', 'cheNotification', 'cheWorkspace', '$sce'];

  workspace: che.IWorkspace;
  pluginRegistryLocation: string;

  pluginRegistry: PluginRegistry;
  cheNotification: CheNotification;
  cheWorkspace: CheWorkspace;
  $sce: ng.ISCEService;

  onChange: Function;
  isLoading: boolean;

  pluginOrderBy = 'displayName';
  plugins: Map<string, IPluginRow> = new Map(); // the key is publisher/name
  selectedPlugins: Array<string> = [];
  deprecatedPluginsInfo: Map<string, {automigrate: boolean; migrateTo: string;}> = new Map();

  private pluginFilter: {displayName: string};

  private cheListHelper: che.widget.ICheListHelper;

  /**
   * Default constructor that is using resource
   */
  constructor(pluginRegistry: PluginRegistry, cheListHelperFactory: che.widget.ICheListHelperFactory, $scope: ng.IScope,
              cheNotification: CheNotification, cheWorkspace: CheWorkspace, $sce: ng.ISCEService) {
    this.pluginRegistry = pluginRegistry;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;
    this.$sce = $sce;

    const helperId = 'workspace-plugins';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);

    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.pluginFilter = {displayName: ''};

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
  }

  $onInit(): void {
    this.loadPlugins();
  }

  /**
   * Loads the list of plugins from registry.
   */
  loadPlugins(): void {
    this.plugins = new Map();
    this.deprecatedPluginsInfo = new Map();
    this.isLoading = true;
    this.pluginRegistry.fetchPlugins(this.pluginRegistryLocation).then((result: Array<IPlugin>) => {
      this.isLoading = false;
      result.filter(item => item.type !== PluginRegistry.EDITOR_TYPE).forEach((item: IPlugin) => {
        // since plugin registry returns an array of plugins/editors with a single version we need to unite the plugin versions into one
        const pluginID = `${item.publisher}/${item.name}`;
        // set the default selected to latest
        const selected = 'latest';

        if (!this.plugins.has(pluginID)) {
          const {name, displayName, description, publisher} = item;
          const versions = [];
          const id =  `${pluginID}/${selected}`;
          this.plugins.set(pluginID, {id, name, displayName, description, publisher, selected, versions});
        }

        const value = item.version;
        const label = !item.deprecate ? item.version : `${item.version}  [DEPRECATED]`;
        this.plugins.get(pluginID).versions.push({value, label});
        if (item.deprecate) {
          this.deprecatedPluginsInfo.set(item.id, item.deprecate);
          if (selected === item.version) {
            this.plugins.get(pluginID).isDeprecated = true;
          }
        }
      });

      this.selectedPlugins.forEach(plugin => {
        // a selected plugin is in the form publisher/name/version
        // find the currently selected ones and set them along with their id
        const {publisher, name, version} = this.splitPluginId(plugin);
        const pluginID = `${publisher}/${name}`;

        if (this.plugins.has(pluginID)) {
          const foundPlugin = this.plugins.get(pluginID);
          foundPlugin.id = plugin;
          foundPlugin.selected = version;
          foundPlugin.isDeprecated = this.isDeprecatedPlugin(plugin);
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
  updatePlugin(plugin: IPluginRow): void {
    const pluginID = `${plugin.publisher}/${plugin.name}`;
    const pluginIDWithVersion = `${plugin.publisher}/${plugin.name}/${plugin.selected}`;

    this.plugins.get(pluginID).selected = plugin.selected;
    this.plugins.get(pluginID).id = pluginIDWithVersion;

    if (plugin.isEnabled) {
      this.selectedPlugins.push(pluginIDWithVersion);
    } else {
      this.selectedPlugins.splice(this.selectedPlugins.indexOf(plugin.id), 1);
    }

    this.cheWorkspace.getWorkspaceDataManager().setPlugins(this.workspace, this.selectedPlugins);

    this.onChange();
  }

  /**
   * Update the selected plugin version when the plugin version dropdown is changed
   *
   * @param {IPlugin} plugin
   */
  updateSelectedPlugin(plugin: IPluginRow): void {
    const pluginID = `${plugin.publisher}/${plugin.name}`;
    const pluginIDWithVersion = `${pluginID}/${plugin.selected}`;

    this.plugins.get(pluginID).isDeprecated = this.isDeprecatedPlugin(pluginIDWithVersion);
    this.plugins.get(pluginID).selected = plugin.selected;
    this.plugins.get(pluginID).id = pluginIDWithVersion;

    const currentlySelectedPlugins = this.cheWorkspace.getWorkspaceDataManager().getPlugins(this.workspace);

    if (plugin.isEnabled) {
      currentlySelectedPlugins.splice(this.selectedPlugins.indexOf(plugin.id), 1, pluginIDWithVersion);
    } else {
      currentlySelectedPlugins.push(pluginIDWithVersion);
    }

    this.cheWorkspace.getWorkspaceDataManager().setPlugins(this.workspace, currentlySelectedPlugins);
    this.selectedPlugins = currentlySelectedPlugins;

    this.onChange();
  }

  /**
   * Returns a warning message for the plugin.
   *
   * @param {string} pluginId
   * @returns {any} warning message
   */
  getWarningMessage(pluginId: string): any {
    let warningMessage = 'This plugin is deprecated.';
    const deprecatedInfo = this.deprecatedPluginsInfo.get(pluginId);
    if (deprecatedInfo && deprecatedInfo.migrateTo) {
      const {publisher, name} = this.splitPluginId(pluginId);
      const pluginToMigrate = this.splitPluginId(deprecatedInfo.migrateTo);
      if (pluginToMigrate.publisher === publisher && pluginToMigrate.name === name) {
        warningMessage += ' Select a newer version in the dropdown list.';
      } else {
        const targetPlugin = this.plugins.get(`${pluginToMigrate.publisher}/${pluginToMigrate.name}`);
        if (targetPlugin) {
          warningMessage += ` Use <b>${targetPlugin.displayName} (${targetPlugin.publisher} publisher)</b>.`;
        }
      }
    }
    return this.$sce.trustAsHtml(warningMessage);
  }

  /**
   * Returns true if the plugin deprecated.
   *
   * @param {string} pluginId
   */
  isDeprecatedPlugin(pluginId: string): boolean {
    return this.deprecatedPluginsInfo.has(pluginId);
  }

  /**
   * Auto-migrate plugins.
   *
   * @param {string[]} plugins
   */
  private autoMigratePlugins(plugins: string[]): void {
    const currentlySelectedPlugins = this.cheWorkspace.getWorkspaceDataManager().getPlugins(this.workspace);
    plugins.forEach((pluginId: string) => {
      const deprecatedInfo = this.deprecatedPluginsInfo.get(pluginId);
      if (deprecatedInfo && deprecatedInfo.automigrate && deprecatedInfo.migrateTo) {
        if (this.selectedPlugins.indexOf(deprecatedInfo.migrateTo) === -1) {
          currentlySelectedPlugins.splice(this.selectedPlugins.indexOf(pluginId), 1, deprecatedInfo.migrateTo);
        } else {
          currentlySelectedPlugins.splice(this.selectedPlugins.indexOf(pluginId), 1);
        }
      }
    });
    this.cheWorkspace.getWorkspaceDataManager().setPlugins(this.workspace, currentlySelectedPlugins);
    this.selectedPlugins = currentlySelectedPlugins;

    this.onChange();
  }

  /**
   * Update the state of plugins.
   */
  private updatePlugins(): void {
    this.selectedPlugins = this.cheWorkspace.getWorkspaceDataManager().getPlugins(this.workspace);
    // check each plugin's enabled state:
    this.plugins.forEach((plugin: IPluginRow) => {
      const selectedPluginId = this.findInSelected(plugin);
      plugin.isEnabled = !!selectedPluginId;
      if (selectedPluginId) {
        plugin.id = selectedPluginId;
        plugin.isDeprecated = this.isDeprecatedPlugin(plugin.id);
        const {version} = this.splitPluginId(selectedPluginId);
        plugin.selected = version;
      }
    });
    this.cheListHelper.setList(Array.from(this.plugins.values()), 'displayName');
  }

  /**
   * Finds given plugin in the list of enabled plugins and returns its ID with version
   * @param {IPlugin} plugin
   * @returns {string} plugin ID
   */
  private findInSelected(plugin: IPluginRow): string {
    const pluginId = this.selectedPlugins.find(selectedPluginId => {
      const partialId = `${plugin.publisher}/${plugin.name}/`;
      return selectedPluginId.indexOf(partialId) !== -1;
    });
    return !!pluginId ? pluginId : '';
  }

  /**
   * Splits a plugin ID by a separator (slash)
   * @param id a string in form `${publisher}/${name}` or `${publisher}/${name}/${version}`
   */
  private splitPluginId(id: string): { publisher: string, name: string, version?: string } {
    const parts = id.split('/');
    return {
      publisher: parts[0],
      name: parts[1],
      version: parts[2]
    };
  }

}
