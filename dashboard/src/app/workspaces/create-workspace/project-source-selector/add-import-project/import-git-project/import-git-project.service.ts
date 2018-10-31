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
import {editingProgress, IEditingProgress} from '../../project-source-selector-editing-progress';

/**
 * This class is handling the service for the Git project import.
 *
 * @author Oleksii Kurinnyi
 */
export class ImportGitProjectService implements IEditingProgress {
  /**
   * Git project location.
   */
  private _location: string;

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this._location = '';
  }

  get location(): string {
    return this._location;
  }

  /**
   * Returns project's adding progress.
   *
   * @return {editingProgress}
   */
  checkEditingProgress(): editingProgress {
    if (!this.location) {
      return null;
    }

    return {
      message: 'Adding of a project from Git repository is not completed yet.',
      number: 1
    };
  }

  /**
   * Callback which is called when location is changed.
   *
   * @param {string=""} location
   */
  onLocationChanged(location: string = ''): void {
    this._location = location;
  }

  /**
   * Returns project's properties.
   *
   * @return {che.IProjectTemplate}
   */
  getProjectProps(): che.IProjectTemplate {
    const props = {} as che.IProjectTemplate;
    const regExpExecArray = /.*\/([^\/]+?)(?:.git)?$/i.exec(this._location);
    const name = angular.isArray(regExpExecArray) && regExpExecArray.length > 1 ? regExpExecArray[1] : 'new-project';
    const path = '/' +  name.replace(/[^\w-_]/g, '_');
    props.name = name;
    props.displayName = name;
    props.description = '';
    props.path = path;
    props.category = '';

    props.source = {
      type: 'git',
      location: this._location
    };

    return props;
  }

}
