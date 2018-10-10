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
  CodeMirror: Function;
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
windowObject.CodeMirror = require('codemirror');
if (windowObject.jsonlint === undefined) {
  windowObject.jsonlint = require('jsonlint');
}
/* tslint:enable */

import 'codemirror/mode/javascript/javascript';
import 'codemirror/mode/yaml/yaml';
import 'codemirror/mode/dockerfile/dockerfile';
import 'codemirror/addon/lint/lint';
import 'codemirror/addon/lint/json-lint';
import 'codemirror/addon/edit/matchbrackets';
import 'codemirror/addon/edit/closebrackets';
import 'codemirror/addon/fold/foldcode';
import 'codemirror/addon/fold/foldgutter';
import 'codemirror/addon/fold/brace-fold';
import 'codemirror/addon/selection/active-line';
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
import 'angular-websocket';
import 'ng-lodash';
import '../node_modules/angular-gravatar/build/md5.min.js';
import '../node_modules/angular-gravatar/build/angular-gravatar.min.js';

// include UD app
import './app/index.module';
