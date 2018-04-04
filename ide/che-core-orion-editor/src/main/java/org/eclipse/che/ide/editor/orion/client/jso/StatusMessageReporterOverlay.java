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
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;
import org.eclipse.che.ide.status.message.StatusMessageReporter;

/**
 * Overlay for status message reporter function which sends messages about editor status.
 *
 * @author Alexander Andrienko
 */
public class StatusMessageReporterOverlay extends JavaScriptObject {

  protected StatusMessageReporterOverlay() {}

  /**
   * Create StatusMessageReporterOverlay for delegation status message reporting to class {@link
   * StatusMessageReporter}.
   *
   * @param messageReporter delegate to report message status of the editor.
   */
  public static final native StatusMessageReporterOverlay create(
      StatusMessageReporter messageReporter) /*-{
        return function (message, type, isAccessible) {
            messageReporter.@org.eclipse.che.ide.status.message.StatusMessageReporter::notifyObservers(*)(message, type, isAccessible)
        };
    }-*/;
}
