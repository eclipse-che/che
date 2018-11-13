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
package org.eclipse.che.ide.ext.java.client.navigation.filestructure;

/**
 * Interface to signal selection of an element
 *
 * @author Thomas Mäder
 * @param <T> the type of element selected.
 */
public interface ElementSelectionDelegate<T> {
  void onSelect(T element);

  void onCancel();
}
