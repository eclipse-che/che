/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.model.impl.devfile;

import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.eclipse.che.api.core.model.workspace.devfile.PreviewUrl;

@Embeddable
public class PreviewUrlImpl implements PreviewUrl {

  @Column(name = "preview_url_port")
  private int port;

  @Column(name = "preview_url_path")
  private String path;

  public PreviewUrlImpl() {}

  public PreviewUrlImpl(PreviewUrl previewUrl) {
    this(previewUrl.getPort(), previewUrl.getPath());
  }

  public PreviewUrlImpl(int port, String path) {
    this.port = port;
    this.path = path;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", PreviewUrlImpl.class.getSimpleName() + "[", "]")
        .add("port=" + port)
        .add("path='" + path + "'")
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PreviewUrlImpl that = (PreviewUrlImpl) o;
    return port == that.port && Objects.equals(path, that.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(port, path);
  }
}
