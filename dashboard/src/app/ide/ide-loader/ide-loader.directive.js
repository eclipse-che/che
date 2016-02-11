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
 * Defines a directive for displaying full screen loader displayed to load the IDE
 * @author Florent Benoit
 */
class IdeLoader {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict='E';
    this.templateUrl = 'app/ide/ide-loader/ide-loader.html';


    this.controller = 'IdeLoaderCtrl';
    this.controllerAs = 'ideLoaderCtrl';
    this.bindToController = true;
  }

}

export default IdeLoader;

