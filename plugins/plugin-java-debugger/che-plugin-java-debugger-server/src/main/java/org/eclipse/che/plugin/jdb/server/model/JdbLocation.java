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
package org.eclipse.che.plugin.jdb.server.model;

import com.sun.jdi.StackFrame;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Method;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerExtensionService;

/**
 * {@link org.eclipse.che.api.debug.shared.model.Location} implementation for Java debugger.
 *
 * @author Anatolii Bazko
 */
public class JdbLocation implements Location {
  private final Method method;
  private final Location internal;
  private final StackFrame jdiStackFrame;
  private final JavaLanguageServerExtensionService languageServer;

  public JdbLocation(JavaLanguageServerExtensionService languageServer, StackFrame stackFrame) {
    this(languageServer, stackFrame, new JdbMethod(stackFrame));
  }

  /**
   * Intends to create location when thread is not suspended. Information concerning thread and
   * frame are not available.
   */
  public JdbLocation(
      JavaLanguageServerExtensionService languageServer, com.sun.jdi.Location jdiLocation) {
    this.languageServer = languageServer;
    this.internal = getLocation(jdiLocation);
    this.method = null;
    this.jdiStackFrame = null;
  }

  public JdbLocation(
      JavaLanguageServerExtensionService languageServer, StackFrame jdiStackFrame, Method method) {
    this.jdiStackFrame = jdiStackFrame;
    this.languageServer = languageServer;
    this.method = method;
    this.internal = getLocation(jdiStackFrame.location());
  }

  @Override
  public String getTarget() {
    return internal.getTarget();
  }

  @Override
  public int getLineNumber() {
    return internal.getLineNumber();
  }

  @Override
  public boolean isExternalResource() {
    return internal.isExternalResource();
  }

  @Override
  public String getExternalResourceId() {
    return internal.getExternalResourceId();
  }

  @Override
  public String getResourceProjectPath() {
    return internal.getResourceProjectPath();
  }

  @Override
  public Method getMethod() {
    return method;
  }

  @Override
  public long getThreadId() {
    return jdiStackFrame == null ? -1 : jdiStackFrame.thread().uniqueID();
  }

  private Location getLocation(com.sun.jdi.Location jdiLocation) {
    try {
      return languageServer.findResourcesByFqn(
          jdiLocation.declaringType().name(), jdiLocation.lineNumber());
    } catch (Exception e) {
      return new LocationImpl(jdiLocation.declaringType().name(), jdiLocation.lineNumber());
    }
  }
}
