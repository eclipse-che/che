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


