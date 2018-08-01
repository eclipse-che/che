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

import static org.eclipse.che.ide.project.ResolvingProjectStateHolder.ResolvingProjectState.IN_PROGRESS;
import static org.eclipse.che.ide.project.ResolvingProjectStateHolder.ResolvingProjectState.RESOLVED;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathChangedEvent;
import org.eclipse.che.ide.ext.java.shared.dto.HighlightedPosition;
import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;
import org.eclipse.che.ide.project.ResolvingProjectStateHolder;
import org.eclipse.che.ide.project.ResolvingProjectStateHolderRegistry;
import org.eclipse.che.ide.resource.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Roman Nikitenko */
@RunWith(MockitoJUnitRunner.class)
public class JavaReconcilerStrategyTest {
  private static final String FILE_NAME = "TestClass.java";
  private static final String FILE_PATH = "some/path/to/file/TestClass.java";
  private static final String PROJECT_PATH = "some/path/to/project";

  @Mock private EventBus eventBus;
  @Mock private TextEditor editor;
  @Mock private JavaCodeAssistProcessor codeAssistProcessor;
  @Mock private AnnotationModel annotationModel;
  @Mock private HandlerRegistration handlerRegistration;
  @Mock private SemanticHighlightRenderer highlighter;
  @Mock private JavaReconcileClient client;
  @Mock private ReconcileResult reconcileResult;
  @Mock private File file;
  @Mock private ResolvingProjectStateHolder resolvingProjectStateHolder;
  @Mock private ResolvingProjectStateHolderRegistry resolvingProjectStateHolderRegistry;
  @Mock private JavaLocalizationConstant localizationConstant;
  @Mock private JsonRpcPromise<ReconcileResult> reconcileResultPromise;

  @Captor private ArgumentCaptor<Consumer<ReconcileResult>> reconcileResultCaptor;

  @InjectMocks private JavaReconcilerStrategy javaReconcilerStrategy;

  @Before
  public void setUp() throws Exception {
    EditorInput editorInput = mock(EditorInput.class);
    Project project = mock(Project.class);
    when(project.exists()).thenReturn(true);
    Optional<Resource> srcFolder = mock(Optional.class);
    Container startPoint = mock(Container.class);

    when(editor.getEditorInput()).thenReturn(editorInput);
    when(editorInput.getFile()).thenReturn(file);
    when(file.getProject()).thenReturn(project);
    when(file.getLocation()).thenReturn(Path.valueOf(FILE_PATH));

    when(project.getPath()).thenReturn(PROJECT_PATH);
    when(file.getParentWithMarker(nullable(String.class))).thenReturn(srcFolder);
    when(srcFolder.isPresent()).thenReturn(true);
    when(srcFolder.get()).thenReturn(startPoint);
    when(startPoint.getLocation()).thenReturn(Path.valueOf("some/path"));

    when(resolvingProjectStateHolderRegistry.getResolvingProjectStateHolder(nullable(String.class)))
        .thenReturn(resolvingProjectStateHolder);
    when(localizationConstant.codeAssistErrorMessageResolvingProject()).thenReturn("error");

    when(client.reconcile(anyString(), anyString())).thenReturn(reconcileResultPromise);
    when(reconcileResultPromise.onSuccess(
            org.mockito.ArgumentMatchers.<Consumer<ReconcileResult>>any()))
        .thenReturn(reconcileResultPromise);

    javaReconcilerStrategy.setDocument(mock(Document.class));
  }

  @Test
  public void shouldDisableReconcilerWhenResolvingProjectIsInProgress() throws Exception {
    when(resolvingProjectStateHolder.getState()).thenReturn(IN_PROGRESS);

    javaReconcilerStrategy.parse();

    verify(reconcileResultPromise).onSuccess(reconcileResultCaptor.capture());
    reconcileResultCaptor.getValue().accept(reconcileResult);

    verify(reconcileResult, never()).getProblems();
    verify(reconcileResult, never()).getHighlightedPositions();
    verify(codeAssistProcessor, never()).enableCodeAssistant();
    verify(codeAssistProcessor).disableCodeAssistant(anyString());
    verify(highlighter).reconcile(eq(Collections.<HighlightedPosition>emptyList()));
  }

  @Test
  public void shouldDoParseWhenResolvingProjectHasResolved() throws Exception {
    when(resolvingProjectStateHolder.getState()).thenReturn(RESOLVED);
    HighlightedPosition highlightedPosition = mock(HighlightedPosition.class);
    List<HighlightedPosition> positions = new ArrayList<>();
    positions.add(highlightedPosition);
    when(reconcileResult.getHighlightedPositions()).thenReturn(positions);

    javaReconcilerStrategy.parse();

    verify(reconcileResultPromise).onSuccess(reconcileResultCaptor.capture());
    reconcileResultCaptor.getValue().accept(reconcileResult);

    verify(reconcileResult).getProblems();
    verify(reconcileResult).getHighlightedPositions();
    verify(codeAssistProcessor).enableCodeAssistant();
    verify(codeAssistProcessor, never()).disableCodeAssistant(anyString());
    verify(highlighter).reconcile(eq(positions));
  }

  @Test
  public void shouldSkipReconcileResultByFilePath() throws Exception {
    reset(resolvingProjectStateHolder);
    when(reconcileResult.getFileLocation()).thenReturn("some/path/to/stranger/file");

    javaReconcilerStrategy.onReconcileOperation(reconcileResult);

    verify(resolvingProjectStateHolder, never()).getState();
    verify(reconcileResult, never()).getProblems();
    verify(reconcileResult, never()).getHighlightedPositions();
    verify(codeAssistProcessor, never()).enableCodeAssistant();
    verify(codeAssistProcessor, never()).disableCodeAssistant(anyString());
  }

  @Test
  public void shouldApplyReconcileResultAtReconcileOperation() throws Exception {
    when(resolvingProjectStateHolder.getState()).thenReturn(RESOLVED);
    HighlightedPosition highlightedPosition = mock(HighlightedPosition.class);
    List<HighlightedPosition> positions = new ArrayList<>();
    positions.add(highlightedPosition);
    when(reconcileResult.getHighlightedPositions()).thenReturn(positions);
    when(reconcileResult.getFileLocation()).thenReturn(FILE_PATH);

    javaReconcilerStrategy.onReconcileOperation(reconcileResult);

    verify(reconcileResult).getProblems();
    verify(reconcileResult).getHighlightedPositions();
    verify(codeAssistProcessor).enableCodeAssistant();
    verify(codeAssistProcessor, never()).disableCodeAssistant(anyString());
    verify(highlighter).reconcile(eq(positions));
  }

  @Test
  public void shouldDoReconcileWhenClasspathIsChanged() throws Exception {
    ClasspathChangedEvent event = mock(ClasspathChangedEvent.class);
    when(event.getPath()).thenReturn(PROJECT_PATH);

    javaReconcilerStrategy.onClasspathChanged(event);

    verify(client).reconcile(anyString(), anyString());
  }

  @Test
  public void shouldSkipReconcileWhenClasspathIsChangedForAnotherProject() throws Exception {
    ClasspathChangedEvent event = mock(ClasspathChangedEvent.class);
    when(event.getPath()).thenReturn("some/another/project");

    javaReconcilerStrategy.onClasspathChanged(event);

    verify(client, never()).reconcile(anyString(), anyString());
  }
}
