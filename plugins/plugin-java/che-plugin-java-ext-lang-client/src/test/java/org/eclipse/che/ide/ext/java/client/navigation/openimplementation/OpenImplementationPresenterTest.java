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

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.ext.java.dto.DtoClientImpls.ImplementersResponseDto;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.jdt.ls.extension.api.dto.ImplementersResponse;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
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
  @Mock private JavaLanguageExtensionServiceClient javaLanguageExtensionServiceClient;
  @Mock private AppContext appContext;
  @Mock private EditorAgent editorAgent;
  @Mock private OpenFileInEditorHelper openHelper;
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
  @Mock private ImplementersResponseDto implementationDescriptor;
  @Mock private SymbolInformation type1;
  @Mock private SymbolInformation type2;
  @Mock private JarEntry jarEntry;
  @Mock private Container workspaceRoot;
  @Mock private PositionConverter positionConverter;
  @Mock private SVGResource svgResource;
  @Mock private OMSVGSVGElement omsvgsvgElement;

  @Mock private Promise<ImplementersResponse> implementersPromise;
  @Mock private Promise<JarEntry> jarEntryPromise;
  @Mock private Promise<String> contentPromise;
  @Mock private Promise<Optional<File>> realFilePromise;

  @Captor ArgumentCaptor<Operation<ImplementersResponse>> implementationsOperation;
  @Captor ArgumentCaptor<Operation<JarEntry>> jarEntryOperation;
  @Captor ArgumentCaptor<Operation<String>> contentOperation;
  @Captor ArgumentCaptor<Operation<Optional<File>>> realFileOperation;

  private OpenImplementationPresenter presenter;

  @Before
  public void setUp() throws Exception {
    presenter =
        new OpenImplementationPresenter(
            javaLanguageExtensionServiceClient, javaResources, popupResources, locale, openHelper);
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
    // when(editor.getCursorOffset()).thenReturn(123);
    when(editor.getCursorPosition()).thenReturn(new TextPosition(12, 3));
    when(implementersPromise.then(any(Operation.class))).thenReturn(implementersPromise);
    when(javaLanguageExtensionServiceClient.findImplementations(
            any()
            /*new TextDocumentPositionParams(new TextDocumentIdentifier(file.getLocation().toString()),
            new Position(
                1,
                1))*/ ))
        .thenReturn(implementersPromise);

    when(implementationDescriptor.getImplementers()).thenReturn(Collections.singletonList(type1));
    when(implementationDescriptor.getSearchedElement()).thenReturn("memberName");
    when(locale.openImplementationWindowTitle(eq("memberName"), eq(1))).thenReturn("foo");

    when(type1.getKind()).thenReturn(SymbolKind.Class);
    when(type1.getLocation()).thenReturn(new Location("/memberPath", mock(Range.class)));
    // when(appContext.getWorkspaceRoot()).thenReturn(workspaceRoot);
    // when(workspaceRoot.getFile(anyString())).thenReturn(realFilePromise);
    // when(realFilePromise.then(any(Operation.class))).thenReturn(realFilePromise);

    presenter.show(editor);
    verify(implementersPromise).then(implementationsOperation.capture());
    implementationsOperation.getValue().apply(implementationDescriptor);

    // verify(realFilePromise).then(realFileOperation.capture());
    // realFileOperation.getValue().apply(Optional.of(file));

    verify(openHelper).openLocation(any(Location.class));
  }

  //  @Test
  //  public void testShouldDisplayNoImplementations() throws Exception {
  //    when(editor.getEditorInput()).thenReturn(editorInput);
  //    when(editorInput.getFile()).thenReturn(file);
  //    when(file.getRelatedProject()).thenReturn(Optional.of(relatedProject));
  //
  // when(file.getParentWithMarker(eq(SourceFolderMarker.ID))).thenReturn(Optional.of(srcFolder));
  //    when(file.getLocation()).thenReturn(Path.valueOf("/a/b/c/d/file.java"));
  //    when(srcFolder.getLocation()).thenReturn(Path.valueOf("/a/b"));
  //    when(file.getResourceType()).thenReturn(Resource.FILE);
  //    when(file.getExtension()).thenReturn("java");
  //    when(file.getName()).thenReturn("file.java");
  //    when(relatedProject.getLocation()).thenReturn(Path.valueOf("/a"));
  //    when(editor.getCursorOffset()).thenReturn(123);
  //    when(implementersPromise.then(any(Operation.class))).thenReturn(implementersPromise);
  //    when(javaLanguageExtensionServiceClient.findImplementations(eq(Path.valueOf("/a")),
  // eq("c.d.file"),
  //     eq(123)))
  //            .thenReturn(implementersPromise);
  //
  //    when(implementationDescriptor.getImplementers()).thenReturn(Collections.emptyList());
  //    when(implementationDescriptor.getSearchedElement()).thenReturn("memberName");
  //    when(locale.openImplementationWindowTitle(eq("memberName"), eq(1))).thenReturn("foo");
  //    when(type1.getFlags()).thenReturn(-1);
  //
  //    when(dtoFactory.createDto(eq(Type.class))).thenReturn(type1);
  //    when(editor.getPositionConverter()).thenReturn(positionConverter);
  //    when(positionConverter.offsetToPixel(anyInt()))
  //        .thenReturn(new PositionConverter.PixelCoordinates(1, 1));
  //
  //    presenter.show(editor);
  //    verify(implementersPromise).then(implementationsOperation.capture());
  //    implementationsOperation.getValue().apply(implementationDescriptor);
  //
  //    verify(locale, times(2)).noImplementations();
  //  }
}
