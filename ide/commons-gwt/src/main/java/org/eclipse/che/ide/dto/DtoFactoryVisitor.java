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
package org.eclipse.che.ide.dto;

/**
 * Visitor pattern. Generally needed to register {@link DtoProvider}s (by generated code) in {@link
 * DtoFactory}. Class, which contains generated code for client side, implements this interface.
 * When all implementations of this interface is instantiated - its {@link #accept(DtoFactory)}
 * method will be called.
 *
 * @author Artem Zatsarynnyi
 */
public interface DtoFactoryVisitor {
  void accept(DtoFactory dtoFactory);
}
