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
package org.eclipse.che.plugin.pullrequest.client.parts.contribute;

/**
 * Used to detect if pull request title/comment/branch is changed.
 *
 * @author Yevhenii Voevodin
 * @see ContributePartView#addBranchChangedHandler(TextChangedHandler)
 * @see ContributePartView#addContributionCommentChangedHandler(TextChangedHandler)
 * @see ContributePartView#addContributionTitleChangedHandler(TextChangedHandler)
 */
public interface TextChangedHandler {

  /**
   * Called when title/comment/branch is changed
   *
   * @param newText new text content
   */
  void onTextChanged(String newText);
}
