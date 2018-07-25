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
package org.eclipse.che.api.git.params;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.git.shared.AddRequest;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#add(AddParams)}.
 *
 * @author Igor Vinokur
 */
public class AddParams {

  private List<String> filePattern;
  private boolean isUpdate;

  private AddParams() {}

  /**
   * Create new {@link AddParams} instance.
   *
   * @param filePattern file pattern of files to add
   */
  public static AddParams create(List<String> filePattern) {
    return new AddParams().withFilePattern(filePattern);
  }

  /** Create new {@link AddParams} instance */
  public static AddParams create() {
    return new AddParams();
  }

  /** @see AddRequest#getFilePattern() */
  public List<String> getFilePattern() {
    return filePattern == null ? new ArrayList<>() : filePattern;
  }

  /** @see AddRequest#withFilePattern(List) */
  public AddParams withFilePattern(List<String> filePattern) {
    this.filePattern = filePattern;
    return this;
  }

  /** @see AddRequest#isUpdate() * */
  public boolean isUpdate() {
    return isUpdate;
  }

  /** @see AddRequest#withUpdate(boolean) * */
  public AddParams withUpdate(boolean isUpdate) {
    this.isUpdate = isUpdate;
    return this;
  }
}
