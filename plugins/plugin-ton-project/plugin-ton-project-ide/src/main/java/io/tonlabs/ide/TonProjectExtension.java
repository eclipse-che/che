/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package io.tonlabs.ide;

import static io.tonlabs.shared.Constants.TON_CATEGORY;

import com.google.inject.Inject;
import io.tonlabs.ide.action.DeployToTestNetAction;
import io.tonlabs.ide.action.RunOnLocalNodeAction;
import io.tonlabs.ide.action.TonProjectAction;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;

/** TON Project extension that registers actions and icons. */
@Extension(title = "TON Project Extension", version = "0.0.1")
public class TonProjectExtension {

  /**
   * Constructor.
   *
   * @param actionManager the {@link ActionManager} that is used to register our actions
   * @param runOnLocalNodeAction action to be registered
   * @param tonProjectResources the resources that contains our icon
   * @param iconRegistry the {@link IconRegistry} that is used to register our icon
   */
  @Inject
  public TonProjectExtension(
      ActionManager actionManager,
      RunOnLocalNodeAction runOnLocalNodeAction,
      DeployToTestNetAction deployToTestNetAction,
      TonProjectResources tonProjectResources,
      IconRegistry iconRegistry) {

    DefaultActionGroup tonGroup = new DefaultActionGroup("TON", true, actionManager);

    actionManager.registerAction("tonProject", tonGroup);
    addAction(actionManager, tonGroup, runOnLocalNodeAction, "runOnLocalNodeAction");
    addAction(actionManager, tonGroup, deployToTestNetAction, "deployToTestNetAction");

    iconRegistry.registerIcon(
        new Icon(TON_CATEGORY + ".samples.category.icon", tonProjectResources.tonIcon()));

    DefaultActionGroup mainContextMenuGroup =
        (DefaultActionGroup) actionManager.getAction("resourceOperation");
    mainContextMenuGroup.add(tonGroup);

    DefaultActionGroup mainMenuGroup =
        (DefaultActionGroup) actionManager.getAction(IdeActions.GROUP_MAIN_MENU);
    mainMenuGroup.add(tonGroup, new Constraints(Anchor.AFTER, IdeActions.GROUP_RUN));
  }

  private static void addAction(
      ActionManager actionManager, DefaultActionGroup group, TonProjectAction action, String name) {

    actionManager.registerAction(name, action);
    group.add(action);
  }
}
