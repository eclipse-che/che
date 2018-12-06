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
package org.eclipse.che.plugin.golang.ide;

import static org.eclipse.che.plugin.golang.shared.Constants.GOLANG_FILE_EXT;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;

/** @author Eugene Ivantsov */
@Extension(title = "Golang")
public class GolangExtension {

  public static final String GOLANG_CATEGORY = "Golang";

  @Inject
  private void prepareActions(GolangResources resources, IconRegistry iconRegistry) {
    iconRegistry.registerIcon(
        new Icon(GOLANG_CATEGORY + ".samples.category.icon", resources.golangIcon()));
  }

  // TODO(vzhukovskyi): has to be removed when https://github.com/eclipse/che/issues/11907 resolved
  @Inject
  private void temporaryDisableRenameAction(EventBus eventBus) {
    eventBus.addHandler(
        ActivePartChangedEvent.TYPE,
        event -> {
          PartPresenter activePart = event.getActivePart();
          if (!(activePart instanceof TextEditor)) {
            return;
          }

          if (!GOLANG_FILE_EXT.equals(
              ((TextEditor) activePart)
                  .getEditorInput()
                  .getFile()
                  .getLocation()
                  .getFileExtension())) {
            return;
          }

          TextEditorConfiguration configuration = ((TextEditor) activePart).getConfiguration();
          if (!(configuration instanceof LanguageServerEditorConfiguration)) {
            return;
          }

          ((LanguageServerEditorConfiguration) configuration)
              .getServerCapabilities()
              .setRenameProvider(false);
        });
  }
}
