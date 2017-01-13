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
package org.eclipse.che.ide.api.editor.quickfix;

import elemental.dom.Element;

import org.eclipse.che.ide.api.editor.codeassist.CodeAssistCallback;

/**
 * An <code>QuickAssistAssistant</code> provides support for quick fixes and quick
 * assists.
 * Its purpose is to propose, display, and insert quick assists and quick fixes
 * available at the current source viewer's quick assist invocation context.
 */
public interface QuickAssistAssistant {

    /**
     * Shows all possible quick fixes and quick assists at the viewer's cursor position.
     *
     * @param line the line on which the assist was triggered
     * @param anchorElement element used for position
     */
    void showPossibleQuickAssists(int line, Element anchorElement);

    /**
     * Shows all possible quick fixes and quick assists at the viewer's cursor position.
     *
     * @param offset the offset on which the assist was triggered
     * @param coordX horizontal offset used for position
     * @param coordX vertical offset used for position
     */
    void showPossibleQuickAssists(int offset, float coordX, float coordY);

    /**
     * Compute the quick assist proposals.
     *
     * @param line the line on which the assist was triggered
     * @param callback called when computation is done
     */
    void computeQuickAssist(int line, CodeAssistCallback callback);

    /**
     * Registers a given quick assist processor for a particular content type. If there is already
     * a processor registered, the new processor is registered instead of the old one.
     *
     * @param processor
     *         the quick assist processor to register, or <code>null</code> to remove
     *         an existing one
     */
    void setQuickAssistProcessor(QuickAssistProcessor processor);

    /**
     * Returns the quick assist processor to be used for the given content type.
     *
     * @return the quick assist processor or <code>null</code> if none exists
     */
    QuickAssistProcessor getQuickAssistProcessor();
}
