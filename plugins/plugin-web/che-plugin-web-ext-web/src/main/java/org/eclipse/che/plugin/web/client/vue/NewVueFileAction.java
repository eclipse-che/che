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
package org.eclipse.che.plugin.web.client.vue;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.plugin.web.client.WebLocalizationConstant;
import org.eclipse.che.plugin.web.shared.Constants;

/**
 * Action to create new Vue file.
 *
 * @author Sébastien Demanou
 */
@Singleton
public class NewVueFileAction extends AbstractNewResourceAction {
  private static final String DEFAULT_CONTENT =
      "<template></template>\n"
          + "\n"
          + "<script>\n"
          + "  export default {};\n"
          + "<script>\n"
          + "\n"
          + "<style scoped>\n"
          + "</style>\n";

  @Inject
  public NewVueFileAction(
      WebLocalizationConstant localizationConstant,
      DialogFactory dialogFactory,
      CoreLocalizationConstant coreLocalizationConstant,
      EventBus eventBus,
      AppContext appContext,
      NotificationManager notificationManager,
      Provider<EditorAgent> editorAgentProvider) {
    super(
        localizationConstant.newVueFileActionTitle(),
        localizationConstant.newVueFileActionDescription(),
        null,
        dialogFactory,
        coreLocalizationConstant,
        eventBus,
        appContext,
        notificationManager,
        editorAgentProvider);
  }

  @Override
  protected String getExtension() {
    return Constants.VUE_EXT;
  }

  @Override
  protected String getDefaultContent() {
    return DEFAULT_CONTENT;
  }
}
