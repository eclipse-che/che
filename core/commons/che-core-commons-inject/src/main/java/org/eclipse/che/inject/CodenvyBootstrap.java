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
package org.eclipse.che.inject;

/**
 * CodenvyBootstrap is entry point of codenvy application implemented as ServletContextListener.
 * Deprecated in favor of {@link org.eclipse.che.inject.CheBootstrap}
 */
@Deprecated
public class CodenvyBootstrap extends CheBootstrap {}
