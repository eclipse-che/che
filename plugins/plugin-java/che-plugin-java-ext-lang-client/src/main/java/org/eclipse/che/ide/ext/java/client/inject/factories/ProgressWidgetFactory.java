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
