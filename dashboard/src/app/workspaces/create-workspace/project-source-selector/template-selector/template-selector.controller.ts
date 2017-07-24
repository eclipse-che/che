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

import {TemplateSelectorSvc} from './template-selector.service';
import {StackSelectorSvc} from '../../stack-selector/stack-selector.service';
import {ProjectSourceSelectorService} from '../project-source-selector.service';
import {IProjectSourceSelectorServiceObserver} from '../project-source-selector-service.observer';
import {ProjectSource} from '../project-source.enum';
import {ActionType} from '../project-source-selector-action-type.enum';

/**
 * This class is handling the controller for template selector.
 *
 * @author Oleksii Kurinnyi
 */
export class TemplateSelectorController implements IProjectSourceSelectorServiceObserver {
  /**
   * Filter service.
   */
  private $filter: ng.IFilterService;
  /**
   * Project source selector service.
   */
  private projectSourceSelectorService: ProjectSourceSelectorService;
  /**
   * Template selector service.
   */
  private templateSelectorSvc: TemplateSelectorSvc;
  /**
   * Stack selector service.
   */
  private stackSelectorSvc: StackSelectorSvc;
  /**
   * Helper for lists.
   */
  private cheListHelper: che.widget.ICheListHelper;
  /**
   * The list of tags of selected stack.
   */
  private stackTags: string[];
  /**
   * Sorted list of all templates.
   */
  private allTemplates: Array<che.IProjectTemplate>;
  /**
   * Filtered and sorted list of templates.
   */
  private filteredTemplates: Array<che.IProjectTemplate>;
  /**
   * List of selected templates.
   */
  private selectedTemplates: Array<che.IProjectTemplate>;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($filter: ng.IFilterService, $scope: ng.IScope, projectSourceSelectorService: ProjectSourceSelectorService, templateSelectorSvc: TemplateSelectorSvc, stackSelectorSvc: StackSelectorSvc, cheListHelperFactory: che.widget.ICheListHelperFactory) {
    this.$filter = $filter;
    this.projectSourceSelectorService = projectSourceSelectorService;
    this.templateSelectorSvc = templateSelectorSvc;
    this.stackSelectorSvc = stackSelectorSvc;

    const helperId = 'template-selector';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.filteredTemplates = [];
    this.selectedTemplates = this.templateSelectorSvc.getTemplates();

    this.allTemplates = this.$filter('orderBy')(this.templateSelectorSvc.getAllTemplates(), ['projectType', 'displayName']);
    this.filterAndSortTemplates();

    this.onStackChanged();
    this.stackSelectorSvc.subscribe(this.onStackChanged.bind(this));

    this.projectSourceSelectorService.subscribe(this.onProjectSourceSelectorServicePublish.bind(this));
  }

  /**
   * Callback which is called when stack is selected.
   */
  onStackChanged(): void {
    const stackId = this.stackSelectorSvc.getStackId();
    if (!stackId) {
      return;
    }

    const stack = this.stackSelectorSvc.getStackById(stackId);
    this.stackTags = stack ? stack.tags : [];

    this.filterAndSortTemplates();
  }

  /**
   * Callback which is called when project template is added to the list of ready-to-import projects.
   * Make samples not checked.
   *
   * @param {ActionType} action the type of action
   * @param {ProjectSource} source the project's source
   */
  onProjectSourceSelectorServicePublish(action: ActionType, source: ProjectSource): void {
    if (action !== ActionType.ADD_PROJECT || source !== ProjectSource.SAMPLES) {
      return;
    }

    this.cheListHelper.deselectAllItems();
    this.selectedTemplates = [];

    this.templateSelectorSvc.onTemplateSelected(this.selectedTemplates);
  }

  /**
   * Filters templates by tags and sort them by project type and template name.
   */
  filterAndSortTemplates(): void {
    const stackTags = !this.stackTags ? [] : this.stackTags.map((tag: string) => tag.toLowerCase());

    if (stackTags.length) {
      this.filteredTemplates = this.allTemplates.filter((template: che.IProjectTemplate) => {
        const templateTags = template.tags.map((tag: string) => tag.toLowerCase());
        return stackTags.some((tag: string) => templateTags.indexOf(tag) > -1);
      });
    }

    this.cheListHelper.setList(this.filteredTemplates, 'name');
    this.selectedTemplates.forEach((template: che.IProjectTemplate) => {
      this.cheListHelper.itemsSelectionStatus[template.name] = true;
    });
  }

  /**
   * Callback which is when the template checkbox is clicked.
   */
  onTemplateChanged(): void {
    this.selectedTemplates = this.cheListHelper.getSelectedItems() as Array<che.IProjectTemplate>;
    this.templateSelectorSvc.onTemplateSelected(this.selectedTemplates);
  }

}
