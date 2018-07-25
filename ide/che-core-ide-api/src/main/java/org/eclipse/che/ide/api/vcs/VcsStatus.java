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
package org.eclipse.che.ide.api.vcs;

import static java.util.Arrays.stream;

import org.eclipse.che.ide.api.theme.Style;

public enum VcsStatus {
  UNTRACKED("untracked"),
  ADDED("added"),
  MODIFIED("modified"),
  NOT_MODIFIED("not_modified");

  private String value;

  VcsStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public String getColor() {
    switch (this) {
      case UNTRACKED:
        return Style.getVcsStatusUntrackedColor();
      case MODIFIED:
        return Style.getVcsStatusModifiedColor();
      case ADDED:
        return Style.getVcsStatusAddedColor();
      case NOT_MODIFIED:
        return null;
      default:
        return null;
    }
  }

  public static VcsStatus from(String value) {
    return stream(VcsStatus.values())
        .filter(vcsStatus -> vcsStatus.getValue().equals(value.toLowerCase()))
        .findAny()
        .orElse(null);
  }
}
