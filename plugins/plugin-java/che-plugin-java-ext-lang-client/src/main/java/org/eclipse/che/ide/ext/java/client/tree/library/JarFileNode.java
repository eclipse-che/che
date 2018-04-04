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
package org.eclipse.che.ide.ext.java.client.tree.library;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.debug.HasLocation;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.events.FileEvent;
import org.eclipse.che.ide.api.editor.events.FileEvent.FileEventHandler;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.ext.java.shared.dto.ClassContent;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.settings.NodeSettings;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

/**
 * It might be used for any jar content.
 *
 * @author Vlad Zhukovskiy
 */
@Beta
public class JarFileNode extends SyntheticNode<JarEntry>
    implements VirtualFile, HasAction, FileEventHandler, ResourceChangedHandler, HasLocation {

  private final int libId;
  private final Path project;
  private final JavaResources javaResources;
  private final NodesResources nodesResources;
  private final JavaNavigationService service;
  private final EditorAgent editorAgent;
  private final EventBus eventBus;

  private HandlerRegistration fileEventHandlerRegistration;
  private HandlerRegistration resourceChangeHandlerRegistration;
  private boolean contentGenerated;

  @Inject
  public JarFileNode(
      @Assisted JarEntry jarEntry,
      @Assisted int libId,
      @Assisted Path project,
      @Assisted NodeSettings nodeSettings,
      JavaResources javaResources,
      NodesResources nodesResources,
      JavaNavigationService service,
      EditorAgent editorAgent,
      EventBus eventBus) {
    super(jarEntry, nodeSettings);
    this.libId = libId;
    this.project = project;
    this.javaResources = javaResources;
    this.nodesResources = nodesResources;
    this.service = service;
    this.editorAgent = editorAgent;
    this.eventBus = eventBus;

    getAttributes()
        .put(
            CUSTOM_BACKGROUND_FILL,
            singletonList(Style.theme.projectExplorerReadonlyItemBackground()));
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return Promises.resolve(Collections.<Node>emptyList());
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed() {
    if (fileEventHandlerRegistration == null) {
      fileEventHandlerRegistration = eventBus.addHandler(FileEvent.TYPE, this);
    }

    editorAgent.openEditor(this);
  }

  /** {@inheritDoc} */
  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    presentation.setPresentableText(getDisplayName());
    presentation.setPresentableIcon(
        isClassFile() ? javaResources.javaFile() : nodesResources.file());
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public String getName() {
    return getData().getName();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public Path getLocation() {
    return Path.valueOf(getData().getPath());
  }

  /** {@inheritDoc} */
  @Override
  public String getDisplayName() {
    if (isClassFile()) {
      return getData().getName().substring(0, getData().getName().lastIndexOf(".class"));
    } else {
      return getData().getName();
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isReadOnly() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public String getContentUrl() {
    return null;
  }

  public Path getProjectLocation() {
    return project;
  }

  /** {@inheritDoc} */
  @Override
  public Promise<String> getContent() {
    if (libId != -1) {
      return service
          .getContent(project, libId, Path.valueOf(getData().getPath()))
          .then(
              new Function<ClassContent, String>() {
                @Override
                public String apply(ClassContent result) throws FunctionException {
                  JarFileNode.this.contentGenerated = result.isGenerated();
                  return result.getContent();
                }
              });
    } else {
      return service
          .getContent(project, getData().getPath())
          .then(
              new Function<ClassContent, String>() {
                @Override
                public String apply(ClassContent result) throws FunctionException {
                  JarFileNode.this.contentGenerated = result.isGenerated();
                  return result.getContent();
                }
              });
    }
  }

  /** {@inheritDoc} */
  @Override
  public Promise<Void> updateContent(String content) {
    throw new IllegalStateException("Update content on class file is not supported.");
  }

  private boolean isClassFile() {
    return getData().getName().endsWith(".class");
  }

  public boolean isContentGenerated() {
    return contentGenerated;
  }

  @Override
  public Path getProject() {
    return project;
  }

  @Override
  public void onFileOperation(FileEvent event) {
    Path filePath = event.getFile().getLocation();
    Path currentPath = getLocation();

    if (!filePath.equals(currentPath)) {
      return;
    }

    switch (event.getOperationType()) {
      case OPEN:
        {
          if (resourceChangeHandlerRegistration == null) {
            resourceChangeHandlerRegistration =
                eventBus.addHandler(ResourceChangedEvent.getType(), this);
          }

          break;
        }

      case CLOSE:
        {
          if (resourceChangeHandlerRegistration != null) {
            resourceChangeHandlerRegistration.removeHandler();
            resourceChangeHandlerRegistration = null;
          }

          break;
        }
      default:
    }
  }

  @Override
  public void onResourceChanged(ResourceChangedEvent event) {
    ResourceDelta delta = event.getDelta();
    Path resourceLocation = delta.getResource().getLocation();

    if (REMOVED == delta.getKind() && project.equals(resourceLocation)) {
      EditorPartPresenter editorPart = editorAgent.getOpenedEditor(getLocation());
      editorAgent.closeEditor(editorPart);

      removeHandlers();
    }
  }

  private void removeHandlers() {
    if (fileEventHandlerRegistration != null) {
      fileEventHandlerRegistration.removeHandler();
      fileEventHandlerRegistration = null;
    }

    if (resourceChangeHandlerRegistration != null) {
      resourceChangeHandlerRegistration.removeHandler();
      resourceChangeHandlerRegistration = null;
    }
  }

  @Override
  public Location toLocation(int lineNumber) {
    return new LocationImpl(
        getLocation().toString(), lineNumber, true, libId, getProject().toString());
  }
}
