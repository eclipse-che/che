/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {ProjectMetadataService} from './project-metadata/project-metadata.service';
import {IObservable, IObservableCallbackFn, Observable} from '../../../../../components/utils/observable';
import {editingProgress} from '../project-source-selector-editing-progress';

/**
 * This class is handling the service for project editing.
 *
 * @author Oleksii Kurinnyi
 */
export class EditProjectService {

  static $inject = ['projectMetadataService'];

  /**
   * Service for editing project's metadata.
   */
  private projectMetadataService: ProjectMetadataService;
  private observable: IObservable<any>;

  /**
   * Default constructor that is using resource injection
   */
  constructor(projectMetadataService: ProjectMetadataService) {
    this.projectMetadataService = projectMetadataService;

    this.observable = new Observable<any>();
  }

  /**
   * Add callback to the list of subscribers.
   *
   * @param {IObservableCallbackFn<any>} action
   */
  subscribe(action: IObservableCallbackFn<any>): void {
    this.observable.subscribe(action);
  }

  /**
   * Remove callback from the list of subscribers.
   *
   * @param {IObservableCallbackFn<any>} action
   */
  unsubscribe(action: IObservableCallbackFn<any>): void {
    this.observable.unsubscribe(action);
  }

  /**
   * Returns project template.
   *
   * @return {che.IProjectTemplate}
   */
  getProjectTemplate(): che.IProjectTemplate {
    return this.projectMetadataService.getProjectTemplate();
  }

  /**
   * Restore template to initial state.
   */
  restoreTemplate(): void {
    this.observable.publish(null);
  }

  /**
   * Checks if any project's template is adding or importing.
   *
   * @return {editingProgress}
   */
  checkEditingProgress(): editingProgress {
    return this.projectMetadataService.checkEditingProgress();
  }

}
