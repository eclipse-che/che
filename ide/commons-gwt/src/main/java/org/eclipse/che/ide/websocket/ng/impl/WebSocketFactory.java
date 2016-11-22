/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.websocket.ng.impl;

/**
 * Guice factory interface to create a web socket connection based on an URL
 * with all related dependencies injected
 *
 * @author Dmitry Kuleshov
 */
public interface WebSocketFactory {
    WebSocketConnection create(String url);
}
