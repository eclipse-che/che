/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * This class is handling the controller for popover which includes add/import project widget.
 *
 * @author Oleksii Kurinnyi
 */
export class AddProjectPopoverController {

  /**
   * Popover is open when <code>true</code>.
   */
  private isOpen: boolean;
  /**
   * Callback to check uniqueness of project name.
   * Provided by parent controller.
   */
  /* tslint:disable */
  private isProjectNameUnique: (data: {name: string}) => boolean;
  /* tslint:enable */
  /**
   * Callback which is called when project templates are added to the list of ready-to-import projects.
   * Provided by parent controller.
   */
  private projectOnAdd: (data: {templates: Array<che.IProjectTemplate>}) => void;

  /**
   * Default constructor that is using resource injection
   */
  constructor() {
    this.isOpen = false;
  }

  /**
   * Callback which should be called when project templates are added.
   *
   * @param {Array<che.IProjectTemplate>} projectTemplates
   */
  projectTemplateOnAdd(projectTemplates: Array<che.IProjectTemplate>): void {
    this.projectOnAdd({templates: projectTemplates});

    // close popover
    this.isOpen = false;
  }

}
