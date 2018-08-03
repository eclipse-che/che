/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * This class is handling the controller for adding the new package.
 *
 * @author Ann Shumilova
 */
export class AddPackagePopoverController {
  private onAddPackage: Function;
  private isOpen: boolean;
  private packageName: string;
  private packageLocation: string;

  constructor() {
    this.isOpen = false;
    this.packageName = '';
    this.packageLocation = '';
  }

  /**
   * Adds package to be considered.
   */
  addPackage(): void {
    this.onAddPackage({ 'name': this.packageName, 'location': this.packageLocation});
    this.close();
  }

  /**
   * Closes the popover.
   */
  close(): void {
    this.isOpen = false;
    this.packageName = '';
    this.packageLocation = '';
  }
}
