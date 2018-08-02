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
package org.eclipse.che.commons.xml;

/**
 * Describes qualified name
 *
 * @author Eugene Voevodin
 */
public class QName {

  private String prefix;
  private String localName;

  public QName(String name) {
    applyName(name);
  }

  public String getPrefix() {
    return prefix;
  }

  public String getLocalName() {
    return localName;
  }

  public String getName() {
    return hasPrefix() ? prefix + ':' + localName : localName;
  }

  public boolean hasPrefix() {
    return prefix != null && !prefix.isEmpty();
  }

  private void applyName(String newName) {
    final int separator = newName.indexOf(':');
    if (separator != -1) {
      localName = newName.substring(separator + 1);
      prefix = newName.substring(0, separator);
    } else {
      localName = newName;
    }
  }
}
