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
 * This class is handling the service for the Zip project import.
 *
 * @author Oleksii Kurinnyi
 */
export class ImportZipProjectService implements IEditingProgress {
  /**
   * Zip project location.
   */
  private _location: string;
  /**
   * Skip root folder if <code>true</code>.
   */
  private _skipFirstLevel: boolean;

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this._location = '';
    this._skipFirstLevel = false;
  }

  get location(): string {
    return this._location;
  }

  get skipFirstLevel(): boolean {
    return this._skipFirstLevel;
  }

  /**
   * Returns resolved promise if there is no project to add. Otherwise returns rejected promise.
   *
   * @return {editingProgress}
   */
  checkEditingProgress(): editingProgress {
    if (!this.location) {
      return null;
    }

    return {
      message: 'Adding of a project from ZIP archive is not completed yet.',
      number: 1
    };
  }

  /**
   * Callback which is called when location is changed.
   *
   * @param {string=""} location Zip project location
   * @param {boolean=false} skipFirstLevel skip root folder if <code>true</code>
   */
  onChanged(location: string = '', skipFirstLevel: boolean = false): void {
    this._location = location;
    this._skipFirstLevel = skipFirstLevel;
  }

  /**
   * Returns project's properties.
   *
   * @return {che.IProjectTemplate}
   */
  getProjectProps(): che.IProjectTemplate {
    const props = {} as che.IProjectTemplate;

    const [ , name] = /\/([^\/]+)\.zip$/i.exec(this._location);
    const path = '/' +  name.replace(/[^\w-_]/g, '_');
    props.name = name;
    props.displayName = name;
    props.description = '';
    props.path = path;
    props.category = '';

    props.source = {
      type: 'zip',
      location: this._location,
      parameters: {
        skipFirstLevel: this._skipFirstLevel
      }
    };

    return props;
  }

}
