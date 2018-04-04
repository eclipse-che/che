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
package org.eclipse.che.api.core.model.factory;

/**
 * Defines the contract for the factory IDE instance.
 *
 * @author Anton Korneta
 */
public interface Ide {

  /** Returns configuration of IDE on application loaded event */
  OnAppLoaded getOnAppLoaded();

  /** Returns configuration of IDE on application closed event */
  OnAppClosed getOnAppClosed();

  /** Returns configuration of IDE on projects loaded event */
  OnProjectsLoaded getOnProjectsLoaded();
}
