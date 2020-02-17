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

import { TogglableFeature } from './branding.constant';
import { CheBranding } from './che-branding';

export type FooterLink = {
  title: string;
  reference: string;
};

/**
 * This class handles configuration data of Dashboard.
 * @author Oleksii Kurinnyi
 */
export class CheDashboardConfigurationService {

  static $inject = [
    '$q',
    'cheBranding',
  ];

  $q: ng.IQService;
  cheBranding: CheBranding;

  constructor(
    $q: ng.IQService,
    cheBranding: CheBranding,
  ) {
    this.$q = $q;
    this.cheBranding = cheBranding;
  }

  allowedMenuItem(menuItem: che.ConfigurableMenuItem | string): boolean {
    const disabledItems = this.cheBranding.getConfiguration().menu.disabled;
    return (disabledItems as string[]).indexOf(menuItem) === -1;
  }

  allowRoutes(menuItem: che.ConfigurableMenuItem): ng.IPromise<void> {
    const defer = this.$q.defer<void>();
      if (this.allowedMenuItem(menuItem)) {
        defer.resolve();
      } else {
        defer.reject();
      }

    return defer.promise;
  }

  enabledFeature(feature: TogglableFeature): boolean {
    const disabledFeatures = this.cheBranding.getConfiguration().features.disabled;
    return disabledFeatures.indexOf(feature) === -1;
  }

  getFooterLinks(): { [key: string]: FooterLink } {
    const links: { [key: string]: FooterLink } = {};
    if (this.cheBranding.getProductSupportEmail()) {
      links.supportEmail = {
        title: 'Make a wish',
        reference: this.cheBranding.getProductSupportEmail()
      };
    }
    if (this.cheBranding.getFooter().email) {
      const email = this.cheBranding.getFooter().email;
      links.email = {
        title: email.title,
        reference: email.address
      };
    }
    if (this.cheBranding.getDocs().general) {
      links.docs = {
        title: 'Docs',
        reference: this.cheBranding.getDocs().general
      };
    }
    if (this.cheBranding.getDocs().faq) {
      links.faq = {
        title: 'FAQ',
        reference: this.cheBranding.getDocs().faq
      };
    }
    if (this.cheBranding.getProductHelpPath() && this.cheBranding.getProductHelpTitle()) {
      links.supportPath = {
        title: this.cheBranding.getProductHelpTitle(),
        reference: this.cheBranding.getProductHelpPath()
      };
    }
    return links;
  }

}
