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
package org.eclipse.che.ide.ext.java.client.navigation.openimplementation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.Collections;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.position.PositionConverter.PixelCoordinates;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.ext.java.dto.DtoClientImpls.ImplementersResponseDto;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.ui.popup.PopupResources.PopupStyle;
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

/** @author Valeriy Svydenko */
@RunWith(GwtMockitoTestRunner.class)
public class OpenImplementationPresenterTest {
  // constructor mocks
  @Mock private JavaLanguageExtensionServiceClient javaLanguageExtensionServiceClient;
  @Mock private OpenFileInEditorHelper openHelper;
  @Mock private JavaResources javaResources;
  @Mock private PopupStyle popupStyle;
  @Mock private PopupResources popupResources;
  @Mock private JavaLocalizationConstant locale;

  // other mocks

  @Mock private TextEditor editor;
  @Mock private EditorInput editorInput;
  @Mock private File file;
  @Mock private Project relatedProject;
  @Mock private Container srcFolder;
  @Mock private ImplementersResponseDto implementationDescriptor;
  @Mock private SymbolInformation type;
  @Mock private PositionConverter positionConverter;

  @Mock private Promise<ImplementersResponse> implementersPromise;

  @Captor ArgumentCaptor<Operation<ImplementersResponse>> implementationsOperation;

  private OpenImplementationPresenter presenter;

  @Before
  public void setUp() throws Exception {
    when(popupResources.popupStyle()).thenReturn(popupStyle);
    when(popupStyle.header()).thenReturn("header");
    when(popupStyle.item()).thenReturn("item");
    when(popupStyle.popup()).thenReturn("popup");
    when(popupStyle.body()).thenReturn("body");
    presenter =
        new OpenImplementationPresenter(
            javaLanguageExtensionServiceClient, javaResources, popupResources, locale, openHelper);
  }

  @Test
  public void testShouldDisplayOneImplementationIsRealFile() throws Exception {
    when(editor.getEditorInput()).thenReturn(editorInput);
    when(editorInput.getFile()).thenReturn(file);
    when(file.getLocation()).thenReturn(Path.valueOf("/a/b/c/d/file.java"));
    when(srcFolder.getLocation()).thenReturn(Path.valueOf("/a/b"));
    when(file.getResourceType()).thenReturn(Resource.FILE);
    when(file.getExtension()).thenReturn("java");
    when(file.getName()).thenReturn("file.java");
    when(relatedProject.getLocation()).thenReturn(Path.valueOf("/a"));
    when(editor.getCursorPosition()).thenReturn(new TextPosition(1, 1));
    when(editor.getCursorOffset()).thenReturn(123);
    when(editor.getPositionConverter()).thenReturn(positionConverter);
    when(implementersPromise.then(any(Operation.class))).thenReturn(implementersPromise);
    when(javaLanguageExtensionServiceClient.findImplementations(any()))
        .thenReturn(implementersPromise);

    when(implementationDescriptor.getImplementers()).thenReturn(Collections.singletonList(type));
    when(implementationDescriptor.getSearchedElement()).thenReturn("memberName");
    when(locale.openImplementationWindowTitle(eq("memberName"), eq(1))).thenReturn("foo");

    when(type.getKind()).thenReturn(SymbolKind.Class);
    when(type.getLocation()).thenReturn(new Location("/memberPath", mock(Range.class)));

    presenter.show(editor);
    verify(implementersPromise).then(implementationsOperation.capture());
    implementationsOperation.getValue().apply(implementationDescriptor);

    verify(openHelper).openLocation(eq(type.getLocation()));
  }

  @Test
  public void testShouldDisplayNoImplementations() throws Exception {
    when(editor.getEditorInput()).thenReturn(editorInput);
    when(editorInput.getFile()).thenReturn(file);

    when(file.getLocation()).thenReturn(Path.valueOf("/a/b/c/d/file.java"));
    when(srcFolder.getLocation()).thenReturn(Path.valueOf("/a/b"));
    when(file.getResourceType()).thenReturn(Resource.FILE);
    when(file.getExtension()).thenReturn("java");
    when(file.getName()).thenReturn("file.java");
    when(relatedProject.getLocation()).thenReturn(Path.valueOf("/a"));
    when(editor.getCursorPosition()).thenReturn(new TextPosition(1, 1));
    when(editor.getCursorOffset()).thenReturn(123);
    when(editor.getPositionConverter()).thenReturn(positionConverter);
    when(positionConverter.offsetToPixel(eq(123))).thenReturn(new PixelCoordinates(1, 1));
    when(implementersPromise.then(any(Operation.class))).thenReturn(implementersPromise);
    when(javaLanguageExtensionServiceClient.findImplementations(any()))
        .thenReturn(implementersPromise);

    when(implementationDescriptor.getImplementers()).thenReturn(Collections.emptyList());
    when(implementationDescriptor.getSearchedElement()).thenReturn("memberName");
    when(locale.openImplementationWindowTitle(eq("memberName"), eq(1))).thenReturn("foo");

    presenter.show(editor);
    verify(implementersPromise).then(implementationsOperation.capture());
    implementationsOperation.getValue().apply(implementationDescriptor);

    verify(locale).noImplementations();
  }
}
