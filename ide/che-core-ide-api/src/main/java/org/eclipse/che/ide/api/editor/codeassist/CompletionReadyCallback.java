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
package org.eclipse.che.ide.api.editor.codeassist;

import java.util.List;

/** Callback used to be called when the completion proposals are computed. */
public interface CompletionReadyCallback {
  /**
   * Callback used to be called when the completion proposals are computed.
   *
   * @param proposals the proposals
   */
  void onCompletionReady(List<CompletionProposal> proposals);
}
