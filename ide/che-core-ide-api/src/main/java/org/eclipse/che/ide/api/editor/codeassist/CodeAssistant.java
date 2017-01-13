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


/**
 * An <code>CodeAssistant</code> provides support on interactive content completion.
 * <p>
 * A content assistant has a list of {@link CodeAssistProcessor} objects each of which is
 * registered for a particular document content type. The content assistant uses the processors to react on the request of completing
 * documents or presenting context information.
 * </p>
 */
public interface CodeAssistant {

    /**
     * Returns the code assist processor to be used for the given content type.
     *
     * @param contentType the type of the content for which this content assistant is to be requested
     * @return an instance code assist processor or <code>null</code> if none exists for the specified content type
     */
    CodeAssistProcessor getCodeAssistProcessor(String contentType);

    /**
     * Registers a content processor for the given content type.
     *
     * @param contentType the content type
     * @param processor the processor
     */
    void setCodeAssistantProcessor(String contentType, CodeAssistProcessor processor);

    /**
     * Returns the code assist processor for the content type of the specified document position.
     * 
     * @param offset a offset within the document
     * @return a code-assist processor or <code>null</code> if none exists
     */
    CodeAssistProcessor getProcessor(int offset);

    /**
     * Computes completion proposals computed based on the specified document position. The position is used to determine the
     * appropriate content assist processor to invoke.
     * 
     * @param offset a document offset
     * @param triggered if triggered by the content assist key binding
     * @param callback the callback to use once completions are ready
     */
    void computeCompletionProposals(int offset, boolean triggered, CodeAssistCallback callback);

}
