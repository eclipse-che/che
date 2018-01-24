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

import static org.eclipse.che.ide.api.editor.text.LinearRange.createWithStart;

import com.google.common.base.Optional;
import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.ext.java.client.tree.JavaNodeFactory;
import org.eclipse.che.ide.ext.java.client.tree.library.JarFileNode;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.ext.java.shared.OpenDeclarationDescriptor;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.JarEntry;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;

/**
 * @author Evgen Vidolob
 * @author Vlad Zhukovskyi
 */
@Singleton
public class OpenDeclarationFinder {

  private final EditorAgent editorAgent;
  private DtoFactory dtoFactory;
  private final JavaNavigationService navigationService;
  private final JavaLanguageExtensionServiceClient extensionService;
  private final TextDocumentServiceClient textDocumentService;
  private final AppContext appContext;
  private JavaNodeFactory javaNodeFactory;

  @Inject
  public OpenDeclarationFinder(
      EditorAgent editorAgent,
      DtoFactory dtoFactory,
      JavaNavigationService navigationService,
      JavaLanguageExtensionServiceClient extensionService,
      TextDocumentServiceClient textDocumentService,
      AppContext appContext,
      JavaNodeFactory javaNodeFactory) {
    this.editorAgent = editorAgent;
    this.dtoFactory = dtoFactory;
    this.navigationService = navigationService;
    this.extensionService = extensionService;
    this.textDocumentService = textDocumentService;
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
              result -> {
                if (result != null) {
                  handleDescriptor(project.get().getLocation(), result);
                }
              });

    } else if (file instanceof JarFileNode) {
      navigationService
          .findDeclaration(
              ((JarFileNode) file).getProjectLocation(),
              file.getLocation().toString().replace('/', '.'),
              offset)
          .then(
              result -> {
                if (result != null) {
                  handleDescriptor(((JarFileNode) file).getProject(), result);
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
              .setSelectedRange(createWithStart(offset).andLength(0), true);
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
      activateOpenedEditor(descriptor, openedEditor);
      return;
    }

    if (descriptor.isBinary()) {
      getLibraryEntry(projectPath, descriptor);
    } else {
      openFileFromWorkspace(descriptor);
    }
  }

  private void activateOpenedEditor(
      OpenDeclarationDescriptor descriptor, EditorPartPresenter openedEditor) {
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
  }

  private void openFileFromWorkspace(OpenDeclarationDescriptor descriptor) {
    appContext
        .getWorkspaceRoot()
        .getFile(descriptor.getPath())
        .then(
            file -> {
              if (file.isPresent()) {
                openEditor(descriptor, file.get());
              }
            });
  }

  private void getLibraryEntry(Path projectPath, OpenDeclarationDescriptor descriptor) {
    ExternalLibrariesParameters entryParams =
        dtoFactory.createDto(ExternalLibrariesParameters.class);
    entryParams.setNodeId(descriptor.getLibId());
    entryParams.setNodePath(descriptor.getPath());
    entryParams.setProjectUri(projectPath.toString());
    extensionService
        .libraryEntry(entryParams)
        .then(
            entry -> {
              openBinaryContent(projectPath, descriptor, entry);
            });
  }

  private void openBinaryContent(
      Path projectPath, OpenDeclarationDescriptor descriptor, JarEntry entry) {
    ExternalLibrariesParameters params = dtoFactory.createDto(ExternalLibrariesParameters.class);
    params.setNodeId(descriptor.getLibId());
    params.setNodePath(entry.getPath());
    params.setProjectUri(projectPath.toString());
    textDocumentService
        .getFileContent(entry.getUri())
        .then(
            content -> {
              final VirtualFile file =
                  javaNodeFactory.newJarFileNode(entry, descriptor.getLibId(), projectPath, null);
              openEditor(descriptor, file);
            });
  }

  private void openEditor(OpenDeclarationDescriptor descriptor, VirtualFile file) {
    editorAgent.openEditor(
        file,
        new OpenEditorCallbackImpl() {
          @Override
          public void onEditorOpened(final EditorPartPresenter editor) {
            OpenDeclarationFinder.this.onEditorOpened(editor, descriptor);
          }
        });
  }

  private void onEditorOpened(EditorPartPresenter editor, OpenDeclarationDescriptor descriptor) {
    Scheduler.get()
        .scheduleDeferred(
            () -> {
              if (!(editor instanceof TextEditor)) {
                return;
              }
              ((TextEditor) editor)
                  .getDocument()
                  .setSelectedRange(createWithStart(descriptor.getOffset()).andLength(0), true);
              editor.activate();
            });
  }
}
