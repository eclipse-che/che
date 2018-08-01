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
package org.eclipse.che.plugin.pullrequest.client;

import static org.eclipse.che.ide.api.action.IdeActions.TOOL_WINDOWS_GROUP;
import static org.eclipse.che.ide.api.constraints.Anchor.BEFORE;
import static org.eclipse.che.ide.core.StandardComponentInitializer.EDITOR_DISPLAYING_MODE;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.plugin.pullrequest.client.actions.ContributePartDisplayingModeAction;

/**
 * Registers event handlers for adding/removing contribution part.
 *
 * <p>Manages {@code AppContext#getRootProject} current root project state, in the case of adding
 * and removing 'contribution' mixin. Contribution mixin itself is 'synthetic' one and needed only
 * for managing plugin specific project attributes.
 *
 * @author Stephane Tournie
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
@Singleton
@Extension(title = "Contributor", version = "1.0.0")
public class ContributionExtension {
  public static final String CONTRIBUTE_PART_DISPLAYING_MODE = "contributePartDisplayingMode";

  @Inject
  @SuppressWarnings("unused")
  public ContributionExtension(
      ContributeResources resources,
      ContributionMixinProvider contributionMixinProvider,
      KeyBindingAgent keyBinding,
      ActionManager actionManager,
      ContributePartDisplayingModeAction contributePartDisplayingModeAction) {
    resources.contributeCss().ensureInjected();

    actionManager.registerAction(
        CONTRIBUTE_PART_DISPLAYING_MODE, contributePartDisplayingModeAction);

    DefaultActionGroup toolWindowGroup =
        (DefaultActionGroup) actionManager.getAction(TOOL_WINDOWS_GROUP);
    toolWindowGroup.add(
        contributePartDisplayingModeAction, new Constraints(BEFORE, EDITOR_DISPLAYING_MODE));

    if (UserAgent.isMac()) {
      keyBinding
          .getGlobal()
          .addKey(
              new KeyBuilder().action().control().charCode('6').build(),
              CONTRIBUTE_PART_DISPLAYING_MODE);
    } else {
      keyBinding
          .getGlobal()
          .addKey(
              new KeyBuilder().action().alt().charCode('6').build(),
              CONTRIBUTE_PART_DISPLAYING_MODE);
    }
  }
}
