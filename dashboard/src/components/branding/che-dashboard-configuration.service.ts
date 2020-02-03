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

import { CheBranding } from './che-branding.factory';

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

  get ready(): ng.IPromise<void> {
    return this.cheBranding.ready;
  }

  allowedMenuItem(menuItem: che.ConfigurableMenuItem | string): boolean {
    const disabledItems = this.cheBranding.getConfiguration().menu.disabled;
    return (disabledItems as string[]).indexOf(menuItem) === -1;
  }

  allowRoutes(menuItem: che.ConfigurableMenuItem): ng.IPromise<void> {
    return this.cheBranding.ready.then(() => {
      if (this.allowedMenuItem(menuItem) === false) {
        return this.$q.reject();
      }
    });
  }

  enabledFeature(feature: che.TogglableFeature): boolean {
    const disabledFeatures = this.cheBranding.getConfiguration().features.disabled;
    return disabledFeatures.indexOf(feature) === -1;
  }

}
