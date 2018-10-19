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
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.settings.NodeSettings;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.JarEntry;
import org.eclipse.che.plugin.languageserver.ide.location.HasURI;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;

/**
 * It might be used for any jar content.
 *
 * @author Vlad Zhukovskiy
 */
@Beta
public class JarFileNode extends SyntheticNode<JarEntry>
    implements VirtualFile,
        HasAction,
        FileEventHandler,
        ResourceChangedHandler,
        HasLocation,
        HasURI {

  private final String libId;
  private final Path project;
  private final TextDocumentServiceClient service;
  private final DtoFactory dtoFactory;
  private final JavaResources javaResources;
  private final NodesResources nodesResources;
  private final EditorAgent editorAgent;
  private final EventBus eventBus;

  private HandlerRegistration fileEventHandlerRegistration;
  private HandlerRegistration resourceChangeHandlerRegistration;
  private boolean contentGenerated;

  @Inject
  public JarFileNode(
      @Assisted JarEntry jarEntry,
      @Assisted String libId,
      @Assisted Path project,
      @Assisted NodeSettings nodeSettings,
      TextDocumentServiceClient service,
      DtoFactory dtoFactory,
      JavaResources javaResources,
      NodesResources nodesResources,
      EditorAgent editorAgent,
      EventBus eventBus) {
    super(jarEntry, nodeSettings);
    this.libId = libId;
    this.project = project;
    this.service = service;
    this.dtoFactory = dtoFactory;
    this.javaResources = javaResources;
    this.nodesResources = nodesResources;
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
    // We have to use uri instead of path here despite it looks a bit weird.
    // We are allowed to do this because this path is used as virtual file ID in the editor only.
    // First, sometimes we don't have path field filled in (like in case
    // TextDocumentServiceClient#references)
    // Second, to prevent collision in case if two classes have the same FQN from different jars.
    return Path.valueOf(getData().getUri());
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
    ExternalLibrariesParameters params = dtoFactory.createDto(ExternalLibrariesParameters.class);
    params.setProjectUri(project.toString());
    params.setNodePath(getData().getPath());
    params.setNodeId(libId);
    return service
        .getFileContent(getData().getUri())
        .then((Function<String, String>) result -> result);
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
        getData().getPath(), lineNumber, true, getData().getUri(), getProject().toString());
  }

  @Override
  public String getURI() {
    return getData().getUri();
  }
}
