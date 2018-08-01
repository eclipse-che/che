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
package org.eclipse.che.ide.api.resources;

/**
 * Represents mechanism for getting and setting value of the content stamp. This value is changed
 * when file is modified.
 *
 * @author Valeriy Svydenko
 */
public interface ModificationTracker {

  /**
   * Sets modification tracker value.
   *
   * @param stamp encoded content
   */
  void setModificationStamp(String stamp);

  /**
   * Gets modification tracker value. Modification tracker is a value is changed by any modification
   * of the content of the file.
   *
   * @return modification tracker value
   */
  String getModificationStamp();

  /**
   * Update modification tracker value by content. Modification tracker is a value is changed by any
   * modification of the content of the file.
   *
   * @param content actual file content
   */
  void updateModificationStamp(String content);
}
