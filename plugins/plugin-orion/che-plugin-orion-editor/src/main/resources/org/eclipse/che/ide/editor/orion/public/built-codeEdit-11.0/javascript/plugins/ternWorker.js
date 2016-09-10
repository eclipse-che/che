/*******************************************************************************
 * @license
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*eslint-env node, browser*/
/*globals importScripts */
importScripts('../../requirejs/require.min.js'); // synchronous //$NON-NLS-1$
require(["../../orion/require-config.js"], function(config){
	require.config({
		baseUrl: "../../"
	});
	require(["javascript/plugins/ternWorkerCore"], null, config.errback);
});
