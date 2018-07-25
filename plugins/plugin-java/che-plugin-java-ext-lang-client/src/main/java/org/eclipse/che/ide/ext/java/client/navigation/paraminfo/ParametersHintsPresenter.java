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
package org.eclipse.che.ide.ext.java.client.navigation.paraminfo;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.ext.java.shared.dto.model.MethodParameters;
import org.eclipse.che.ide.util.loging.Log;

/**
 * The class contains business logic which calls {@link JavaNavigationService} to get method
 * parameters hints.We can use the class to call service to get parameters hints for particular
 * overloading method.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class ParametersHintsPresenter {

  private final JavaNavigationService navigationService;
  private final ParametersHintsView view;

  @Inject
  public ParametersHintsPresenter(
      JavaNavigationService navigationService, ParametersHintsView view) {
    this.navigationService = navigationService;
    this.view = view;
  }

  /**
   * The method gets method parameters via {@link JavaNavigationService} and then call special
   * method on view to display them.
   *
   * @param activeEditor active editor which contains method or constructor for which parameters
   *     will be displayed
   */
  public void show(final TextEditor activeEditor) {
    final int offset = activeEditor.getCursorOffset();

    if (!isCursorInRightPlace(activeEditor, offset)) {
      return;
    }

    VirtualFile file = activeEditor.getEditorInput().getFile();

    if (file instanceof Resource) {
      final Optional<Project> project = ((Resource) file).getRelatedProject();
      final int lineStartOffset = getLineStartOffset(activeEditor, offset);

      navigationService
          .getMethodParametersHints(
              project.get().getLocation(), JavaUtil.resolveFQN(file), offset, lineStartOffset)
          .then(
              new Operation<List<MethodParameters>>() {
                @Override
                public void apply(List<MethodParameters> parameters) throws OperationException {
                  if (parameters.isEmpty()) {
                    return;
                  }

                  PositionConverter.PixelCoordinates coordinates =
                      activeEditor.getPositionConverter().offsetToPixel(offset);

                  view.show(parameters, coordinates.getX(), coordinates.getY());
                }
              })
          .catchError(
              new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError error) throws OperationException {
                  Log.error(getClass(), error.getMessage());
                }
              });
    }
  }

  private boolean isCursorInRightPlace(TextEditor activeEditor, int offset) {
    Document document = activeEditor.getDocument();

    int lineIndex = document.getLineAtOffset(offset);
    int nextLineIndex = lineIndex + 1;

    int nextLineStart = document.getLineStart(nextLineIndex);
    String contentRange =
        activeEditor.getDocument().getContentRange(offset, nextLineStart - offset);

    return contentRange.contains(")");
  }

  private int getLineStartOffset(TextEditor activeEditor, int offset) {
    Document document = activeEditor.getDocument();
    int lineIndex = document.getLineAtOffset(offset);
    return document.getLineStart(lineIndex);
  }
}
