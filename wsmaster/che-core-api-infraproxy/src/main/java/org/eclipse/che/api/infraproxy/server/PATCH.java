/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.infraproxy.server;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.ws.rs.HttpMethod;

/**
 * Kubernetes API accepts PATCH requests but JAX-RS doesn't provide the annotation for such requests
 * out of the box. So we need to declare one.
 */
@Target(METHOD)
@Retention(RUNTIME)
@HttpMethod("PATCH")
@Documented
public @interface PATCH {}
