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
 * This class is handling the controller for the item in navbar allowing to redirect to the IDE
 * @author Florent Benoit
 */
class IdeListItemNavbarCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(ideSvc, cheWorkspace) {
    this.ideSvc = ideSvc;
    this.cheWorkspace = cheWorkspace;
  }

  displayIDE() {
    this.ideSvc.restoreIDE();
  }

  isIDEAvailable() {
    return this.ideSvc.hasIdeLink();
  }

  /**
   * Check if last opened in IDE workspace is available
   */
  isWorkspaceAvailable() {
    return this.ideSvc.lastWorkspace != null && this.cheWorkspace.getWorkspaceById(this.ideSvc.lastWorkspace.id) != null;
  }
}


export default IdeListItemNavbarCtrl;
