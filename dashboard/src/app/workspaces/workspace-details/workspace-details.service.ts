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
 * This class is handling the data for workspace details sections (tabs)
 *
 * @author Ann Shumilova
 */
export class WorkspaceDetailsService {

    /**
     * Default constructor that is using resource
     * @ngInject for Dependency injection
     */
    constructor () {
      this.sections = [];
    }

  /**
   * Add new section to the workspace details.
   *
   * @param title section title
   * @param content section html content
   * @param icon section icon
   * @param index optional section index (order)
   */
    addSection(title, content, icon, index) {
      let section = {};
      section.title = title;
      section.content = content;
      section.icon = icon;
      section.index = index || this.sections.length;
      this.sections.push(section);
    }

  /**
   * Returns workspace details sections.
   *
   * @returns {Array} array of sections
   */
    getSections() {
      return this.sections;
    }
}
