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

import { CheDashboardConfigurationService, FooterLink } from '../../branding/che-dashboard-configuration.service';

/**
 * This class is handling the controller for the footer.
 *
 * @author Ann Shumilova
 */
export class CheFooterController {

  static $inject = [
    'cheDashboardConfigurationService',
  ];

  links: { [key: string]: FooterLink };

  private cheDashboardConfigurationService: CheDashboardConfigurationService;

  constructor(
    cheDashboardConfigurationService: CheDashboardConfigurationService,
  ) {
    this.cheDashboardConfigurationService = cheDashboardConfigurationService;
  }

  $onInit(): void {
    this.links = this.cheDashboardConfigurationService.getFooterLinks();
  }

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


