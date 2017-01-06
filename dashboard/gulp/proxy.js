/*******************************************************************************
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

'use strict';
var minimist = require('minimist');
var url = require('url');
var proxy = require('proxy-middleware');


var serverOptions = {
  string: 'server',
  default: {server: 'http://localhost:8080'}
};

var options = minimist(process.argv.slice(2), serverOptions);

var patterns = ['/api', '/ext', '/ws', '/datasource', '/java-ca', '/im', '/che', '/admin'];

var proxies = []


patterns.forEach(function(pattern) {
  var proxyOptions = url.parse(options.server + pattern);
  if (pattern === '/im') {
    proxyOptions.route = '/im';
  } else if (pattern === '/che') {
    proxyOptions.route = '/che';
  } else if (pattern === '/admin') {
    proxyOptions.route = '/admin';
  } else if (pattern === '/ext') {
    proxyOptions.route = '/ext';
  } else {
    proxyOptions.route = '/api';
  }
  proxyOptions.preserveHost = false;
  proxies.push(proxy(proxyOptions));

});

console.log('Using remote Che server', options.server);

/*
 * Enable proxy
 */

module.exports = proxies;
