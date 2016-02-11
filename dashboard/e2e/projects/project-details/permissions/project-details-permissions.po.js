/*******************************************************************************
 * Copyright (c) 2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

'use strict';

/**
 * This file uses the Page Object pattern to define the main page for tests
 * https://docs.google.com/presentation/d/1B6manhG0zEXkC-H-tPo2vwU06JhL8w9-XCF9oehXzAQ
 * @author Oleksii Orel
 */

var MainPage = function () {

  this.accessTab = element(by.cssContainingText('md-tab-item span', 'Access'));

  this.permissionsElement = element.all(by.repeater('permission in projectDetailsDevelopersCtrl.permissions'));

};

module.exports = new MainPage();
