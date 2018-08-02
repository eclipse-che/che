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
package org.eclipse.che.plugin.python.ide.action;

import static org.eclipse.che.plugin.python.shared.ProjectAttributes.PYTHON_EXT;

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
import org.eclipse.che.plugin.python.ide.PythonLocalizationConstant;
import org.eclipse.che.plugin.python.ide.PythonResources;

/**
 * Action to create new Python source file.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class CreatePythonFileAction extends AbstractNewResourceAction {

  @Inject
  public CreatePythonFileAction(
      PythonLocalizationConstant localizationConstant,
      PythonResources pythonResources,
      DialogFactory dialogFactory,
      CoreLocalizationConstant coreLocalizationConstant,
      EventBus eventBus,
      AppContext appContext,
      NotificationManager notificationManager,
      Provider<EditorAgent> editorAgentProvider) {
    super(
        localizationConstant.createPythonFileActionTitle(),
        localizationConstant.createPythonFileActionDescription(),
        pythonResources.pythonFile(),
        dialogFactory,
        coreLocalizationConstant,
        eventBus,
        appContext,
        notificationManager,
        editorAgentProvider);
  }

  @Override
  protected String getExtension() {
    return PYTHON_EXT;
  }

  @Override
  protected String getDefaultContent() {
    return "";
  }
}
