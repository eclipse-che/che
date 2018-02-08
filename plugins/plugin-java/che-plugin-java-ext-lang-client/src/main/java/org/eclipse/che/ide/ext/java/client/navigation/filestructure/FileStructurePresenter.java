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
package org.eclipse.che.ide.ext.java.client.navigation.filestructure;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.ext.java.dto.DtoClientImpls;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.jdt.ls.extension.api.dto.ExtendedSymbolInformation;
import org.eclipse.che.jdt.ls.extension.api.dto.FileStructureCommandParameters;
import org.eclipse.che.plugin.languageserver.ide.filestructure.ElementSelectionDelegate;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;

/**
 * Manages jdt.ls based file structure view
 *
 * @author Thomas Mäder
 */
@Singleton
public class FileStructurePresenter implements ElementSelectionDelegate<ExtendedSymbolInformation> {
  private final FileStructureWindow view;
  private final JavaLanguageExtensionServiceClient javaExtensionService;
  private final OpenFileInEditorHelper openHelper;
  private final DtoBuildHelper dtoHelper;

  private TextEditor activeEditor;
  private boolean showInheritedMembers;

  @Inject
  public FileStructurePresenter(
      FileStructureWindow view,
      JavaLanguageExtensionServiceClient javaExtensionService,
      OpenFileInEditorHelper openHelper,
      DtoBuildHelper dtoHelper) {
    this.view = view;
    this.javaExtensionService = javaExtensionService;
    this.openHelper = openHelper;
    this.dtoHelper = dtoHelper;
    this.view.setDelegate(this);
  }

  /**
   * Shows the structure of the opened class.
   *
   * @param editorPartPresenter the active editor
   */
  public void show(EditorPartPresenter editorPartPresenter) {
    if (view.isShowing()) {
      showInheritedMembers = !showInheritedMembers;
      view.setShowInherited(showInheritedMembers);
      VirtualFile file = activeEditor.getEditorInput().getFile();
      javaExtensionService
          .fileStructure(
              new DtoClientImpls.FileStructureCommandParametersDto(
                  new FileStructureCommandParameters(dtoHelper.getUri(file), showInheritedMembers)))
          .then(
              result -> {
                loader.hide();
                view.setInput(result);
                view.show();
              })
          .catchError(
              e -> {
                loader.hide();
              });
    } else {
      showInheritedMembers = false;
      view.setTitle(editorPartPresenter.getEditorInput().getFile().getName());
      view.setShowInherited(showInheritedMembers);

      if (!(editorPartPresenter instanceof TextEditor)) {
        Log.error(getClass(), "Open Declaration support only TextEditor as editor");
        return;
      }
      activeEditor = ((TextEditor) editorPartPresenter);
      VirtualFile file = activeEditor.getEditorInput().getFile();
      javaExtensionService
          .fileStructure(
              new DtoClientImpls.FileStructureCommandParametersDto(
                  new FileStructureCommandParameters(dtoHelper.getUri(file), showInheritedMembers)))
          .then(
              result -> {
                loader.hide();
                view.setInput(result);
                view.show();
              })
          .catchError(
              e -> {
                loader.hide();
                Log.error(getClass(), e);
              });
    }
  }

  @Override
  public void onSelect(ExtendedSymbolInformation element) {
    view.hide();
    showInheritedMembers = false;
    openHelper.openLocation(element.getInfo().getLocation());
  }

  @Override
  public void onCancel() {
    view.hide();
    activeEditor.setFocus();
  }
}
