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

/**
 * @ngdoc controller
 * @name workspaces.details.editors.controller:WorkspaceEditorsController
 * @description This class is handling the controller for details of workspace : section editors
 * @author Ann Shumilova
 */
export class WorkspaceEditorsController {
  static $inject = ['pluginRegistry', 'cheListHelperFactory', '$scope', 'cheNotification', 'cheWorkspace', '$sce'];

  workspace: che.IWorkspace;
  pluginRegistryLocation: string;

  pluginRegistry: PluginRegistry;
  cheNotification: CheNotification;
  cheWorkspace: CheWorkspace;
  $sce: ng.ISCEService;

  onChange: Function;
  isLoading: boolean;

  editorOrderBy = 'displayName';
  editors: Map<string, IPluginRow> = new Map(); // the key is publisher/name
  selectedEditor: string = '';
  deprecatedEditorsInfo: Map<string, {automigrate: boolean; migrateTo: string;}> = new Map();

  private editorFilter: {displayName: string};

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

    const helperId = 'workspace-editors';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);

    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.editorFilter = {displayName: ''};

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
    this.loadEditors();
  }

  /**
   * Loads the list of editors from registry.
   */
  loadEditors(): void {
    this.editors = new Map();
    this.deprecatedEditorsInfo = new Map();
    this.isLoading = true;
    this.pluginRegistry.fetchPlugins(this.pluginRegistryLocation).then((result: Array<IPlugin>) => {
      this.isLoading = false;
      result.filter(item => item.type === PluginRegistry.EDITOR_TYPE).forEach((item: IPlugin) => {
        // since editor registry returns an array of editors/editors with a single version we need to unite the editor versions into one
        const editorID = `${item.publisher}/${item.name}`;
        // set the default selected to latest
        const selected = 'latest';

        if (!this.editors.has(editorID)) {
          const {name, displayName, description, publisher} = item;
          const versions = [];
          const id =  `${editorID}/${selected}`;
          this.editors.set(editorID, {id, name, displayName, description, publisher, selected, versions});
        }

        const value = item.version;
        const label = !item.deprecate ? item.version : `${item.version}  [DEPRECATED]`;
        this.editors.get(editorID).versions.push({value, label});
        if (item.deprecate) {
          this.deprecatedEditorsInfo.set(item.id, item.deprecate);
          if (selected === item.version) {
            this.editors.get(editorID).isDeprecated = true;
          }
        }
      });

        const {publisher, name, version} = this.splitEditorId(this.selectedEditor);
        const editorID = `${publisher}/${name}`;

        if (this.editors.has(editorID)) {
          const foundEditor = this.editors.get(editorID);
          foundEditor.id = editorID;
          foundEditor.selected = version;
          foundEditor.isDeprecated = this.isDeprecatedEditor(editorID);
        }

      this.updateEditors();
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to load editors.');
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
   * Update editor information based on UI changes.
   *
   * @param {IPlugin} editor
   */
  updateEditor(editor: IPluginRow): void {

      const editorID = `${editor.publisher}/${editor.name}`;
      const editorIDWithVersion = `${editor.publisher}/${editor.name}/${editor.selected}`;

      this.selectedEditor = editor.isEnabled ? editorIDWithVersion : '';

      this.editors.get(editorID).selected = editor.selected;
      this.editors.get(editorID).id = editorIDWithVersion;

      this.cheWorkspace.getWorkspaceDataManager().setEditor(this.workspace, this.selectedEditor);

    this.onChange();
  }

  /**
   * Update the selected editor version when the editor version dropdown is changed
   *
   * @param {IPluginRow} editor
   */
  updateSelectedEditorVersion(editor: IPluginRow): void {
    const editorID = `${editor.publisher}/${editor.name}`;

    // create a editor id with the newly selected version
    const editorIDWithVersion = `${editorID}/${editor.selected}`;

    this.editors.get(editorID).isDeprecated = this.isDeprecatedEditor(editorIDWithVersion);
    this.editors.get(editorID).selected = editor.selected;
    this.editors.get(editorID).id = editorIDWithVersion;

    this.cheWorkspace.getWorkspaceDataManager().setEditor(this.workspace, editorIDWithVersion);

    this.onChange();
  }

  /**
   * Returns a warning message for the editor.
   *
   * @returns {any} warning message
   */
  getWarningMessage(): any {
    let warningMessage = 'This editor is deprecated.';
    const deprecatedInfo = this.deprecatedEditorsInfo.get(this.selectedEditor);
    if (deprecatedInfo && deprecatedInfo.migrateTo) {
      const {publisher, name} = this.splitEditorId(this.selectedEditor);
      const pluginToMigrate = this.splitEditorId(deprecatedInfo.migrateTo);
      if (pluginToMigrate.publisher === publisher && pluginToMigrate.name === name) {
        warningMessage += ' Select a newer version in the dropdown list.';
      } else {
        const targetEditor = this.editors.get(`${pluginToMigrate.publisher}/${pluginToMigrate.name}`);
        if (targetEditor) {
          warningMessage += ` Use <b>${targetEditor.displayName} (${targetEditor.publisher} publisher)</b>.`;
        }
      }
    }

    return this.$sce.trustAsHtml(warningMessage);
  }

  /**
   * Returns true if the editor deprecated.
   *
   * @param {string} editorId
   */
  isDeprecatedEditor(editorId: string): boolean {
    return this.deprecatedEditorsInfo.has(editorId);
  }

  /**
   * Auto-migrate the deprecated editor.
   */
  autoMigrateDeprecatedEditor(): void {
    if (!this.deprecatedEditorsInfo.has(this.selectedEditor)) {
      return;
    }
    const deprecatedInfo = this.deprecatedEditorsInfo.get(this.selectedEditor);
    if (deprecatedInfo && deprecatedInfo.automigrate && deprecatedInfo.migrateTo) {
      this.cheWorkspace.getWorkspaceDataManager().setEditor(this.workspace, deprecatedInfo.migrateTo);
      this.selectedEditor = deprecatedInfo.migrateTo;
    }

    this.onChange();
  }

  /**
   * Update the state of editors.
   */
  private updateEditors(): void {
    this.selectedEditor = this.cheWorkspace.getWorkspaceDataManager().getEditor(this.workspace);

    // check each editor's enabled state:
    this.editors.forEach((editor: IPluginRow) => {
      editor.isEnabled = this.isEditorEnabled(editor);
      if (editor.isEnabled) {
        editor.isDeprecated = this.isDeprecatedEditor(this.selectedEditor);
        const {version} = this.splitEditorId(this.selectedEditor);
        editor.selected = version;
      }
    });

    this.cheListHelper.setList(Array.from(this.editors.values()), 'name');
  }

  /**
   *
   * @param {IPluginRow} editor
   * @returns {boolean} the editor's enabled state
   */
  private isEditorEnabled(editor: IPluginRow): boolean {
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
