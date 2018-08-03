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
package org.eclipse.che.plugin.github.ide;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_EDITOR_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.github.ide.action.OpenOnGitHubAction;
import org.eclipse.che.plugin.ssh.key.client.SshKeyUploaderRegistry;

/**
 * Extension adds GitHub support to the IDE Application.
 *
 * @author Andrey Plotnikov
 */
@Singleton
@Extension(title = "GitHub", version = "3.0.0")
public class GitHubExtension {

  public static final String GITHUB_HOST = "github.com";

  @Inject
  public GitHubExtension(
      SshKeyUploaderRegistry registry,
      GitHubSshKeyUploader gitHubSshKeyProvider,
      ActionManager actionManager,
      OpenOnGitHubAction openOnGitHubAction) {

    registry.registerUploader(GITHUB_HOST, gitHubSshKeyProvider);
    actionManager.registerAction("openOnGitHub", openOnGitHubAction);
    DefaultActionGroup mainContextMenuGroup =
        (DefaultActionGroup) actionManager.getAction(GROUP_MAIN_CONTEXT_MENU);
    mainContextMenuGroup.add(openOnGitHubAction);
    DefaultActionGroup editorContextMenuGroup =
        (DefaultActionGroup) actionManager.getAction(GROUP_EDITOR_CONTEXT_MENU);
    editorContextMenuGroup.add(openOnGitHubAction);
  }
}
