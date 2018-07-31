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
package org.eclipse.che.ide.ext.git.client.merge;

import java.util.List;

/**
 * Git reference bean.
 *
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Jul 20, 2011 2:41:39 PM anya $
 */
public class Reference {
  enum RefType {
    LOCAL_BRANCH,
    REMOTE_BRANCH,
    TAG;
  }

  /** Short name of the reference to display. */
  private String displayName;

  /** Full name of the reference. */
  private String fullName;

  /** Type of the reference. */
  private RefType refType;

  private List<Reference> branches;

  /**
   * @param fullName full name of the reference
   * @param displayName short name of the reference to display
   * @param refType type the reference
   */
  public Reference(String fullName, String displayName, RefType refType) {
    this.displayName = displayName;
    this.fullName = fullName;
    this.refType = refType;
  }

  /** @return the displayName */
  public String getDisplayName() {
    return displayName;
  }

  /** @return the fullName */
  public String getFullName() {
    return fullName;
  }

  /** @return the refType */
  public RefType getRefType() {
    return refType;
  }

  /** @return the branches */
  public List<Reference> getBranches() {
    return branches;
  }

  /** @param branches the branches to set */
  public void setBranches(List<Reference> branches) {
    this.branches = branches;
  }
}
