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
package org.eclipse.che.ide.ext.plugins.client;

import static java.util.Collections.emptyList;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;

/**
 * 'GWT SDM for Che' command type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class GwtCheCommandType implements CommandType {

  private static final String COMMAND_TEMPLATE =
      "mvn -f /projects/che gwt:codeserver -pl :che-ide-gwt-app -am -Pfast,sdm-in-che";
  private static final String ID = "gwt_sdm_che";

  @Inject
  public GwtCheCommandType(PluginsResources resources, IconRegistry iconRegistry) {

    iconRegistry.registerIcon(new Icon("command.type." + ID, resources.gwtCheCommandType()));
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getDisplayName() {
    return "GWT SDM for Che";
  }

  @Override
  public String getDescription() {
    return "Command for launching GWT Super Dev Mode for the Che project sources";
  }

  @Override
  public List<CommandPage> getPages() {
    return emptyList();
  }

  @Override
  public String getCommandLineTemplate() {
    return COMMAND_TEMPLATE;
  }

  @Override
  public String getPreviewUrlTemplate() {
    return "";
  }
}
