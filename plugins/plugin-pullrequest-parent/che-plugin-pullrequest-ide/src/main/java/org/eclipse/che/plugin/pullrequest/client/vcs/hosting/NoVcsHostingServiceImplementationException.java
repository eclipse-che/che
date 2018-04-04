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
