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
 * This class is checking onBoarding state.
 * @author Oleksii Orel
 */
export class OnBoarding {

  /**
   * Default constructor for the artifact API.
   * @ngInject for Dependency injection
   */
  constructor(cheAPI) {
    this.cheUser = cheAPI.getUser();
    this.profile = cheAPI.getProfile().getProfile();
    this.preferences = cheAPI.getProfile().getPreferences();
  }

  isUserOnBoarding() {
    // if admin
    if (this.cheUser.isAdmin()) {
      return false;
    }

    let property = this.preferences.onBoardingFlowCompleted;

    return !property || property !== 'true';
  }

}

