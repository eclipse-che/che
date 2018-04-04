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
 * Defines a directive for displaying factory from template widget.
 * @author Oleksii Orel
 */
export class FactoryFromTemplate implements ng.IDirective {
  restrict: string;
  templateUrl: string;
  controller: string;
  controllerAs: string;
  bindToController: boolean;
  replace: boolean;

  scope: {
    [propName: string]: string;
  };


  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.restrict = 'E';

    this.templateUrl = 'app/factories/create-factory/template-tab/factory-from-template.html';
    this.replace = false;

    this.controller = 'FactoryFromTemplateController';
    this.controllerAs = 'factoryFromTemplateController';

    this.bindToController = true;

    // scope values
    this.scope = {
      factoryContent: '=',
      isImporting: '=',
      editorState: '=?'
    };
  }

}
