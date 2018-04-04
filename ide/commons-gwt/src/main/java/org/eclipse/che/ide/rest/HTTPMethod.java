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
