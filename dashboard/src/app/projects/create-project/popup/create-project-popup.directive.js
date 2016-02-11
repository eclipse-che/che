/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
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
 * Defines a directive for displaying popup about creating project.
 * @author Florent Benoit
 */
export class CreateProjectPopup {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict='E';
    this.templateUrl = 'app/projects/create-project/popup/create-project-popup.html';


    this.controller = 'CreateProjectPopupCtrl';
    this.controllerAs = 'createProjectPopupCtrl';
    this.bindToController = true;
  }

}
