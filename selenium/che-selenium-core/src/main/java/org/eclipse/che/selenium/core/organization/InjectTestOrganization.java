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
package org.eclipse.che.selenium.core.organization;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * To instantiate {@link TestOrganization} in test classes.
 *
 * @author Dmytro Nochevnov
 */
@Target({FIELD})
@Retention(RUNTIME)
@Documented
public @interface InjectTestOrganization {
  /** Organization name prefix. */
  String prefix() default "";

  /** Parent organization name prefix. */
  String parentPrefix() default "";
}
