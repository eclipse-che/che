/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.filestructure;

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
