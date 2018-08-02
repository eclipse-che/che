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
package org.eclipse.che.plugin.pullrequest.client.vcs.hosting;

/**
 * Exception raised when there is no {@link VcsHostingService} implementation for the current
 * project.
 *
 * @author Kevin Pollet
 */
public class NoVcsHostingServiceImplementationException extends Exception {

  private static final long serialVersionUID = 1L;

  /** Constructs an instance of {@link NoVcsHostingServiceImplementationException}. */
  public NoVcsHostingServiceImplementationException() {
    super("No implementation of the VcsHostingService for the current project");
  }
}
