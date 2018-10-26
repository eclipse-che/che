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
package org.eclipse.che.plugin.product.info.client;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_HELP;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;

/** @author Vitalii Parfonov */
@Extension(title = "Che Product Information")
public class CheProductExtension {

  private RedirectToIssueTrackerAction toIssueTrackerAction;
  private RedirectToPublicChatAction toPublicChatAction;

  @Inject
  public CheProductExtension(
      RedirectToIssueTrackerAction toIssueTrackerAction,
      RedirectToPublicChatAction toPublicChatAction) {
    this.toIssueTrackerAction = toIssueTrackerAction;
    this.toPublicChatAction = toPublicChatAction;
  }

  @Inject
  private void prepareActions(ActionManager actionManager) {

    DefaultActionGroup helpGroup = (DefaultActionGroup) actionManager.getAction(GROUP_HELP);

    actionManager.registerAction("toPublicChatAction", toPublicChatAction);
    helpGroup.add(toPublicChatAction);
    actionManager.registerAction("toIssueTrackerAction", toIssueTrackerAction);
    helpGroup.add(toIssueTrackerAction);
  }
}
