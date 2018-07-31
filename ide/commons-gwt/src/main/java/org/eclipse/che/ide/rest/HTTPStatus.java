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
 * HTTP status
 *
 * @author Gennady Azarenkov
 */
public interface HTTPStatus {
  /** HTTP Status-Code 202: Accepted. */
  int ACCEPTED = 202;

  /** HTTP Status-Code 502: Bad Gateway. */
  int BAD_GATEWAY = 502;

  /** HTTP Status-Code 405: Method Not Allowed. */
  int BAD_METHOD = 405;

  /** HTTP Status-Code 400: Bad Request. */
  int BAD_REQUEST = 400;

  /** HTTP Status-Code 408: Request Time-Out. */
  int CLIENT_TIMEOUT = 408;

  /** HTTP Status-Code 409: Conflict. */
  int CONFLICT = 409;

  /** HTTP Status-Code 201: Created. */
  int CREATED = 201;

  /** HTTP Status-Code 413: Request Entity Too Large. */
  int ENTITY_TOO_LARGE = 413;

  /** HTTP Status-Code 403: Forbidden. */
  int FORBIDDEN = 403;

  /** HTTP Status-Code 504: Gateway Timeout. */
  int GATEWAY_TIMEOUT = 504;

  /** HTTP Status-Code 410: Gone. */
  int GONE = 410;

  /** HTTP Status-Code 500: Internal Server Error. */
  int INTERNAL_ERROR = 500;

  /** HTTP Status-Code 411: Length Required. */
  int LENGTH_REQUIRED = 411;

  /** HTTP Status-Code 301: Moved Permanently. */
  int MOVED_PERM = 301;

  /** HTTP Status-Code 302: Temporary Redirect. */
  int FOUND = 302;

  /** HTTP Status-Code 300: Multiple Choices. */
  int MULT_CHOICE = 300;

  /** HTTP Status-Code 204: No Content. */
  int NO_CONTENT = 204;

  /** HTTP Status-Code 406: Not Acceptable. */
  int NOT_ACCEPTABLE = 406;

  /** HTTP Status-Code 203: Non-Authoritative Information. */
  int NOT_AUTHORITATIVE = 203;

  /** HTTP Status-Code 404: Not Found. */
  int NOT_FOUND = 404;

  /** HTTP Status-Code 501: Not Implemented. */
  int NOT_IMPLEMENTED = 501;

  /** HTTP Status-Code 304: Not Modified. */
  int NOT_MODIFIED = 304;

  /** HTTP Status-Code 200: OK. */
  int OK = 200;

  /** HTTP Status-Code 206: Partial Content. */
  int PARTIAL = 206;

  /** HTTP Status-Code 402: Payment Required. */
  int PAYMENT_REQUIRED = 402;

  /** HTTP Status-Code 412: Precondition Failed. */
  int PRECON_FAILED = 412;

  /** HTTP Status-Code 407: Proxy Authentication Required. */
  int PROXY_AUTH = 407;

  /** HTTP Status-Code 414: Request-URI Too Large. */
  int REQ_TOO_LONG = 414;

  /** HTTP Status-Code 205: Reset Content. */
  int RESET = 205;

  /** HTTP Status-Code 303: See Other. */
  int SEE_OTHER = 303;

  /** HTTP Status-Code 307: Temporary Redirect. */
  int TEMP_REDIRECT = 307;

  /** HTTP Status-Code 401: Unauthorized. */
  int UNAUTHORIZED = 401;

  /** HTTP Status-Code 503: Service Unavailable. */
  int UNAVAILABLE = 503;

  /** HTTP Status-Code 415: Unsupported Media Type. */
  int UNSUPPORTED_TYPE = 415;

  /** HTTP Status-Code 305: Use Proxy. */
  int USE_PROXY = 305;

  /** HTTP Status-Code 505: HTTP Version Not Supported. */
  int VERSION = 505;

  /**
   * HTTP/1.1 Status 423 "Locked" code extensions. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'>HTTP/1.1 Status Code Definitions
   * </a> section 10.4 for more information.
   */
  int LOCKED = 423;

  /**
   * HTTP/1.1 Status 207 "Multistatus" code extensions. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'>HTTP/1.1 Status Code Definitions
   * </a> section 10.2 for more information.
   */
  int MULTISTATUS = 207;

  /**
   * HTTP/1.1 Status 405 "Method Not Allowed" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'>HTTP/1.1 Status Code Definitions
   * </a> section 10.4 for more information.
   */
  int METHOD_NOT_ALLOWED = 405;

  /**
   * HTTP/1.1 Status 416 "Requested Range Not Satisfiable" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'>HTTP/1.1 Status Code Definitions
   * </a> section 10.4 for more information.
   */
  int REQUESTED_RANGE_NOT_SATISFIABLE = 416;

  /**
   * HTTP/1.1 Status 100 "Continue" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'>HTTP/1.1 Status Code Definitions
   * </a> section 10.1 for more information.
   */
  int CONTINUE = 100;

  /**
   * HTTP/1.1 Status 101 "Switching Protocols" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'>HTTP/1.1 Status Code Definitions
   * </a> section 10.1 for more information.
   */
  int SWITCHING_PROTOCOLS = 101;

  /**
   * HTTP/1.1 Status 408 "Request Timeout" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'>HTTP/1.1 Status Code Definitions
   * </a> section 10.4 for more information.
   */
  int REQUEST_TIMEOUT = 408;

  /**
   * HTTP/1.1 Status 414 "Request-URI Too Long" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'>HTTP/1.1 Status Code Definitions
   * </a> section 10.4 for more information.
   */
  int REQUEST_URI_TOO_LONG = 414;

  /**
   * HTTP/1.1 Status 417 "Expectation Failed" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'>HTTP/1.1 Status Code Definitions
   * </a> section 10.4 for more information.
   */
  int EXPECTATION_FAILED = 417;

  /**
   * HTTP/1.1 Status 505 "HTTP Version Not Supported" code. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'>HTTP/1.1 Status Code Definitions
   * </a> section 10.5 for more information.
   */
  int HTTP_VERSION_NOT_SUPPORTED = 505;
}
