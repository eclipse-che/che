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
package org.eclipse.che.ide.console;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.name.Named;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Artem Zatsarynnyi */
public interface CommandConsoleFactory {

  /** Create the instance of {@link CommandOutputConsole} for the given {@code command}. */
  @Named("command")
  CommandOutputConsole create(CommandImpl command, String machineName);

  /** Create the instance of {@link DefaultOutputConsole} for the given title. */
  @Named("default")
  OutputConsole create(String title);

  @Named("composite")
  CompositeOutputConsole create(Widget widget, String title, SVGResource icon);
}
