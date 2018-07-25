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
package org.eclipse.che.ide.ext.java.client.resource;

import com.google.common.annotations.Beta;
import org.eclipse.che.ide.api.resources.marker.Marker;
import org.eclipse.che.ide.ext.java.shared.ContentRoot;

/** @author Vlad Zhukovskiy */
@Beta
public class SourceFolderMarker implements Marker {

  public static final String ID = "javaSourceFolderMarker";

  private final ContentRoot contentRoot;

  public SourceFolderMarker(ContentRoot contentRoot) {

    this.contentRoot = contentRoot;
  }

  @Override
  public String getType() {
    return ID;
  }

  public ContentRoot getContentRoot() {
    return contentRoot;
  }
}
