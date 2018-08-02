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
package org.eclipse.che.ide.ui.smartTree;

import com.google.gwt.view.client.ProvidesKey;

/**
 * ModelKeyProviders are responsible for returning a unique key for a given model.
 *
 * @param <T> the model type
 * @author Vlad Zhukovskyi
 */
public interface UniqueKeyProvider<T> extends ProvidesKey<T> {

  /**
   * Gets a non-null key value that maps to this object. Keys must be consistent and unique for a
   * given model, as a database primary key would be used.
   *
   * @return non-null {@link String} key for {@link T} object
   */
  String getKey(T item);
}
