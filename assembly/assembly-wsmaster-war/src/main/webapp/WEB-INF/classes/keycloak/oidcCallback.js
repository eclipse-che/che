/*
    Copyright (c) 2015-2017 Red Hat, Inc.
    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html
    Contributors:
      Red Hat, Inc. - initial API and implementation
*/

function redirectToInitialPage(ide) {
  var redirectUri = window.sessionStorage.getItem('oidc' + (ide ? 'Ide' : 'Dashboard') + 'RedirectUrl');
  console.log('redirectUri in oidcCallback.html: ' + redirectUri);
  var fragmentIndex = redirectUri.indexOf('#');
  debugger;
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
