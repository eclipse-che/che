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
package org.eclipse.che.ide.ext.gwt.client.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.gwt.client.GwtResources;
import org.eclipse.che.ide.macro.CurrentProjectPathMacro;
import org.eclipse.che.ide.macro.DevMachineHostNameMacro;

/**
 * GWT command type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class GwtCommandType implements CommandType {

  public static final String COMMAND_TEMPLATE = "mvn clean gwt:run-codeserver";

  private static final String ID = "gwt";

  private final CurrentProjectPathMacro currentProjectPathMacro;
  private final DevMachineHostNameMacro devMachineHostNameMacro;

  private final List<CommandPage> pages;

  @Inject
  public GwtCommandType(
      GwtResources resources,
      GwtCommandPagePresenter page,
      CurrentProjectPathMacro currentProjectPathMacro,
      DevMachineHostNameMacro devMachineHostNameMacro,
      IconRegistry iconRegistry) {
    this.currentProjectPathMacro = currentProjectPathMacro;
    this.devMachineHostNameMacro = devMachineHostNameMacro;
    pages = new LinkedList<>();
    pages.add(page);

    iconRegistry.registerIcon(new Icon("command.type." + ID, resources.gwtCommandType()));
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getDisplayName() {
    return "GWT";
  }

  @Override
  public String getDescription() {
    return "Command for launching GWT Super Dev Mode";
  }

  @Override
  public List<CommandPage> getPages() {
    return pages;
  }

  @Override
  public String getCommandLineTemplate() {
    return COMMAND_TEMPLATE
        + " -f "
        + currentProjectPathMacro.getName()
        + " -Dgwt.bindAddress="
        + devMachineHostNameMacro.getName();
  }

  @Override
  public String getPreviewUrlTemplate() {
    return "";
  }
}
