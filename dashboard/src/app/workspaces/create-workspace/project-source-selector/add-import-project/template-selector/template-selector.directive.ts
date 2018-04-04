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

/**
 * Defines a directive for the template selector.
 *
 * @author Oleksii Kurinnyi
 */
export class TemplateSelector implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/create-workspace/project-source-selector/add-import-project/template-selector/template-selector.html';
  replace: boolean = true;

  controller: string = 'TemplateSelectorController';
  controllerAs: string = 'templateSelectorController';

  bindToController: boolean = true;

  scope: {
    [propName: string]: string;
  };

  /**
   * Default constructor that is using resource injection
   */
  constructor() {
    this.scope = {
      stackTags: '='
    };
  }

}
