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

const LANGUAGE_ID = 'yaml';
const MODEL_URI = 'inmemory://model.yaml';
const yamlService = (window as any).yamlService;
const m2p = new (window as any).monacoConversion.MonacoToProtocolConverter();
const p2m = new (window as any).monacoConversion.ProtocolToMonacoConverter();

function createDocument(model) {
    return (window as any).yamlLanguageServer.TextDocument.create(
        MODEL_URI,
        model.getModeId(),
        model.getVersionId(),
        model.getValue()
    );
}

function registerYAMLCompletion() {
    monaco.languages.registerCompletionItemProvider(LANGUAGE_ID, {
        provideCompletionItems(model, position) {
            const document = createDocument(model);
            return yamlService
                .doComplete(document, m2p.asPosition(position.lineNumber, position.column), true)
                .then(list => p2m.asCompletionResult(list));
        },

        resolveCompletionItem(item) {
        return yamlService
            .doResolve(m2p.asCompletionItem(item))
            .then(result => p2m.asCompletionItem(result));
        },
    });
}

function registerYAMLDocumentSymbols() {
    monaco.languages.registerDocumentSymbolProvider(LANGUAGE_ID, {
        provideDocumentSymbols(model) {
        const document = createDocument(model);
        return p2m.asSymbolInformations(yamlService.findDocumentSymbols(document));
        },
    });
}

function registerYAMLHover() {
    monaco.languages.registerHoverProvider(LANGUAGE_ID, {
        provideHover(model, position) {
        const doc = createDocument(model);
        return yamlService
            .doHover(doc, m2p.asPosition(position.lineNumber, position.column))
            .then((hover) => p2m.asHover(hover));
        },
    });
}

registerYAMLCompletion();
registerYAMLDocumentSymbols();
registerYAMLHover();
