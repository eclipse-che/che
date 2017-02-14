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
package org.eclipse.che.ide.api.editor.codeassist;

import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal.CompletionCallback;

/**
 * Extends {@link CompletionProposal} with the following
 * function:
 * <ul>
 * <li>Allow computation of the Completion with replacing or inserting text .</li>
 * </ul>
 *
 * @author Evgen Vidolob
 */
public interface CompletionProposalExtension {

    void getCompletion(boolean insert, CompletionCallback callback);
}
