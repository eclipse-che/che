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
package org.eclipse.che.ide.ext.java.client.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.api.editor.changeintercept.ChangeInterceptorProvider;
import org.eclipse.che.ide.api.editor.changeintercept.CloseCStyleCommentChangeInterceptor;
import org.eclipse.che.ide.api.editor.changeintercept.TextChangeInterceptor;
import org.eclipse.che.ide.ext.java.jdt.JavaPartitions;

/** Provider for {@link TextChangeInterceptor}s for java. */
public class JavaChangeInterceptorProvider implements ChangeInterceptorProvider {
  private final Map<String, List<TextChangeInterceptor>> interceptors = new HashMap<>();

  public JavaChangeInterceptorProvider() {
    // doesn't really need a map but more interceptors should appear
    List<TextChangeInterceptor> defaultTypeInterceptors = new ArrayList<>();

    defaultTypeInterceptors.add(new CloseCStyleCommentChangeInterceptor());
    interceptors.put(JavaPartitions.JAVA_DOC, defaultTypeInterceptors);
  }

  @Override
  public List<TextChangeInterceptor> getInterceptors(final String contentType) {
    return this.interceptors.get(contentType);
  }
}
