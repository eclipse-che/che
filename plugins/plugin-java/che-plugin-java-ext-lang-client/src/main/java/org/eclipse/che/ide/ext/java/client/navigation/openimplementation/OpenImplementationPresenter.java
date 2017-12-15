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

import com.google.common.base.Optional;
import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.ext.java.dto.DtoClientImpls.FindImplementationsCommandParametersDto;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.jdt.ls.extension.api.dto.navigation.FindImplementationsCommandParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.navigation.ImplementationsDescriptor;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;

/**
 * The class that manages implementations structure window.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class OpenImplementationPresenter {

  private final JavaNavigationService service;
  private final AppContext context;
  private final EditorAgent editorAgent;
  private final DtoFactory dtoFactory;
  private final JavaResources javaResources;
  private final PopupResources popupResources;
  private final JavaLocalizationConstant locale;

  private TextEditor activeEditor;

  @Inject
  public OpenImplementationPresenter(
      JavaNavigationService javaNavigationService,
      AppContext context,
      DtoFactory dtoFactory,
      JavaResources javaResources,
      PopupResources popupResources,
      JavaLocalizationConstant locale,
      EditorAgent editorAgent) {
    this.service = javaNavigationService;
    this.context = context;
    this.dtoFactory = dtoFactory;
    this.javaResources = javaResources;
    this.popupResources = popupResources;
    this.locale = locale;
    this.editorAgent = editorAgent;
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

    if (file instanceof Resource) {
      final Project project = ((Resource) file).getProject();

      final Optional<Resource> srcFolder =
          ((Resource) file).getParentWithMarker(SourceFolderMarker.ID);

      if (project == null || !srcFolder.isPresent()) {
        return;
      }

      final String fqn = JavaUtil.resolveFQN((Container) srcFolder.get(), (Resource) file);

      service
          .findImplementations(
              new FindImplementationsCommandParametersDto(
                  new FindImplementationsCommandParameters(
                      project.getLocation().toString(), fqn, activeEditor.getCursorOffset())))
          .then(
              impls -> {
                int overridingSize = impls.getImplementations().size();

                String title =
                    locale.openImplementationWindowTitle(impls.getMemberName(), overridingSize);
                NoImplementationWidget noImplementationWidget =
                    new NoImplementationWidget(
                        popupResources,
                        javaResources,
                        locale,
                        OpenImplementationPresenter.this,
                        title);
                if (overridingSize == 1) {
                  actionPerformed(impls.getImplementations().get(0));
                } else if (overridingSize > 1) {
                  openOneImplementation(
                      impls, noImplementationWidget, (TextEditor) editorPartPresenter);
                } else if (!isNullOrEmpty(impls.getMemberName()) && overridingSize == 0) {
                  showNoImplementations(noImplementationWidget, (TextEditor) editorPartPresenter);
                }
              });
    }
  }

  public void actionPerformed(final SymbolInformation symbolInformation) {
    //    if (member.isBinary()) {
    //
    //      final Resource resource = context.getResource();
    //
    //      if (resource == null) {
    //        return;
    //      }
    //
    //      final Optional<Project> project = resource.getRelatedProject();
    //
    //      service
    //          .getEntry(project.get().getLocation(), member.getLibId(), member.getRootPath())
    //          .then(
    //              new Operation<JarEntry>() {
    //                @Override
    //                public void apply(final JarEntry entry) throws OperationException {
    //                  service
    //                      .getContent(
    //                          project.get().getLocation(),
    //                          member.getLibId(),
    //                          Path.valueOf(entry.getPath()))
    //                      .then(
    //                          new Operation<ClassContent>() {
    //                            @Override
    //                            public void apply(ClassContent content) throws OperationException
    // {
    //                              final String clazz =
    //                                  entry.getName().substring(0, entry.getName().indexOf('.'));
    //                              final VirtualFile file =
    //                                  new SyntheticFile(entry.getName(), clazz,
    // content.getContent());
    //                              editorAgent.openEditor(
    //                                  file,
    //                                  new OpenEditorCallbackImpl() {
    //                                    @Override
    //                                    public void onEditorOpened(EditorPartPresenter editor) {
    //                                      setCursor(member.getFileRegion());
    //                                    }
    //                                  });
    //                            }
    //                          });
    //                }
    //              });
    //    } else {
    context
        .getWorkspaceRoot()
        .getFile(symbolInformation.getLocation().getUri())
        .then(
            file -> {
              if (file.isPresent()) {
                editorAgent.openEditor(
                    file.get(),
                    new OpenEditorCallbackImpl() {
                      @Override
                      public void onEditorOpened(EditorPartPresenter editor) {
                        setCursor(symbolInformation.getLocation().getRange());
                      }
                    });
              }
            });
    // }
    Scheduler.get().scheduleDeferred(() -> activeEditor.setFocus());
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

  private void openOneImplementation(
      ImplementationsDescriptor implementationsDescriptor,
      NoImplementationWidget implementationWidget,
      TextEditor editorPartPresenter) {
    int offset = editorPartPresenter.getCursorOffset();
    PositionConverter.PixelCoordinates coordinates =
        editorPartPresenter.getPositionConverter().offsetToPixel(offset);
    for (SymbolInformation symbolInformation : implementationsDescriptor.getImplementations()) {
      implementationWidget.addItem(symbolInformation);
    }
    implementationWidget.show(coordinates.getX(), coordinates.getY());
    implementationWidget.asElement().getStyle().setWidth(600 + "px");
  }

  private void setCursor(final Range region) {
    if (!(editorAgent.getActiveEditor() instanceof TextEditor)) {
      return;
    }

    Scheduler.get()
        .scheduleDeferred(
            () -> {
              ((TextEditor) editorAgent.getActiveEditor())
                  .setCursorPosition(
                      new TextPosition(
                          region.getStart().getLine(), region.getStart().getCharacter()));
            });
  }
}
