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
 * This class is handling the service for the blank project import.
 *
 * @author Oleksii Kurinnyi
 */
export class ImportBlankProjectService implements IEditingProgress {
  /**
   * Project's name.
   */
  private _name: string;
  /**
   * Project's description.
   */
  private _description: string;

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this._name = '';
    this._description = '';
  }

  get name(): string {
    return this._name;
  }

  get description(): string {
    return this._description;
  }

  /**
   * Returns project's adding progress.
   *
   * @return {editingProgress}
   */
  checkEditingProgress(): editingProgress {
    if (!this.name && !this.description) {
      return null;
    }

    return {
      message: 'Adding of a project from "Blank" template is not completed yet.',
      number: 1
    };
  }

  /**
   * Callback which is called when project's name or description is changed.
   *
   * @param {string=""} name the project's name
   * @param {string=""} description the project's description
   */
  onChanged(name: string = '', description: string = ''): void {
    this._name = name;
    this._description = description;
  }

  /**
   * Returns project's properties.
   *
   * @return {che.IProjectTemplate}
   */
  getProjectProps(): che.IProjectTemplate {
    const props = {} as che.IProjectTemplate;

    props.name = this._name;
    props.displayName = this._name;
    props.description = this._description;
    const path = '/' +  this._name.replace(/[^\w-_]/g, '_');
    props.path = path;
    props.category = '';

    return props;
  }

}
