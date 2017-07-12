/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * This class is handling the service for the Zip project import.
 *
 * @author Oleksii Kurinnyi
 */
export class ImportZipProjectService {
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
   * @ngInject for Dependency injection
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
