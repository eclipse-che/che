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
package org.eclipse.che.ide.rest;

/**
 * Created by The eXo Platform SARL .<br>
 * HTTP methods
 *
 * @author Gennady Azarenkov
 */
public interface HTTPMethod {

  String GET = "GET";

  String PUT = "PUT";

  String POST = "POST";

  String DELETE = "DELETE";

  String SEARCH = "SEARCH";

  String PROPFIND = "PROPFIND";

  String PROPPATCH = "PROPPATCH";

  String HEAD = "HEAD";

  String CHECKIN = "CHECKIN";

  String CHECKOUT = "CHECKOUT";

  String COPY = "COPY";

  String LOCK = "LOCK";

  String MOVE = "MOVE";

  String UNLOCK = "UNLOCK";

  String OPTIONS = "OPTIONS";

  String MKCOL = "MKCOL";

  String REPORT = "REPORT";

  String UPDATE = "UPDATE";

  String ACL = "ACL";
}
