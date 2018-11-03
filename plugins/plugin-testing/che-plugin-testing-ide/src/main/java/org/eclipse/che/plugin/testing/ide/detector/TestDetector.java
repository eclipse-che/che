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
package org.eclipse.che.plugin.testing.ide.detector;

import static org.eclipse.che.api.testing.shared.TestExecutionContext.ContextType.CURSOR_POSITION;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.che.api.testing.shared.TestDetectionContext;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestExecutionContext.ContextType;
import org.eclipse.che.api.testing.shared.TestPosition;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;

/** The mechanism for checking if active editor contains tests. */
@Singleton
public class TestDetector {
  private boolean isEnable;
  private boolean isEditorInFocus;
  private List<TestPosition> testPosition;
  private TextEditor currentEditor;
  private TestServiceClient client;
  private NotificationManager notificationManager;
  private DtoFactory dtoFactory;
  private AppContext appContext;

  private PartPresenter activePart;
  private TestExecutionContext.ContextType contextType;

  @Inject
  public TestDetector(
      EventBus eventBus,
      Set<TestFileExtension> fileExtensions,
      TestServiceClient client,
      NotificationManager notificationManager,
      DtoFactory dtoFactory,
      AppContext appContext) {
    this.client = client;
    this.notificationManager = notificationManager;
    this.dtoFactory = dtoFactory;
    this.appContext = appContext;

    isEnable = false;

    Set<String> collectedExtensions =
        fileExtensions
            .stream()
            .map(TestFileExtension::getExtensions)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

    eventBus.addHandler(
        ActivePartChangedEvent.TYPE,
        event -> analyzeActiveEditor(collectedExtensions, event.getActivePart()));
  }

  /*returns true if the editor contains tests otherwise returns false*/
  public boolean isEnabled() {
    return isEnable;
  }

  /*returns true if the editor is active otherwise returns false*/
  public boolean isEditorInFocus() {
    return isEditorInFocus;
  }

  /*returns instance of {@link TestPosition} which describes current test*/
  public List<TestPosition> getTestPosition() {
    return testPosition;
  }

  /*returns active editor*/
  public TextEditor getCurrentEditor() {
    return currentEditor;
  }

  /*returns active part*/
  public PartPresenter getActivePart() {
    return activePart;
  }

  /*returns instance of {@link ContextType}*/
  public ContextType getContextType() {
    return contextType;
  }

  /*updates context type*/
  public void setContextType(ContextType contextType) {
    this.contextType = contextType;
  }

  private void analyzeActiveEditor(Set<String> collectedExtensions, PartPresenter activePart) {
    this.activePart = activePart;
    if (activePart instanceof TextEditor) {
      isEditorInFocus = true;
      contextType = CURSOR_POSITION;
      TextEditor activeEditor = (TextEditor) activePart;
      String fileName = activeEditor.getEditorInput().getFile().getName();
      String fileExtension = fileName.substring(fileName.indexOf('.'));
      if (collectedExtensions.contains(fileExtension)) {
        detectTests(activeEditor);
      } else {
        isEnable = false;
      }
    } else {
      isEditorInFocus = false;
    }
  }

  private void detectTests(TextEditor editor) {
    this.currentEditor = editor;
    TestDetectionContext context = dtoFactory.createDto(TestDetectionContext.class);
    context.setFilePath(currentEditor.getEditorInput().getFile().getLocation().toString());
    context.setOffset(currentEditor.getCursorOffset());
    Resource resource = appContext.getResource();
    Project project =
        (resource == null || resource.getProject() == null)
            ? appContext.getRootProject()
            : resource.getProject();
    context.setProjectPath(project.getPath());
    client
        .detectTests(context)
        .onSuccess(
            testDetectionResult -> {
              isEnable = testDetectionResult.isTestFile();
              testPosition = testDetectionResult.getTestPosition();
            })
        .onFailure(
            jsonRpcError -> {
              Log.error(getClass(), jsonRpcError);
              isEnable = false;
              notificationManager.notify("Can't detect test methods");
            });
  }
}
