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

import { GlobalWarningBannerService } from './global-warning-banner.service';
import { CheWorkspace } from '../api/workspace/che-workspace.factory';
import { CheBranding } from '../branding/che-branding';

/**
 * This service does HEAD requests to plugin and devfile registries. In case if at least one of requests fails, a banner with a warning message will be show.
 * @author Oleksii Kurinnyi
 */
export class RegistryCheckingService {

  static $inject = [
    '$http',
    '$q',
    'cheBranding',
    'cheWorkspace',
    'globalWarningBannerService',
  ];

  private $http: ng.IHttpService;
  private $q: ng.IQService;
  private cheWorkspace: CheWorkspace;
  private globalWarningBannerService: GlobalWarningBannerService;
  private cheBranding: CheBranding;

  private headers: { [name: string]: string; };

  constructor(
    $http: ng.IHttpService,
    $q: ng.IQService,
    cheBranding: CheBranding,
    cheWorkspace: CheWorkspace,
    globalWarningBannerService: GlobalWarningBannerService,
  ) {
    this.$http = $http;
    this.$q = $q;
    this.cheBranding = cheBranding;
    this.cheWorkspace = cheWorkspace;
    this.globalWarningBannerService = globalWarningBannerService;

    this.headers = { 'Authorization': undefined };

    const REGISTRY_CERTIFICATE_ERROR = `Unable to load plugins and/or devfiles. Your ${this.cheBranding.getName()} may be using a self-signed certificate. To resolve this issue, try to import the servers CA certificate into your browser, as described in <a href="${this.cheBranding.getDocs().certificate}" target="_blank">docs</a>. After importing the certificate, refresh the page.`;

    this.cheWorkspace.fetchWorkspaceSettings()
      .then(settings => [
        `${settings.cheWorkspaceDevfileRegistryUrl}/devfiles/index.json`,
        `${settings.cheWorkspacePluginRegistryUrl}/plugins/`
      ])
      .then(locations => this.$q.all(
        locations.map(location => this.checkPossibleCertificateIssue(location))
      ))
      .catch(() => this.globalWarningBannerService.addMessage(REGISTRY_CERTIFICATE_ERROR));
  }

  private checkPossibleCertificateIssue(location: string): ng.IPromise<void> {
    const deferred = this.$q.defer<void>();
    this.$http({
      'method': 'HEAD',
      'url': location,
      'headers': this.headers
    })
      .then(() => deferred.resolve())
      .catch(error => {
        if (error.status === -1) {
          deferred.reject();
        } else {
          deferred.resolve();
        }
      });
    return deferred.promise;
  }

}
