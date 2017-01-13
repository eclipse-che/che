/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.editor.signature;

import com.google.common.base.Optional;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

import javax.validation.constraints.NotNull;

/**
 * Calculates signature information at cursor position.
 *
 * @author Evgen Vidolob
 */
public interface SignatureHelpProvider {

    /**
     * Requests to provide signature information
     * @param document
     *              the document where request called
     * @param offset
     *              the offset where request called
     * @return the promise.
     */
    @NotNull
    Promise<Optional<SignatureHelp>> signatureHelp(Document document, int offset);

    /**
     * Installs the SignatureHelpProvider on the given text view.
     */
    void install(TextEditor editor);

    /**
     * Removes the SignatureHelpProvider from the text view it has previously been installed on.
     */
    void uninstall();
}
