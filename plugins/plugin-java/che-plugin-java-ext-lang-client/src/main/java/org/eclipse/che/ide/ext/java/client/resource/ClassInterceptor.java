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
package org.eclipse.che.ide.ext.java.client.resource;

import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaFile;

import com.google.inject.Singleton;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceInterceptor;
import org.eclipse.che.ide.api.resources.marker.PresentableTextMarker;

/**
 * Intercept java based files (.java), cut extension and adds the marker which is responsible for
 * displaying presentable text to the corresponding resource.
 *
 * @author Vlad Zhukovskiy
 * @since 4.4.0
 */
@Singleton
public class ClassInterceptor implements ResourceInterceptor {

  /** {@inheritDoc} */
  @Override
  public void intercept(Resource resource) {
    if (resource.isFile() && isJavaFile(resource)) {
      resource.addMarker(new PresentableTextMarker(((File) resource).getNameWithoutExtension()));
    }
  }
}
