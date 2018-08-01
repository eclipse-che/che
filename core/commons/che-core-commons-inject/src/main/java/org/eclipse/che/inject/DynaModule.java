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
package org.eclipse.che.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marker annotation for dynamically created modules.
 *
 * <p>{@link CheBootstrap} automatically finds and loads Guice modules (subclasses of {@link
 * com.google.inject.Module}) annotated with &#064DynaModule.
 *
 * @author gazarenkov
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DynaModule {}
