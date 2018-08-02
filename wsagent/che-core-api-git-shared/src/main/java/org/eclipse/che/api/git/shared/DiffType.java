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
package org.eclipse.che.api.git.shared;

/**
 * Type of git diff operation.
 *
 * @author Igor Vinokur
 */
public enum DiffType {
  /** Only names of modified, added, deleted files. */
  NAME_ONLY("--name-only"),
  /**
   * Names staus of modified, added, deleted files.
   *
   * <p>Example:
   *
   * <p>
   *
   * <pre>
   * D   README.txt
   * A   HOW-TO.txt
   * </pre>
   */
  NAME_STATUS("--name-status"),
  RAW("--raw");

  private final String value;

  DiffType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
