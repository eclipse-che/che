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
package org.eclipse.che.plugin.debugger.ide.configuration;

import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.Collections;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;

/**
 * Action for selecting (changing) current debug configuration.
 *
 * @author Artem Zatsarynnyi
 */
public class DebugConfigurationAction extends AbstractPerspectiveAction {

  private final DebugConfigurationsManager configurationsManager;
  private final DebugConfiguration configuration;

  @Inject
  public DebugConfigurationAction(
      DebugConfigurationsManager debugConfigurationsManager,
      @Assisted DebugConfiguration configuration,
      DebuggerLocalizationConstant localizationConstants) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        configuration.getName(),
        localizationConstants.debugConfigurationActionDescription());
    configurationsManager = debugConfigurationsManager;
    this.configuration = configuration;
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    Optional<DebugConfiguration> configurationOptional =
        configurationsManager.getCurrentDebugConfiguration();
    boolean isCurrentConfig =
        configurationOptional.isPresent() && configuration.equals(configurationOptional.get());

    event.getPresentation().setEnabledAndVisible(!isCurrentConfig);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    configurationsManager.setCurrentDebugConfiguration(configuration);
    configurationsManager.apply(configuration);
  }
}
