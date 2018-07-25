/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.type.custom;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.machine.MachineResources;

/**
 * Arbitrary command type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CustomCommandType implements CommandType {

  private static final String ID = "custom";
  private static final String COMMAND_TEMPLATE = "echo \"hello\"";

  private final List<CommandPage> pages;

  @Inject
  public CustomCommandType(
      MachineResources resources, IconRegistry iconRegistry, CustomPagePresenter page) {
    pages = new LinkedList<>();
    pages.add(page);

    iconRegistry.registerIcon(new Icon("command.type." + ID, resources.customCommandType()));
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getDisplayName() {
    return "Custom";
  }

  @Override
  public String getDescription() {
    return "Arbitrary command";
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
