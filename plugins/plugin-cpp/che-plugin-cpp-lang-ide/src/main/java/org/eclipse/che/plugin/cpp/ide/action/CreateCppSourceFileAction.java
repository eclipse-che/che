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
package org.eclipse.che.plugin.cpp.ide.action;

import static org.eclipse.che.plugin.cpp.shared.Constants.CPP_EXT;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.plugin.cpp.ide.CppLocalizationConstant;
import org.eclipse.che.plugin.cpp.ide.CppResources;

/**
 * Action to create new C++ source file.
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class CreateCppSourceFileAction extends NewClikeResourceAction {

  private static final String DEFAULT_CONTENT =
      "#include <iostream>\n"
          + "\n"
          + "int main()\n"
          + "{\n"
          + "    std::cout << \"Hello, world!\\n\";\n"
          + "    return 0;\n"
          + "}";

  @Inject
  public CreateCppSourceFileAction(
      CppLocalizationConstant localizationConstant,
      CppResources cppResources,
      DialogFactory dialogFactory,
      CoreLocalizationConstant coreLocalizationConstant,
      EventBus eventBus,
      AppContext appContext,
      NotificationManager notificationManager,
      Provider<EditorAgent> editorAgentProvider) {
    super(
        localizationConstant.createCppFileActionTitle(),
        localizationConstant.createCppFileActionDescription(),
        cppResources.cppFile(),
        dialogFactory,
        coreLocalizationConstant,
        eventBus,
        appContext,
        notificationManager,
        editorAgentProvider);
  }

  @Override
  protected String getExtension() {
    return CPP_EXT;
  }

  @Override
  protected String getDefaultContent() {
    return DEFAULT_CONTENT;
  }
}
