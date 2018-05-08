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
package org.eclipse.che.ide.command.editor.page.renderer;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.ide.api.command.CommandImpl.ApplicableContext;
import org.eclipse.che.ide.api.console.OutputConsoleRenderer;
import org.eclipse.che.ide.api.console.OutputConsoleRendererRegistry;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;

/** Presenter for {@link CommandEditorPage} which allows to edit command's applicable renderers. */
public class RenderersPage extends AbstractCommandEditorPage
    implements RenderersPageView.ActionDelegate, ResourceChangedHandler {

  private final RenderersPageView view;
  private final OutputConsoleRendererRegistry rendererRegistry;

  /** Initial value of the applicable projects list. */
  private Set<String> applicableRenderersInitial;

  @Inject
  public RenderersPage(
      RenderersPageView view,
      EditorMessages messages,
      EventBus eventBus,
      OutputConsoleRendererRegistry rendererRegistry) {
    super(messages.pageRenderersTitle());

    this.view = view;
    this.rendererRegistry = rendererRegistry;

    eventBus.addHandler(ResourceChangedEvent.getType(), this);

    view.setDelegate(this);
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  protected void initialize() {
    final ApplicableContext context = editedCommand.getApplicableContext();
    Set<String> applicableRenderers = context.getApplicableOutputRenderers();

    if (applicableRenderers == null) {
      applicableRenderers =
          rendererRegistry.getCommandDefaultOutputRenderers(editedCommand.getType());

      // Add default renderers to the context
      applicableRenderers.forEach(c -> context.addOutputRenderer(c));
    }

    applicableRenderersInitial = new HashSet<>(applicableRenderers);

    refreshRenderers();
  }

  /** Refresh 'Renderers' section in the view. */
  private void refreshRenderers() {
    final Map<OutputConsoleRenderer, Boolean> renderersStates = new HashMap<>();
    ApplicableContext context = editedCommand.getApplicableContext();
    Set<String> applicableRenderers = context.getApplicableOutputRenderers();

    rendererRegistry
        .getAllOutputRenderers()
        .forEach(
            c -> {
              renderersStates.put(
                  c, applicableRenderers != null && applicableRenderers.contains(c.getName()));
            });
    view.setRenderers(renderersStates);
  }

  @Override
  public boolean isDirty() {
    if (editedCommand == null) {
      return false;
    }

    ApplicableContext context = editedCommand.getApplicableContext();

    return !(applicableRenderersInitial.equals(context.getApplicableOutputRenderers()));
  }

  @Override
  public void onApplicableRendererChanged(OutputConsoleRenderer renderer, boolean applicable) {
    final ApplicableContext context = editedCommand.getApplicableContext();

    if (applicable) {
      // if command is bound with one project at least
      // then remove command from the workspace
      if (context.getApplicableProjects().isEmpty()) {
        context.setWorkspaceApplicable(false);
      }

      context.addOutputRenderer(renderer.getName());
    } else {
      context.removeOutputRenderer(renderer.getName());
    }

    editedCommand.setOutputRenderers(context.getApplicableOutputRenderers());
    notifyDirtyStateChanged();
  }

  @Override
  public void onResourceChanged(ResourceChangedEvent event) {
    final ResourceDelta delta = event.getDelta();
    final Resource resource = delta.getResource();

    if (resource.isProject()) {
      // defer refreshing the projects section since appContext#getRenderers may return old data
      Scheduler.get().scheduleDeferred(this::refreshRenderers);
    }
  }
}
