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

import { CheDashboardConfigurationService } from '../../components/branding/che-dashboard-configuration.service';

export class GetStartedConfigService {

  static $inject = [
    'cheDashboardConfigurationService'
  ];

  private cheDashboardConfigurationService: CheDashboardConfigurationService;

  constructor(
    cheDashboardConfigurationService: CheDashboardConfigurationService,
  ) {
    this.cheDashboardConfigurationService = cheDashboardConfigurationService;
  }

  allowGetStartedRoutes(): ng.IPromise<void> {
    return this.cheDashboardConfigurationService.allowRoutes('getstarted');
  }

}
