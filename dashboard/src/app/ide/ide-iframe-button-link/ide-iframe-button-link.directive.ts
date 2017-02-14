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
 * Defines a directive for displaying iframe for displaying the IDE.
 * @author Florent Benoit
 */
class IdeIframeButtonLink {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict='E';
    this.templateUrl = 'app/ide/ide-iframe-button-link/ide-iframe-button-link.html';


    this.controller = 'IdeIFrameButtonLinkCtrl';
    this.controllerAs = 'ideIFrameButtonLinkCtrl';
    this.bindToController = true;
  }

}

export default IdeIframeButtonLink;

