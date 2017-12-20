/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.resources.action;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;
import static org.eclipse.che.ide.resource.Path.valueOf;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;

/**
 * Scrolls from resource in the context to the stored location in the Project View.
 *
 * @author Vlad Zhukovskiy
 * @since 4.4.0
 */
@Beta
@Singleton
public class RevealResourceAction extends AbstractPerspectiveAction {

  private static final String PATH = "path";

  private final AppContext appContext;
  private final EventBus eventBus;

  @Inject
  public RevealResourceAction(
      AppContext appContext, EventBus eventBus, CoreLocalizationConstant localizedConstant) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localizedConstant.actionRevealResourceText(),
        localizedConstant.actionRevealResourceDescription());
    this.appContext = appContext;
    this.eventBus = eventBus;
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    final Resource[] resources = appContext.getResources();

    event.getPresentation().setVisible(true);
    event.getPresentation().setEnabled(resources != null && resources.length == 1);
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    Map<String, String> params = e.getParameters();
    if (params != null && params.containsKey(PATH)) {
      String pathToReveal = params.get(PATH);
      Path path = valueOf(pathToReveal);

      checkState(!path.isEmpty());

      eventBus.fireEvent(new RevealResourceEvent(path));
    } else {
      final Resource[] resources = appContext.getResources();

      checkState(resources != null && resources.length == 1);

      eventBus.fireEvent(new RevealResourceEvent(resources[0]));
    }
  }
}
