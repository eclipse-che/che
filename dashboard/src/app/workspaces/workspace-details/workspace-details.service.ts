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

interface IPage {
  title: string;
  content: string;
  icon: string;
  index: number;
}

interface ISection {
 title: string;
 description: string;
 content: string;
}

/**
 * This class is handling the data for workspace details sections (tabs)
 *
 * @author Ann Shumilova
 */
export class WorkspaceDetailsService {
  pages: IPage[];
  sections: ISection[];

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.pages = [];
    this.sections = [];
  }

  /**
   * Add new page(tab) to the workspace details.
   *
   * @param title page title
   * @param content page html content
   * @param icon page icon
   * @param index optional page index (order)
   */
  addPage(title: string, content: string, icon: string, index: number): void {
    let page: IPage = {
      title: title,
      content: content,
      icon: icon,
      index: index || this.pages.length
    };
    this.pages.push(page);
  }

  /**
   * Adds new section in workspace details.
   *
   * @param title title of the section
   * @param description description of teh section (optional)
   * @param content html content of the section
   */
  addSection(title: string, description: string, content: string): void {
    let section: ISection = {
      title: title,
      description: description,
      content: content
    }

    this.sections.push(section);
  }

  /**
   * Returns workspace details pages(tabs).
   *
   * @returns {Array} array of pages(tabs)
   */
  getPages(): IPage[] {
    return this.pages;
  }

  /**
   * Returns the array of workspace details sections.
   *
   * @returns {ISection[]} list of sections
   */
  getSections(): ISection[] {
    return this.sections;
  }
}
