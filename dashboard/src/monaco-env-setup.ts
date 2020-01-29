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
declare var require: Function;

// tslint:disable-next-line:no-var-requires
(window as any).yamlLanguageServer = require('yaml-language-server');
var resolveSchema = function (url: string): Promise<string> {
    const promise = new Promise<string>((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.onload = () => resolve(xhr.responseText);
        xhr.onerror = () => reject(xhr.statusText);
        xhr.open('GET', url, true);
        xhr.send();
        console.log('resolving schema ' + url);
    });
    return promise;
  };

const workspaceContext = {
    resolveRelativePath: (relativePath, resource) => (window as any).url.resolve(resource, relativePath),
};

(window as any).yamlService = (window as any).yamlLanguageServer.getLanguageService(resolveSchema, workspaceContext, []);
// set up monaco initially
(window as any).MonacoEnvironment = {
    getWorkerUrl: function (moduleId, label) {
        return 'app/editor.worker.module.js';
    }
};

const monaco = (window as any).Monaco;
monaco.editor.defineTheme('che', {
    base: 'vs', // can also be vs-dark or hc-black
    inherit: true, // can also be false to completely replace the builtin rules
    rules: [
        { token: 'string.yaml', foreground: '000000' },
        { token: 'comment', foreground: '777777'}
    ],
    colors: {
        'editor.lineHighlightBackground': '#f0f0f0',
        'editorLineNumber.foreground': '#aaaaaa',
        'editorGutter.background': '#f8f8f8'
    }
});

monaco.editor.setTheme('che');

// tslint:disable-next-line: no-var-requires
const mod = require('monaco-languages/release/esm/yaml/yaml.js');

const languageID = 'yaml';

// register the YAML language with Monaco
monaco.languages.register({
    id: languageID,
    extensions: ['.yaml', '.yml'],
    aliases: ['YAML'],
    mimetypes: ['application/json']
});

monaco.languages.setMonarchTokensProvider(languageID, mod.language);
monaco.languages.setLanguageConfiguration(languageID, mod.conf);
