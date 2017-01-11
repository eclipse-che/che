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
package org.eclipse.che.plugin.jsonexample.ide.editor;


import org.eclipse.che.ide.api.editor.codeassist.Completion;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.LinearRange;

public class SimpleCompletion implements Completion {

    private final String proposal;

    public SimpleCompletion(String proposal) {
        this.proposal = proposal;
    }

    @Override
    public void apply(Document document) {
        document.replace(
                document.getCursorOffset(),
                proposal.length(),
                proposal);
    }

    @Override
    public LinearRange getSelection(Document document) {
        return null;
    }
}
