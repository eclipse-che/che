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
package org.eclipse.che.ide.ext.plugins.client.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.plugins.client.PluginsResources;

/**
 * 'GWT SDM for Che' command type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class GwtCheCommandType implements CommandType {

  public static final String CODE_SERVER_FQN = "com.google.gwt.dev.codeserver.CodeServer";
  public static final String COMMAND_TEMPLATE =
      "mvn -Psdm -pl :assembly-ide-war -am clean gwt:codeserver";

  private static final String ID = "gwt_sdm_che";

  private final List<CommandPage> pages;

  @Inject
  public GwtCheCommandType(
      PluginsResources resources, GwtCheCommandPagePresenter page, IconRegistry iconRegistry) {
    pages = new LinkedList<>();
    pages.add(page);

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
    return pages;
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
