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
 * This service handles warning messages to show them in a banner.
 * @author Oleksii Kurinnyi
 */
export class GlobalWarningBannerService {

  static $inject = [
    '$rootScope',
  ];

  private $rootScope: che.IRootScopeService;

  private messages: string[];

  constructor(
    $rootScope: che.IRootScopeService,
  ) {
    this.$rootScope = $rootScope;

    this.$rootScope.globalWarningBannerHtml = '';
    this.$rootScope.showGlobalWarningBanner = false;
    this.messages = [];
  }

  addMessage(message: string): void {
    this.messages.push(message);

    this.updateBannerHtml();
    this.showBanner();
  }

  clearMessage(message: string): void {
    const index = this.messages.indexOf(message);
    if (index === -1) {
      return;
    }
    this.messages.splice(index, 1);

    this.updateBannerHtml();
    if (this.messages.length === 0) {
      this.hideBanner();
    }
  }

  clearAllMessages(): void {
    this.messages.length = 0;

    this.updateBannerHtml();
    this.hideBanner();
  }

  private updateBannerHtml(): void {
    this.$rootScope.globalWarningBannerHtml = this.messages
      .map(message => `<p>${message}</p>`)
      .join('');
  }

  private showBanner(): void {
    this.$rootScope.showGlobalWarningBanner = true;
  }

  private hideBanner(): void {
    this.$rootScope.showGlobalWarningBanner = false;
  }

}
