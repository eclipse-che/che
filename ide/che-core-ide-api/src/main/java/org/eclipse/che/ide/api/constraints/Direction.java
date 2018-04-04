/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.constraints;

/**
 * Defines possible directions of an item relative to another item.
 *
 * @author Roman Nikitenko
 */
public enum Direction {

  /** Direction type that specifies the item to be the horizontally at the moment of addition. */
  HORIZONTALLY,

  /** Direction type that specifies the item to be the vertically at the moment of addition. */
  VERTICALLY
}
