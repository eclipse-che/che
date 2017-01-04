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
 * This class is handling the controller for the demo of components
 * @author Florent Benoit
 */
export class DemoComponentsCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {

    // selecter
    this.booksByAuthor = {};
    this.booksByAuthor['St Exupery'] = [{title:'The little prince'}];
    this.booksByAuthor['V. Hugo'] = [{title:'Les miserables'}, {title:'The Hunchback of Notre-Dame'}];
    this.booksByAuthor['A. Dumas'] = [{title:'The count of Monte Cristo'}, {title:'The Three Musketeers'}];


    this.button2Disabled = true;

    this.listItemsDocs = ['Document1', 'Document2', 'Document3', 'Document4', 'Document5'];

    this.listItemsTasks = [{name : 'Task 1', done: false}, {name : 'Task 2', done: true}, {name : 'Task 3', done: false},
      {name : 'Task 4', done: true}, {name : 'Task 5', done: false}];
  }


  toggleDisabled2() {
    this.button2Disabled = !this.button2Disabled;
  }

  isToggleDisabled2() {
    return this.button2Disabled;
  }


}
