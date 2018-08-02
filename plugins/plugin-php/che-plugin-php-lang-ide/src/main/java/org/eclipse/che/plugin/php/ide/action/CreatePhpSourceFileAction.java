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
package org.eclipse.che.plugin.php.ide.action;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.plugin.php.ide.PhpLocalizationConstant;
import org.eclipse.che.plugin.php.ide.PhpResources;
import org.eclipse.che.plugin.php.shared.Constants;

/**
 * Action to create new PHP source file.
 *
 * @author Kaloyan Raev
 */
@Singleton
public class CreatePhpSourceFileAction extends NewPhplikeResourceAction {

  private static final String DEFAULT_CONTENT = "<?php\n" + "\n";

  @Inject
  public CreatePhpSourceFileAction(
      PhpLocalizationConstant localizationConstant,
      PhpResources resources,
      DialogFactory dialogFactory,
      CoreLocalizationConstant coreLocalizationConstant,
      EventBus eventBus,
      AppContext appContext,
      NotificationManager notificationManager,
      Provider<EditorAgent> editorAgentProvider) {
    super(
        localizationConstant.createPhpFileActionTitle(),
        localizationConstant.createPhpFileActionDescription(),
        resources.phpFile(),
        dialogFactory,
        coreLocalizationConstant,
        eventBus,
        appContext,
        notificationManager,
        editorAgentProvider);
  }

  @Override
  protected String getExtension() {
    return Constants.PHP_EXT;
  }

  @Override
  protected String getDefaultContent() {
    return DEFAULT_CONTENT;
  }
}
