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
package org.eclipse.che.ide.ext.java.client.navigation.openimplementation;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.Collections;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.ext.java.shared.dto.ImplementationsDescriptorDTO;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Valeriy Svydenko */
@RunWith(GwtMockitoTestRunner.class)
public class OpenImplementationPresenterTest {
  // constructor mocks
  @Mock private JavaNavigationService javaNavigationService;
  @Mock private AppContext appContext;
  @Mock private EditorAgent editorAgent;
  @Mock() private JavaResources javaResources;
  @Mock private DtoFactory dtoFactory;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private PopupResources popupResources;

  @Mock private JavaLocalizationConstant locale;

  // other mocks

  @Mock private TextEditor editor;
  @Mock private EditorInput editorInput;
  @Mock private File file;
  @Mock private Project relatedProject;
  @Mock private Container srcFolder;
  @Mock private ImplementationsDescriptorDTO implementationDescriptor;
  @Mock private Type type1;
  @Mock private Type type2;
  @Mock private JarEntry jarEntry;
  @Mock private Container workspaceRoot;
  @Mock private PositionConverter positionConverter;
  @Mock private SVGResource svgResource;
  @Mock private OMSVGSVGElement omsvgsvgElement;

  @Mock private Promise<ImplementationsDescriptorDTO> implementationsPromise;
  @Mock private Promise<JarEntry> jarEntryPromise;
  @Mock private Promise<String> contentPromise;
  @Mock private Promise<Optional<File>> realFilePromise;

  @Captor ArgumentCaptor<Operation<ImplementationsDescriptorDTO>> implementationsOperation;
  @Captor ArgumentCaptor<Operation<JarEntry>> jarEntryOperation;
  @Captor ArgumentCaptor<Operation<String>> contentOperation;
  @Captor ArgumentCaptor<Operation<Optional<File>>> realFileOperation;

  private OpenImplementationPresenter presenter;

  @Before
  public void setUp() throws Exception {
    presenter =
        new OpenImplementationPresenter(
            javaNavigationService,
            appContext,
            dtoFactory,
            javaResources,
            popupResources,
            locale,
            editorAgent);
  }

  @Test
  public void testShouldDisplayOneImplementationIsRealFile() throws Exception {
    when(editor.getEditorInput()).thenReturn(editorInput);
    when(editorInput.getFile()).thenReturn(file);
    when(file.getRelatedProject()).thenReturn(Optional.of(relatedProject));
    when(file.getParentWithMarker(eq(SourceFolderMarker.ID))).thenReturn(Optional.of(srcFolder));
    when(file.getLocation()).thenReturn(Path.valueOf("/a/b/c/d/file.java"));
    when(srcFolder.getLocation()).thenReturn(Path.valueOf("/a/b"));
    when(file.getResourceType()).thenReturn(Resource.FILE);
    when(file.getExtension()).thenReturn("java");
    when(file.getName()).thenReturn("file.java");
    when(relatedProject.getLocation()).thenReturn(Path.valueOf("/a"));
    when(editor.getCursorOffset()).thenReturn(123);
    when(implementationsPromise.then(any(Operation.class))).thenReturn(implementationsPromise);
    when(javaNavigationService.getImplementations(eq(Path.valueOf("/a")), eq("c.d.file"), eq(123)))
        .thenReturn(implementationsPromise);

    when(implementationDescriptor.getImplementations())
        .thenReturn(Collections.singletonList(type1));
    when(implementationDescriptor.getMemberName()).thenReturn("memberName");
    when(locale.openImplementationWindowTitle(eq("memberName"), eq(1))).thenReturn("foo");

    when(type1.isBinary()).thenReturn(false);
    when(type1.getRootPath()).thenReturn("/memberPath");
    when(appContext.getWorkspaceRoot()).thenReturn(workspaceRoot);
    when(workspaceRoot.getFile(anyString())).thenReturn(realFilePromise);
    when(realFilePromise.then(any(Operation.class))).thenReturn(realFilePromise);

    presenter.show(editor);
    verify(implementationsPromise).then(implementationsOperation.capture());
    implementationsOperation.getValue().apply(implementationDescriptor);

    verify(realFilePromise).then(realFileOperation.capture());
    realFileOperation.getValue().apply(Optional.of(file));

    verify(editorAgent)
        .openEditor(any(VirtualFile.class), any(EditorAgent.OpenEditorCallback.class));
  }

  @Test
  public void testShouldDisplayNoImplementations() throws Exception {
    when(editor.getEditorInput()).thenReturn(editorInput);
    when(editorInput.getFile()).thenReturn(file);
    when(file.getRelatedProject()).thenReturn(Optional.of(relatedProject));
    when(file.getParentWithMarker(eq(SourceFolderMarker.ID))).thenReturn(Optional.of(srcFolder));
    when(file.getLocation()).thenReturn(Path.valueOf("/a/b/c/d/file.java"));
    when(srcFolder.getLocation()).thenReturn(Path.valueOf("/a/b"));
    when(file.getResourceType()).thenReturn(Resource.FILE);
    when(file.getExtension()).thenReturn("java");
    when(file.getName()).thenReturn("file.java");
    when(relatedProject.getLocation()).thenReturn(Path.valueOf("/a"));
    when(editor.getCursorOffset()).thenReturn(123);
    when(implementationsPromise.then(any(Operation.class))).thenReturn(implementationsPromise);
    when(javaNavigationService.getImplementations(eq(Path.valueOf("/a")), eq("c.d.file"), eq(123)))
        .thenReturn(implementationsPromise);

    when(implementationDescriptor.getImplementations()).thenReturn(Collections.emptyList());
    when(implementationDescriptor.getMemberName()).thenReturn("memberName");
    when(locale.openImplementationWindowTitle(eq("memberName"), eq(1))).thenReturn("foo");
    when(type1.getFlags()).thenReturn(-1);

    when(dtoFactory.createDto(eq(Type.class))).thenReturn(type1);
    when(editor.getPositionConverter()).thenReturn(positionConverter);
    when(positionConverter.offsetToPixel(anyInt()))
        .thenReturn(new PositionConverter.PixelCoordinates(1, 1));

    presenter.show(editor);
    verify(implementationsPromise).then(implementationsOperation.capture());
    implementationsOperation.getValue().apply(implementationDescriptor);

    verify(locale, times(2)).noImplementations();
  }
}
