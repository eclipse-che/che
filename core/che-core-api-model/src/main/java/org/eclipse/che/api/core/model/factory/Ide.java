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
