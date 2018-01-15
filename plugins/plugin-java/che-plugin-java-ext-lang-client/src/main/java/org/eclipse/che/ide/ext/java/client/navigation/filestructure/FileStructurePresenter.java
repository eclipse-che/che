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
package org.eclipse.che.ide.ext.java.client.navigation.filestructure;

import com.google.common.base.Optional;
import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.SyntheticFile;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.ext.java.shared.dto.ClassContent;
import org.eclipse.che.ide.ext.java.shared.dto.Region;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.Member;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.ide.util.loging.Log;

/**
 * The class that manages class structure window.
 *
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class FileStructurePresenter implements FileStructure.ActionDelegate {
  private final FileStructure view;
  private final JavaNavigationService javaNavigationService;
  private final AppContext context;
  private final EditorAgent editorAgent;
  private final MessageLoader loader;

  private TextEditor activeEditor;
  private boolean showInheritedMembers;
  private int cursorOffset;

  @Inject
  public FileStructurePresenter(
      FileStructure view,
      JavaNavigationService javaNavigationService,
      AppContext context,
      EditorAgent editorAgent,
      LoaderFactory loaderFactory) {
    this.view = view;
    this.javaNavigationService = javaNavigationService;
    this.context = context;
    this.editorAgent = editorAgent;
    this.loader = loaderFactory.newLoader();
    this.view.setDelegate(this);
  }

  /**
   * Shows the structure of the opened class.
   *
   * @param editorPartPresenter the active editor
   */
  public void show(EditorPartPresenter editorPartPresenter) {
    loader.show();
    view.setTitle(editorPartPresenter.getEditorInput().getFile().getName());

    if (!(editorPartPresenter instanceof TextEditor)) {
      Log.error(getClass(), "Open Declaration support only TextEditor as editor");
      return;
    }
    activeEditor = ((TextEditor) editorPartPresenter);
    cursorOffset = activeEditor.getCursorOffset();
    VirtualFile file = activeEditor.getEditorInput().getFile();

    if (file instanceof Resource) {
      final Optional<Project> project = ((Resource) file).getRelatedProject();

      final Optional<Resource> srcFolder =
          ((Resource) file).getParentWithMarker(SourceFolderMarker.ID);

      if (!srcFolder.isPresent()) {
        return;
      }

      final String fqn = JavaUtil.resolveFQN((Container) srcFolder.get(), (Resource) file);

      javaNavigationService
          .getCompilationUnit(project.get().getLocation(), fqn, showInheritedMembers)
          .then(
              new Operation<CompilationUnit>() {
                @Override
                public void apply(CompilationUnit unit) throws OperationException {
                  view.setStructure(unit, showInheritedMembers);
                  showInheritedMembers = !showInheritedMembers;
                  loader.hide();
                  view.show();
                }
              })
          .catchError(
              new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                  Log.error(FileStructurePresenter.class, arg.getMessage());
                  loader.hide();
                }
              });
    }
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(final Member member) {
    if (member.isBinary()) {

      final Resource resource = context.getResource();

      if (resource == null) {
        return;
      }

      final Optional<Project> project = resource.getRelatedProject();

      javaNavigationService
          .getEntry(project.get().getLocation(), member.getLibId(), member.getRootPath())
          .then(
              new Operation<JarEntry>() {
                @Override
                public void apply(final JarEntry entry) throws OperationException {
                  javaNavigationService
                      .getContent(
                          project.get().getLocation(),
                          member.getLibId(),
                          Path.valueOf(entry.getPath()))
                      .then(
                          new Operation<ClassContent>() {
                            @Override
                            public void apply(ClassContent content) throws OperationException {
                              final String clazz =
                                  entry.getName().substring(0, entry.getName().indexOf('.'));
                              final VirtualFile file =
                                  new SyntheticFile(entry.getName(), clazz, content.getContent());
                              editorAgent.openEditor(
                                  file,
                                  new OpenEditorCallbackImpl() {
                                    @Override
                                    public void onEditorOpened(EditorPartPresenter editor) {
                                      setCursor(editor, member.getFileRegion().getOffset());
                                    }
                                  });
                            }
                          });
                }
              });
    } else {
      context
          .getWorkspaceRoot()
          .getFile(member.getRootPath())
          .then(
              new Operation<Optional<File>>() {
                @Override
                public void apply(Optional<File> file) throws OperationException {
                  if (file.isPresent()) {
                    editorAgent.openEditor(
                        file.get(),
                        new OpenEditorCallbackImpl() {
                          @Override
                          public void onEditorOpened(EditorPartPresenter editor) {
                            setCursor(editor, member.getFileRegion().getOffset());
                          }
                        });
                  }
                }
              });
    }
    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                setCursorPosition(member.getFileRegion());
              }
            });
    showInheritedMembers = false;
  }

  @Override
  public void onEscapeClicked() {
    activeEditor.setFocus();
    setCursor(activeEditor, cursorOffset);
  }

  private void setCursorPosition(Region region) {
    LinearRange linearRange =
        LinearRange.createWithStart(region.getOffset()).andLength(region.getLength());
    activeEditor.setFocus();
    activeEditor.getDocument().setSelectedRange(linearRange, true);
  }

  private void setCursor(EditorPartPresenter editor, int offset) {
    if (editor instanceof TextEditor) {
      ((TextEditor) editor)
          .getDocument()
          .setSelectedRange(LinearRange.createWithStart(offset).andLength(0), true);
    }
  }
}
