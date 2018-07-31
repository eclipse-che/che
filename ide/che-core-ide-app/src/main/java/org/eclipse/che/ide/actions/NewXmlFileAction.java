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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * Action to create new XML file.
 *
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class NewXmlFileAction extends AbstractNewResourceAction {
  private static final String DEFAULT_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

  @Inject
  public NewXmlFileAction(
      CoreLocalizationConstant localizationConstant,
      Resources resources,
      DialogFactory dialogFactory,
      CoreLocalizationConstant coreLocalizationConstant,
      EventBus eventBus,
      AppContext appContext,
      NotificationManager notificationManager,
      Provider<EditorAgent> editorAgentProvider) {
    super(
        localizationConstant.actionNewXmlFileTitle(),
        localizationConstant.actionNewXmlFileDescription(),
        resources.defaultFile(),
        dialogFactory,
        coreLocalizationConstant,
        eventBus,
        appContext,
        notificationManager,
        editorAgentProvider);
  }

  @Override
  protected String getExtension() {
    return "xml";
  }

  @Override
  protected String getDefaultContent() {
    return DEFAULT_CONTENT;
  }
}
