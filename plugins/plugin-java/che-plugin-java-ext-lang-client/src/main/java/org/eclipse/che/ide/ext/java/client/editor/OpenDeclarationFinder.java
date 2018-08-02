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
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.common.base.Optional;
import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.DelayedTask;
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
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.tree.JavaNodeFactory;
import org.eclipse.che.ide.ext.java.client.tree.library.JarFileNode;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.ext.java.shared.OpenDeclarationDescriptor;
import org.eclipse.che.ide.ext.java.shared.dto.ClassContent;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.loging.Log;

/**
 * @author Evgen Vidolob
 * @author Vlad Zhukovskyi
 */
@Singleton
public class OpenDeclarationFinder {

  private final EditorAgent editorAgent;
  private final JavaNavigationService navigationService;
  private final AppContext appContext;
  private JavaNodeFactory javaNodeFactory;

  @Inject
  public OpenDeclarationFinder(
      EditorAgent editorAgent,
      JavaNavigationService navigationService,
      AppContext appContext,
      JavaNodeFactory javaNodeFactory) {
    this.editorAgent = editorAgent;
    this.navigationService = navigationService;
    this.appContext = appContext;
    this.javaNodeFactory = javaNodeFactory;
  }

  public void openDeclaration() {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor == null) {
      return;
    }

    if (!(activeEditor instanceof TextEditor)) {
      Log.error(getClass(), "Open Declaration support only TextEditor as editor");
      return;
    }
    TextEditor editor = ((TextEditor) activeEditor);
    int offset = editor.getCursorOffset();
    final VirtualFile file = editor.getEditorInput().getFile();

    if (file instanceof Resource) {
      final Optional<Project> project = ((Resource) file).getRelatedProject();

      final Optional<Resource> srcFolder =
          ((Resource) file).getParentWithMarker(SourceFolderMarker.ID);

      if (!srcFolder.isPresent()) {
        return;
      }

      final String fqn = JavaUtil.resolveFQN((Container) srcFolder.get(), (Resource) file);

      navigationService
          .findDeclaration(project.get().getLocation(), fqn, offset)
          .then(
              new Operation<OpenDeclarationDescriptor>() {
                @Override
                public void apply(OpenDeclarationDescriptor result) throws OperationException {
                  if (result != null) {
                    handleDescriptor(project.get().getLocation(), result);
                  }
                }
              });

    } else if (file instanceof JarFileNode) {
      navigationService
          .findDeclaration(
              ((JarFileNode) file).getProjectLocation(),
              file.getLocation().toString().replace('/', '.'),
              offset)
          .then(
              new Operation<OpenDeclarationDescriptor>() {
                @Override
                public void apply(OpenDeclarationDescriptor result) throws OperationException {
                  if (result != null) {
                    handleDescriptor(((JarFileNode) file).getProject(), result);
                  }
                }
              });
    }
  }

  private void setCursorAndActivateEditor(final EditorPartPresenter editor, final int offset) {

    /*
    For some undefined reason we need to wrap cursor and focus setting into timer with 1ms.
    When editors are switching, they don't have enough time to redraw and set focus. Need
    to investigate this problem more deeply. But at this moment this trick works as well.
    */

    new DelayedTask() {
      @Override
      public void onExecute() {
        if (editor instanceof TextEditor) {
          ((TextEditor) editor)
              .getDocument()
              .setSelectedRange(LinearRange.createWithStart(offset).andLength(0), true);
          editor.activate(); // force set focus to the editor
        }
      }
    }.delay(1);
  }

  private void handleDescriptor(
      final Path projectPath, final OpenDeclarationDescriptor descriptor) {
    final EditorPartPresenter openedEditor =
        editorAgent.getOpenedEditor(Path.valueOf(descriptor.getPath()));
    if (openedEditor != null) {
      editorAgent.openEditor(
          openedEditor.getEditorInput().getFile(),
          new OpenEditorCallbackImpl() {
            @Override
            public void onEditorOpened(EditorPartPresenter editor) {
              setCursorAndActivateEditor(editor, descriptor.getOffset());
            }

            @Override
            public void onEditorActivated(EditorPartPresenter editor) {
              setCursorAndActivateEditor(editor, descriptor.getOffset());
            }
          });
      return;
    }

    if (descriptor.isBinary()) {
      navigationService
          .getEntry(projectPath, descriptor.getLibId(), descriptor.getPath())
          .then(
              new Operation<JarEntry>() {
                @Override
                public void apply(final JarEntry entry) throws OperationException {
                  navigationService
                      .getContent(projectPath, descriptor.getLibId(), Path.valueOf(entry.getPath()))
                      .then(
                          new Operation<ClassContent>() {
                            @Override
                            public void apply(ClassContent content) throws OperationException {
                              final VirtualFile file =
                                  javaNodeFactory.newJarFileNode(
                                      entry, descriptor.getLibId(), projectPath, null);
                              editorAgent.openEditor(
                                  file,
                                  new OpenEditorCallbackImpl() {
                                    @Override
                                    public void onEditorOpened(final EditorPartPresenter editor) {
                                      Scheduler.get()
                                          .scheduleDeferred(
                                              new Scheduler.ScheduledCommand() {
                                                @Override
                                                public void execute() {
                                                  if (editor instanceof TextEditor) {
                                                    ((TextEditor) editor)
                                                        .getDocument()
                                                        .setSelectedRange(
                                                            LinearRange.createWithStart(
                                                                    descriptor.getOffset())
                                                                .andLength(0),
                                                            true);
                                                    editor.activate();
                                                  }
                                                }
                                              });
                                    }
                                  });
                            }
                          });
                }
              });
    } else {
      appContext
          .getWorkspaceRoot()
          .getFile(descriptor.getPath())
          .then(
              new Operation<Optional<File>>() {
                @Override
                public void apply(Optional<File> file) throws OperationException {
                  if (file.isPresent()) {
                    editorAgent.openEditor(
                        file.get(),
                        new OpenEditorCallbackImpl() {
                          @Override
                          public void onEditorOpened(final EditorPartPresenter editor) {
                            Scheduler.get()
                                .scheduleDeferred(
                                    new Scheduler.ScheduledCommand() {
                                      @Override
                                      public void execute() {
                                        if (editor instanceof TextEditor) {
                                          ((TextEditor) editor)
                                              .getDocument()
                                              .setSelectedRange(
                                                  LinearRange.createWithStart(
                                                          descriptor.getOffset())
                                                      .andLength(0),
                                                  true);
                                          editor.activate();
                                        }
                                      }
                                    });
                          }
                        });
                  }
                }
              });
    }
  }
}
