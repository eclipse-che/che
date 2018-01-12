/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
