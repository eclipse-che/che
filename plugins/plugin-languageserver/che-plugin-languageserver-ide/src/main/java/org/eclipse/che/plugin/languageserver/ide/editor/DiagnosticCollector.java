/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.editor;

import org.eclipse.lsp4j.Diagnostic;

/** @author Evgen Vidolob */
public interface DiagnosticCollector {
  /**
   * Notification of a diagnostic.
   *
   * @param diagnosticsCollection an identifier for the set of diagnostics the reported diagnostic
   *     belongs to.
   * @param diagnostic Diagnostic - The discovered diagnostic.
   */
  void acceptDiagnostic(String diagnosticsCollection, Diagnostic diagnostic);

  /**
   * Notification sent before starting the reporting of a collection of diagnostics Typically, this
   * would tell a diagnostic collector to clear previously recorded diagnostics in the same
   * collections.
   *
   * @param diagnosticsCollection the diagnostic collection to start reporting for
   */
  void beginReporting(String diagnosticsCollection);

  /**
   * Notification sent after having completed diagnostic process. Typically, this would tell a
   * diagnostic collector that no more diagnostics should be expected in this collection.
   *
   * @param diagnosticsCollection the diagnostic collection to end reporting for
   */
  void endReporting(String diagnosticsCollection);
}
