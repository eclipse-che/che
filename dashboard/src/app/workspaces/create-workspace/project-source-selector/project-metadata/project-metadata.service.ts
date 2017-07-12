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
 * This class is handling the service for project's metadata.
 *
 * @author Oleksii Kurinnyi
 */
export class ProjectMetadataService {
  /**
   * The project template.
   */
  private projectTemplate: che.IProjectTemplate;

  /**
   * Callback which is called when metadata is changed.
   *
   * @param {che.IProjectTemplate} projectTemplate the project's template
   */
  onMetadataChanged(projectTemplate: che.IProjectTemplate): void {
    this.projectTemplate = projectTemplate;
  }

  /**
   * Returns project template.
   *
   * @return {che.IProjectTemplate}
   */
  getProjectTemplate(): che.IProjectTemplate {
    this.projectTemplate.displayName = this.projectTemplate.name;
    this.projectTemplate.path = '/' +  this.projectTemplate.name.replace(/[^\w-_]/g, '_');

    return this.projectTemplate;
  }

}
