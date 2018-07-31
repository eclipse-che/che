/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';


/**
 * This class is handling the controller for the footer.
 *
 * @author Ann Shumilova
 */
export class CheFooterController {

  /**
   * Returns 'Make a wish' email subject.
   *
   * @param productName
   * @returns {string}
   */
  getWishEmailSubject(productName: string): string {
    return this.getEmailSubject('Wishes for ' + productName);
  }

  /**
   * Returns formed subject for email.
   *
   * @param subject
   * @returns {string}
   */
  getEmailSubject(subject: string): string {
    return '?subject=' + encodeURIComponent(subject);
  }
}


