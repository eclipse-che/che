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
package org.eclipse.che.plugin.maven.client.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.java.client.MavenResources;

/**
 * Maven command type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MavenCommandType implements CommandType {

  private static final String ID = "mvn";
  private static final String COMMAND_TEMPLATE = "mvn clean install -f ${current.project.path}";

  private final List<CommandPage> pages;

  @Inject
  public MavenCommandType(
      MavenResources resources, MavenCommandPagePresenter page, IconRegistry iconRegistry) {
    pages = new LinkedList<>();
    pages.add(page);

    iconRegistry.registerIcon(new Icon("command.type." + ID, resources.maven()));
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getDisplayName() {
    return "Maven";
  }

  @Override
  public String getDescription() {
    return "Command for executing Maven command line";
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
    return "${server.tomcat8}/${current.project.relpath}";
  }
}
