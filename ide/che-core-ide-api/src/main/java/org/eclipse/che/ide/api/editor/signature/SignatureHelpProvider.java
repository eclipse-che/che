/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.signature;

import com.google.common.base.Optional;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/**
 * Calculates signature information at cursor position.
 *
 * @author Evgen Vidolob
 */
public interface SignatureHelpProvider {

  /**
   * Requests to provide signature information
   *
   * @param document the document where request called
   * @param offset the offset where request called
   * @return the promise.
   */
  @NotNull
  Promise<Optional<SignatureHelp>> signatureHelp(Document document, int offset);

  /** Installs the SignatureHelpProvider on the given text view. */
  void install(TextEditor editor);

  /** Removes the SignatureHelpProvider from the text view it has previously been installed on. */
  void uninstall();
}
