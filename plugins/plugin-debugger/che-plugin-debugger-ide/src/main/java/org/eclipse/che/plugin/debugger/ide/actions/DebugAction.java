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
package org.eclipse.che.plugin.debugger.ide.actions;

import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;

/**
 * Action that allows to connect to the debugger with the current debug configuration.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DebugAction extends AbstractPerspectiveAction {

  private final DebuggerLocalizationConstant localizationConstants;
  private final DebugConfigurationsManager configurationsManager;

  @Inject
  public DebugAction(
      DebuggerLocalizationConstant localizationConstants,
      DebuggerResources resources,
      DebugConfigurationsManager debugConfigurationsManager) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        localizationConstants.debugActionTitle(),
        localizationConstants.debugActionDescription(),
        resources.debug());
    this.localizationConstants = localizationConstants;
    this.configurationsManager = debugConfigurationsManager;
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    Optional<DebugConfiguration> configurationOptional =
        configurationsManager.getCurrentDebugConfiguration();

    event.getPresentation().setEnabledAndVisible(configurationOptional.isPresent());
    if (configurationOptional.isPresent()) {
      event
          .getPresentation()
          .setText(
              localizationConstants.debugActionTitle()
                  + " '"
                  + configurationOptional.get().getName()
                  + "'");
    }
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {
    Optional<DebugConfiguration> configurationOptional =
        configurationsManager.getCurrentDebugConfiguration();
    if (configurationOptional.isPresent()) {
      configurationsManager.apply(configurationOptional.get());
    }
  }
}
