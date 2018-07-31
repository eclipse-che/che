/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.codeassist;

import java.util.List;

/** Callback interface for code assistant requests. */
public interface CodeAssistCallback {
  /** Called when the completion proposals are computed. */
  void proposalComputed(List<CompletionProposal> proposals);
}
