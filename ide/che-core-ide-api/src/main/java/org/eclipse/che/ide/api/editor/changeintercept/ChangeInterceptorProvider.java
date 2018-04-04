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
package org.eclipse.che.ide.api.editor.changeintercept;

import java.util.List;

/** Component that associates {@link TextChangeInterceptor}s to a content type. */
public interface ChangeInterceptorProvider {

  /**
   * Returns the change interceptors for the content type.
   *
   * @param contentType the content type
   * @return the change interceptors
   */
  List<TextChangeInterceptor> getInterceptors(String contentType);
}
