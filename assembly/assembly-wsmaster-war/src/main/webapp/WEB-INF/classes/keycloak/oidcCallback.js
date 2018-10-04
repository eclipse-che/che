/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

function redirectToInitialPage(ide) {
  var redirectUri = window.sessionStorage.getItem('oidc' + (ide ? 'Ide' : 'Dashboard') + 'RedirectUrl');
  var fragmentIndex = redirectUri.indexOf('#');
  if (location.hash) {
    var keycloakParameters;
    if (fragmentIndex == -1) {
      keycloakParameters = location.hash;
    } else {
      keycloakParameters = '&' + location.hash.substring(1);
    }
    redirectUri += keycloakParameters;
  }
  window.location = redirectUri;
}
