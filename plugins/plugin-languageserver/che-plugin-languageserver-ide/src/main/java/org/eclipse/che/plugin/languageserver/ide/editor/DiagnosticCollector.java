/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide.editor;


import org.eclipse.che.api.languageserver.shared.lsapi.DiagnosticDTO;

/**
 * @author Evgen Vidolob
 */
public interface DiagnosticCollector {
    /**
     * Notification of a diagnostic.
     *
     * @param diagnostic
     *         Diagnostic - The discovered diagnostic.
     */
    void acceptDiagnostic(DiagnosticDTO diagnostic);

    /**
     * Notification sent before starting the diagnostic process.
     * Typically, this would tell a diagnostic collector to clear previously recorded diagnostic.
     */
    void beginReporting();

    /**
     * Notification sent after having completed diagnostic process.
     * Typically, this would tell a diagnostic collector that no more diagnostics should be expected in this
     * iteration.
     */
    void endReporting();
}
