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
package org.eclipse.che.api.core.model.factory;

/**
 * Defines factory button attributes.
 *
 * @author Anton Korneta
 */
public interface ButtonAttributes {

  /** Returns factory button color */
  String getColor();

  /** Returns factory button counter */
  Boolean getCounter();

  /** Returns factory button logo */
  String getLogo();

  /** Returns factory button style */
  String getStyle();
}
