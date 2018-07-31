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
package org.eclipse.che.api.core.factory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provide factory parameter compatibility options.
 *
 * @author Alexander Garagatyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FactoryParameter {
  enum Obligation {
    MANDATORY,
    OPTIONAL
  }

  enum Version {
    // NEVER must be the last defined constant
    V4_0,
    NEVER;

    public static Version fromString(String v) {
      if (null != v) {
        switch (v) {
          case "4.0":
            return V4_0;
        }
      }

      throw new IllegalArgumentException("Unknown version " + v + ".");
    }

    @Override
    public String toString() {
      return super.name().substring(1).replace('_', '.');
    }
  }

  Obligation obligation();

  boolean setByServer() default false;

  boolean trackedOnly() default false;

  Version deprecatedSince() default Version.NEVER;

  Version ignoredSince() default Version.NEVER;
}
