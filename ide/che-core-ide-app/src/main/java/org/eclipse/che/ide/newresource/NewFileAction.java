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
package org.eclipse.che.ide.newresource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * Action to create new file.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class NewFileAction extends AbstractNewResourceAction {
  @Inject
  public NewFileAction(
      CoreLocalizationConstant localizationConstant,
      Resources resources,
      DialogFactory dialogFactory,
      EventBus eventBus,
      AppContext appContext,
      NotificationManager notificationManager,
      Provider<EditorAgent> editorAgentProvider) {
    super(
        localizationConstant.actionNewFileTitle(),
        localizationConstant.actionNewFileDescription(),
        resources.defaultFile(),
        dialogFactory,
        localizationConstant,
        eventBus,
        appContext,
        notificationManager,
        editorAgentProvider);
  }
}
