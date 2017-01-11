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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.plugin.jsonexample.ide.JsonExampleResources;

/**
 * A very simple implementation of a {@link CompletionProposal} that basically
 * only contains a string.
 */
public class SimpleCompletionProposal implements CompletionProposal {

    private String proposal;

    /**
     * Constructor.
     *
     * @param proposal
     *         the actual proposal
     */
    public SimpleCompletionProposal(String proposal) {
        this.proposal = proposal;
    }

    @Override
    public void getAdditionalProposalInfo(AsyncCallback<Widget> callback) {
        callback.onSuccess(null);
    }

    @Override
    public String getDisplayString() {
        return proposal;
    }

    @Override
    public Icon getIcon() {
        return new Icon("", JsonExampleResources.INSTANCE.completion());
    }

    @Override
    public void getCompletion(CompletionCallback callback) {
        callback.onCompletion(new SimpleCompletion(proposal));
    }
}
