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
 * Defines a directive for displaying factory commands.
 * @author Florent Benoit
 */
export class FactoryCommand {
  private restrict: string;
  private templateUrl: string;
  private replace: boolean;
  private controller: string;
  private controllerAs: string;
  private bindToController: boolean;

  private scope: {
    [propName: string]: string;
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';

    this.templateUrl = 'app/factories/create-factory/command/factory-command.html';
    this.replace = false;

    this.controller = 'FactoryCommandController';
    this.controllerAs = 'factoryCommandCtrl';

    this.bindToController = true;

    // scope values
    this.scope = {
      factoryObject: '=cdvyFactoryObject',
      onChange: '&cdvyOnChange'
    };
  }
}
