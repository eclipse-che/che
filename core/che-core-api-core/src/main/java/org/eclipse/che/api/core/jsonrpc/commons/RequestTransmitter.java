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
package org.eclipse.che.api.core.jsonrpc.commons;

import org.eclipse.che.api.core.jsonrpc.commons.transmission.EndpointIdConfigurator;

/** Simple factory that provides facilities to manually build JSON RPC requests */
public interface RequestTransmitter {
  EndpointIdConfigurator newRequest();
}
