/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.navigation.openimplementation;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.languageserver.shared.dto.DtoClientImpls.TextDocumentPositionParamsDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.jdt.ls.extension.api.dto.ImplementersResponse;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;

/**
 * The class that manages implementations structure window.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class OpenImplementationPresenter {

  private final JavaLanguageExtensionServiceClient javaLanguageExtensionServiceClient;
  private final JavaResources javaResources;
  private final PopupResources popupResources;
  private final JavaLocalizationConstant locale;
  private final OpenFileInEditorHelper openHelper;

  private TextEditor activeEditor;

  @Inject
  public OpenImplementationPresenter(
      JavaLanguageExtensionServiceClient javaLanguageExtensionServiceClient,
      AppContext context,
      DtoFactory dtoFactory,
      JavaResources javaResources,
      PopupResources popupResources,
      JavaLocalizationConstant locale,
      EditorAgent editorAgent,
      OpenFileInEditorHelper openHelper) {
    this.javaLanguageExtensionServiceClient = javaLanguageExtensionServiceClient;
    this.javaResources = javaResources;
    this.popupResources = popupResources;
    this.locale = locale;
    this.openHelper = openHelper;
  }

  /**
   * Shows the implementations of the selected element.
   *
   * @param editorPartPresenter the active editor
   */
  public void show(final EditorPartPresenter editorPartPresenter) {
    if (!(editorPartPresenter instanceof TextEditor)) {
      Log.error(getClass(), "Open Declaration support only TextEditor as editor");
      return;
    }
    activeEditor = ((TextEditor) editorPartPresenter);
    final VirtualFile file = activeEditor.getEditorInput().getFile();

    javaLanguageExtensionServiceClient
        .findImplementations(
            new TextDocumentPositionParamsDto(
                new TextDocumentPositionParams(
                    new TextDocumentIdentifier(file.getLocation().toString()),
                    new Position(
                        activeEditor.getCursorPosition().getLine(),
                        activeEditor.getCursorPosition().getCharacter()))))
        .then(
            impls -> {
              int overridingSize = impls.getImplementers().size();

              String title =
                  locale.openImplementationWindowTitle(impls.getSearchedElement(), overridingSize);
              NoImplementationWidget noImplementationWidget =
                  new NoImplementationWidget(
                      popupResources,
                      javaResources,
                      locale,
                      OpenImplementationPresenter.this,
                      title);
              if (overridingSize == 1) {
                openOneImplementation(impls.getImplementers().get(0));
              } else if (overridingSize > 1) {
                openImplementations(
                    impls, noImplementationWidget, (TextEditor) editorPartPresenter);
              } else if (!isNullOrEmpty(impls.getSearchedElement()) && overridingSize == 0) {
                showNoImplementations(noImplementationWidget, (TextEditor) editorPartPresenter);
              }
            });
  }

  public void openOneImplementation(final SymbolInformation symbolInformation) {
    this.openHelper.openLocation(symbolInformation.getLocation());
  }

  private void showNoImplementations(
      NoImplementationWidget noImplementationWidget, TextEditor editorPartPresenter) {
    int offset = editorPartPresenter.getCursorOffset();
    PositionConverter.PixelCoordinates coordinates =
        editorPartPresenter.getPositionConverter().offsetToPixel(offset);
    SymbolInformation symbolInformation = new SymbolInformation();
    symbolInformation.setKind(null);
    noImplementationWidget.addItem(symbolInformation);
    noImplementationWidget.show(coordinates.getX(), coordinates.getY());
  }

  private void openImplementations(
      ImplementersResponse implementersResponse,
      NoImplementationWidget implementationWidget,
      TextEditor editorPartPresenter) {
    int offset = editorPartPresenter.getCursorOffset();
    PositionConverter.PixelCoordinates coordinates =
        editorPartPresenter.getPositionConverter().offsetToPixel(offset);
    for (SymbolInformation symbolInformation : implementersResponse.getImplementers()) {
      implementationWidget.addItem(symbolInformation);
    }
    implementationWidget.show(coordinates.getX(), coordinates.getY());
    implementationWidget.asElement().getStyle().setWidth(600 + "px");
  }
}
