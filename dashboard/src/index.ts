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
export interface ICheWindow extends Window {
  $: Function;
  jQuery: Function;
  Monaco: any;
  yamlLanguageServer: any;
  monacoConversion: any;
  MonacoEnvironment: any;
  url: any;
  jsyaml: Object;
  jsonlint?: Object;
}

declare const require: Function;
/* tslint:disable */
const windowObject = <ICheWindow>window;
const $ = require('jquery');
windowObject.$ = $;
windowObject.jQuery = $;
windowObject.jsyaml = require('js-yaml');
if (windowObject.jsonlint === undefined) {
  windowObject.jsonlint = require('jsonlint');
}
windowObject.monacoConversion = require('monaco-languageclient/lib/monaco-converter');
windowObject.url = require('url');
windowObject.Monaco = require('monaco-editor-core/esm/vs/editor/editor.main');

/* tslint:enable */
import './monaco-env-setup';
import './monaco-languages-setup';
import 'angular';
import 'angular-animate';
import 'angular-cookies';
import 'angular-file-upload';
import 'angular-touch';
import 'angular-sanitize';
import 'angular-resource';
import 'angular-route';
import 'angular-ui-bootstrap';
import 'angular-aria';
import 'angular-material';
import 'angular-messages';
import 'angular-moment';
import 'angular-filter';
import 'angular-uuid4';
import 'ng-lodash';
import '../node_modules/angular-gravatar/build/md5.min.js';
import '../node_modules/angular-gravatar/build/angular-gravatar.min.js';
import '../node_modules/angular-websocket/dist/angular-websocket.min.js';

// include UD app
import './app/index.module';

