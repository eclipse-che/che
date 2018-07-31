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
package org.eclipse.che.plugin.testing.ide;

import java.util.Arrays;
import org.mockito.Mock;
import org.mockito.internal.debugging.MockitoDebuggerImpl;

/**
 * Interface that allows printing the invocations made on all mocked / spied fields.
 *
 * @author David Festal
 */
public interface MockitoPrinter {
  default void printInvocationsOnAllMockedFields() {
    new MockitoDebuggerImpl()
        .printInvocations(
            Arrays.asList(this.getClass().getDeclaredFields())
                .stream()
                .filter(
                    field -> {
                      return field.isAnnotationPresent(Mock.class);
                    })
                .map(
                    field -> {
                      try {
                        field.setAccessible(true);
                        return field.get(this);
                      } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                      }
                    })
                .filter(field -> field != null)
                .toArray(Object[]::new));
  }
}
