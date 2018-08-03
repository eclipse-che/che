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

declare const uiCodemirrorDirective: any;

export class CodeMirrorConstant {

  constructor(register: che.IRegisterService) {
    register.app.constant('udCodemirrorConfig', {
      codemirror: {
        lineWrapping: true,
        lineNumbers: true,
        mode: 'application/json',
        gutters: ['CodeMirror-lint-markers', 'CodeMirror-linenumbers', 'CodeMirror-foldgutter'],
        lint: true,
        matchBrackets: true,
        autoCloseBrackets: true,
        foldGutter: true,
        styleActiveLine: true,
        theme: 'che',
        onLoad: (editor: any) => {
          editor.refresh();
        }
      }
    }).config(() => {
      uiCodemirrorDirective.$inject = ['$timeout', 'udCodemirrorConfig']; // jshint ignore:line
    });
  }
}
