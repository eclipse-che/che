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
package org.eclipse.che.ide.console;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Console panel with provided user widget.
 *
 * @author Vlad Zhukovskyi
 * @since 5.18.0
 */
public class CompositeOutputConsole implements OutputConsole {

  private final Widget widget;
  private final String title;
  private final SVGResource icon;

  @Inject
  public CompositeOutputConsole(
      @Assisted Widget widget, @Assisted String title, @Assisted SVGResource icon) {
    this.widget = widget;
    this.title = title;
    this.icon = icon;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(() -> widget);
  }

  @Override
  public SVGResource getTitleIcon() {
    return icon;
  }

  @Override
  public boolean isFinished() {
    return true;
  }

  @Override
  public void stop() {}

  @Override
  public void close() {}

  @Override
  public void addActionDelegate(ActionDelegate actionDelegate) {}
}
