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
import {IPlugin, PluginRegistry, IPluginRow} from '../../../../components/api/plugin-registry.factory';
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {CheWorkspace} from '../../../../components/api/workspace/che-workspace.factory';

const PLUGIN_SEPARATOR = ',';
const PLUGIN_VERSION_SEPARATOR = ':';
const EDITOR_TYPE = 'Che Editor';

/**
 * @ngdoc controller
 * @name workspaces.details.editors.controller:WorkspaceEditorsController
 * @description This class is handling the controller for details of workspace : section editors
 * @author Ann Shumilova
 */
export class WorkspaceEditorsController {
  static $inject = ['pluginRegistry', 'cheListHelperFactory', '$scope', 'cheNotification', 'cheWorkspace'];

  workspace: che.IWorkspace;
  pluginRegistryLocation: string;
  pluginRegistry: PluginRegistry;
  cheNotification: CheNotification;
  cheWorkspace: CheWorkspace;
  onChange: Function;
  isLoading: boolean;

  editorOrderBy = 'displayName';
  editors: Map<string, IPluginRow> = new Map(); // the key is publisher/name
  selectedEditor: string = '';
  editorFilter: any;

  private cheListHelper: che.widget.ICheListHelper;

  /**
   * Default constructor that is using resource
   */
  constructor(pluginRegistry: PluginRegistry, cheListHelperFactory: che.widget.ICheListHelperFactory, $scope: ng.IScope,
    cheNotification: CheNotification, cheWorkspace: CheWorkspace) {
    this.pluginRegistry = pluginRegistry;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;

    const helperId = 'workspace-editors';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);

    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.editorFilter = { displayName: '' };

    const deRegistrationFn = $scope.$watch(() => {
      return this.workspace;
    }, (workspace: che.IWorkspace) => {
      if (!workspace) {
        return;
      }
      this.updateEditors();
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
    this.editors = new Map();
    this.isLoading = true;
    this.pluginRegistry.fetchPlugins(this.pluginRegistryLocation).then((result: Array<IPluginRow>) => {
      this.isLoading = false;
      result.forEach((item: IPluginRow) => {
        if (item.type === EDITOR_TYPE) {

          // since plugin registry returns an array of plugins/editors with a single version we need to unite the editor versions into one
          const pluginID = `${item.publisher}/${item.name}`;

          item.selected = 'latest'; // set the default selected to latest

          if (this.editors.has(pluginID)) {
            const foundPlugin = this.editors.get(pluginID);
            foundPlugin.versions.push(item.version);
          } else {
            item.versions = [item.version];
            this.editors.set(pluginID, item);
          }
        }
      });

      this.updateEditors();
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
    this.editorFilter.displayName = str;
    this.cheListHelper.applyFilter('displayName', this.editorFilter);
  }

  /**
   * Update plugin information based on UI changes.
   *
   * @param {IPlugin} plugin
   */
  updateEditor(plugin: IPluginRow): void {
    if (plugin.type === EDITOR_TYPE) {
      const pluginID = `${plugin.publisher}/${plugin.name}`;
      const pluginIDWithVersion = `${plugin.publisher}/${plugin.name}/${plugin.selected}`;

      this.selectedEditor = plugin.isEnabled ? pluginIDWithVersion : '';

      this.editors.get(pluginID).selected = plugin.selected;
      this.editors.get(pluginID).id = pluginIDWithVersion;

      this.cheWorkspace.getWorkspaceDataManager().setEditor(this.workspace, this.selectedEditor);
    }

    this.onChange();
  }

  /**
   * Update the selected editor version when the editor version dropdown is changed
   *
   * @param {IPlugin} plugin
   */
  updateSelectedEditorVersion(plugin: IPluginRow): void {
    if (plugin.type === EDITOR_TYPE) {
      const pluginID = `${plugin.publisher}/${plugin.name}`;

      // create a plugin id with the newly selected version
      const pluginIDWithVersion = `${pluginID}/${plugin.selected}`;

      this.editors.get(pluginID).selected = plugin.selected;
      this.editors.get(pluginID).id = pluginIDWithVersion;

      this.cheWorkspace.getWorkspaceDataManager().setEditor(this.workspace, pluginIDWithVersion);
    }
    this.onChange();
  }

  /**
   * Update the state of plugins.
   */
  private updateEditors(): void {
    this.selectedEditor = this.cheWorkspace.getWorkspaceDataManager().getEditor(this.workspace);

    // check each editor's enabled state:
    this.editors.forEach((editor: IPluginRow) => {
      editor.isEnabled = this.isEditorEnabled(editor);
      if (editor.isEnabled) {

        // this.selectedEditor is in the form publisher/name/pluginVersion
        const { publisher, name, version } = this.splitEditorId(this.selectedEditor);
        editor.selected = version;
      }
    });

    this.cheListHelper.setList(Array.from(this.editors.values()), 'name');
  }

  /**
   *
   * @param {IPlugin} plugin
   * @returns {boolean} the editor's enabled state
   */
  private isEditorEnabled(editor: IPlugin): boolean {
    const partialId = `${editor.publisher}/${editor.name}/`;
    return this.selectedEditor && this.selectedEditor.indexOf(partialId) !== -1;
  }

  /**
   * Splits an editor ID by a separator (slash)
   * @param id a string in form `${publisher}/${name}` or `${publisher}/${name}/${version}`
   */
  private splitEditorId(id: string): { publisher: string, name: string, version?: string } {
    const parts = id.split('/');
    return {
      publisher: parts[0],
      name: parts[1],
      version: parts[2]
    };
  }

}
