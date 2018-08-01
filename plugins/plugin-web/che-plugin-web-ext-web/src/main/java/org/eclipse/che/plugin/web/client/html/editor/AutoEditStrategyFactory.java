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
package org.eclipse.che.plugin.web.client.html.editor;

import org.eclipse.che.ide.api.editor.changeintercept.TextChangeInterceptor;

/**
 * Allows to define a new AutoEditStrategy based on text editor and content type.
 *
 * @author Florent Benoit
 */
public interface AutoEditStrategyFactory {

  /**
   * Build a new instance
   *
   * @return a new strategy
   */
  TextChangeInterceptor build(String contentType);
}
