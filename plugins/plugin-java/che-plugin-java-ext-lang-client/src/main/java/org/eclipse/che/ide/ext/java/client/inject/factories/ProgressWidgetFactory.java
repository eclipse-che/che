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
package org.eclipse.che.ide.ext.java.client.inject.factories;

import org.eclipse.che.ide.ext.java.client.progressor.ProgressView;
import org.eclipse.che.ide.ext.java.client.settings.property.PropertyWidget;

/**
 * The factory which creates instances of {@link PropertyWidget}.
 *
 * @author Valeriy Svydenko
 */
public interface ProgressWidgetFactory {
  /**
   * Create new instance of {@link PropertyWidget}
   *
   * @return instance of {@link PropertyWidget}
   */
  ProgressView create();
}
