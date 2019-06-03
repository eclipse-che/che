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
  editors: Array<IPlugin> = [];
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

    this.loadPlugins();
  }

  /**
   * Loads the list of plugins from registry.
   */
  loadPlugins(): void {
    this.editors = [];
    this.isLoading = true;
    this.pluginRegistry.fetchPlugins(this.pluginRegistryLocation).then((result: Array<IPlugin>) => {
      this.isLoading = false;
      result.forEach((item: IPlugin) => {
        if (item.type === EDITOR_TYPE) {
          this.editors.push(item);
        };
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
  updateEditor(plugin: IPlugin): void {
    if (plugin.type === EDITOR_TYPE) {
      this.selectedEditor = plugin.isEnabled ? plugin.id : '';
      this.cheWorkspace.getWorkspaceDataManager().setEditor(this.workspace, this.selectedEditor);
    }
    
    this.onChange();
  }

  /**
   * Update the state of plugins.
   */
  private updateEditors(): void {
    this.selectedEditor = this.cheWorkspace.getWorkspaceDataManager().getEditor(this.workspace);

    // check each editor's enabled state:
    this.editors.forEach((editor: IPlugin) => {
      editor.isEnabled = this.isEditorEnabled(editor);
    });

    this.cheListHelper.setList(this.editors, 'name');
  }

  /**
   *
   * @param {IPlugin} plugin
   * @returns {boolean} the editor's enabled state
   */
  private isEditorEnabled(editor: IPlugin): boolean {
    return editor.id === this.selectedEditor;
  }
}
