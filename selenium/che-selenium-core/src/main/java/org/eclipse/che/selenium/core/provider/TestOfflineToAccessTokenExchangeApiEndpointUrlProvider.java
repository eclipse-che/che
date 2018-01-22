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
package org.eclipse.che.selenium.core.provider;

import com.google.inject.Provider;
import java.net.URL;

/**
 * Endpoint of API to exchange offline token to access token.
 *
 * @author Dmytro Nochevnov
 */
public interface TestOfflineToAccessTokenExchangeApiEndpointUrlProvider extends Provider<URL> {}
