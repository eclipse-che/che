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
import {IEditingProgress, editingProgress} from '../../project-source-selector-editing-progress';

/**
 * This class is handling the service for project's metadata.
 *
 * @author Oleksii Kurinnyi
 */
export class ProjectMetadataService implements IEditingProgress {
  /**
   * The original project template.
   */
  private origProjectTemplate: che.IProjectTemplate;
  /**
   * Edited project template.
   */
  private projectTemplate: che.IProjectTemplate;

  /**
   * Set original project template.
   *
   * @param {che.IProjectTemplate} projectTemplate
   */
  setOrigProjectTemplate(projectTemplate: che.IProjectTemplate): void {
    this.origProjectTemplate = projectTemplate;
  }

  /**
   * Returns project's metadata editing progress.
   *
   * @return {boolean}
   */
  checkEditingProgress(): editingProgress {
    if (!this.projectTemplate) {
      return null;
    }

    const sameName = this.origProjectTemplate.name.trim() === this.projectTemplate.name.trim(),
          sameDescription = this.origProjectTemplate.description.trim() === this.projectTemplate.description.trim(),
          sameSourceLocation =  this.origProjectTemplate.source.location.trim() === this.projectTemplate.source.location.trim();
    if (sameName && sameDescription && sameSourceLocation) {
      return null;
    }

    return {
      number: 1,
      message: `Editing of the project "${this.origProjectTemplate.name}" is not completed yet.`
    };
  }

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
