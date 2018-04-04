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
