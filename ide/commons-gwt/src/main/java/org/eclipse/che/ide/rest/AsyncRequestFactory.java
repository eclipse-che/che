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
package org.eclipse.che.ide.rest;

import com.google.common.base.Preconditions;
import com.google.gwt.http.client.RequestBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.dto.JsonSerializable;

/**
 * Provides implementations of {@link AsyncRequest} instances.
 *
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class AsyncRequestFactory {
  private static final String DTO_CONTENT_TYPE = MimeType.APPLICATION_JSON;
  private final DtoFactory dtoFactory;

  @Inject
  public AsyncRequestFactory(DtoFactory dtoFactory) {
    this.dtoFactory = dtoFactory;
  }

  /**
   * Creates new GET request to the specified {@code url}.
   *
   * @param url request URL
   * @return new {@link AsyncRequest} instance to send GET request
   */
  public AsyncRequest createGetRequest(String url) {
    return createGetRequest(url, false);
  }

  /**
   * Creates new GET request to the specified {@code url}.
   *
   * @param url request URL
   * @param async if <b>true</b> - request will be sent in asynchronous mode
   * @return new {@link AsyncRequest} instance to send GET request
   */
  public AsyncRequest createGetRequest(String url, boolean async) {
    return createRequest(RequestBuilder.GET, url, null, async);
  }

  /**
   * Creates new POST request to the specified {@code url} with the provided {@code data}.
   *
   * @param url request URL
   * @param dtoData the DTO to send as body of the request. Must implement {@link JsonSerializable}
   *     interface. May be {@code null}.
   * @return new {@link AsyncRequest} instance to send POST request
   */
  public AsyncRequest createPostRequest(String url, Object dtoData) {
    return createPostRequest(url, dtoData, false);
  }

  /**
   * Creates new PUT request to the specified {@code url} with the provided {@code data}.
   *
   * @param url request URL
   * @param dtoData the DTO to send as body of the request. Must implement {@link JsonSerializable}
   *     interface. May be {@code null}.
   * @return new {@link AsyncRequest} instance to send PUT request
   */
  public AsyncRequest createPutRequest(String url, Object dtoData) {
    return createPutRequest(url, dtoData, false);
  }

  /**
   * Creates new POST request to the specified {@code url} with the provided {@code data}.
   *
   * @param url request URL
   * @param dtoArray the array of DTO to send as body of the request. Must contain objects that
   *     implement {@link JsonSerializable} interface. May be {@code null}.
   * @return new {@link AsyncRequest} instance to send POST request
   */
  public AsyncRequest createPostRequest(String url, List<Object> dtoArray) {
    return createPostRequest(url, dtoArray, false);
  }

  /**
   * Creates new POST request to the specified {@code url} with the provided {@code data}.
   *
   * @param url request URL
   * @param dtoData the DTO to send as body of the request. Must implement {@link JsonSerializable}
   *     interface. May be {@code null}.
   * @param async if <b>true</b> - request will be sent in asynchronous mode
   * @return new {@link AsyncRequest} instance to send POST request
   */
  public AsyncRequest createPostRequest(String url, Object dtoData, boolean async) {
    return createRequest(RequestBuilder.POST, url, dtoData, async);
  }

  /**
   * Creates new PUT request to the specified {@code url} with the provided {@code data}.
   *
   * @param url request URL
   * @param dtoData the DTO to send as body of the request. Must implement {@link JsonSerializable}
   *     interface. May be {@code null}.
   * @param async if <b>true</b> - request will be sent in asynchronous mode
   * @return new {@link AsyncRequest} instance to send POST request
   */
  public AsyncRequest createPutRequest(String url, Object dtoData, boolean async) {
    return createRequest(RequestBuilder.PUT, url, dtoData, async);
  }

  /**
   * Creates new POST request to the specified {@code url} with the provided {@code data}.
   *
   * @param url request URL
   * @param dtoArray the array of DTO to send as body of the request. Must contain objects that
   *     implement {@link JsonSerializable} interface. May be {@code null}.
   * @param async if <b>true</b> - request will be sent in asynchronous mode
   * @return new {@link AsyncRequest} instance to send POST request
   */
  public AsyncRequest createPostRequest(String url, List<Object> dtoArray, boolean async) {
    return createRequest(RequestBuilder.POST, url, dtoArray, async);
  }

  /**
   * Creates new HTTP request to the specified {@code url}.
   *
   * @param method request method
   * @param url request URL
   * @param dtoBody the DTO to send as body of the request. Must implement {@link JsonSerializable}
   *     interface. May be {@code null}.
   * @param async if <b>true</b> - request will be sent in asynchronous mode
   * @return new {@link AsyncRequest} instance to send POST request
   * @throws NullPointerException in case if request {@code method} is {@code null}, reason
   *     includes:
   *     <ul>
   *       <li>Request method should not be a null
   *     </ul>
   */
  public AsyncRequest createRequest(
      RequestBuilder.Method method, String url, Object dtoBody, boolean async) {
    return doCreateRequest(method, url, dtoBody, async);
  }

  /**
   * Creates new HTTP request to the specified {@code url}.
   *
   * @param method request method
   * @param url request URL
   * @param dtoBody the DTO to send as body of the request. Must implement {@link JsonSerializable}
   *     interface. May be {@code null}.
   * @param async if <b>true</b> - request will be sent in asynchronous mode
   * @return new {@link AsyncRequest} instance to send POST request
   * @throws NullPointerException in case if request {@code method} is {@code null}, reason
   *     includes:
   *     <ul>
   *       <li>Request method should not be a null
   *     </ul>
   */
  public AsyncRequest createRequest(
      RequestBuilder.Method method, String url, List<Object> dtoBody, boolean async) {
    return doCreateRequest(method, url, dtoBody, async);
  }

  /**
   * Creates new HTTP request to the specified {@code url}.
   *
   * @param method request method
   * @param url request URL
   * @param dtoBody the DTO to send as body of the request. Must implement {@link JsonSerializable}
   *     interface or contain objects that implement it. May be {@code null}.
   * @param async if <b>true</b> - request will be sent in asynchronous mode
   * @return new {@link AsyncRequest} instance to send POST request
   * @throws NullPointerException in case if request {@code method} is {@code null}, reason
   *     includes:
   *     <ul>
   *       <li>Request method should not be a null
   *     </ul>
   */
  protected AsyncRequest doCreateRequest(
      RequestBuilder.Method method, String url, Object dtoBody, boolean async) {
    Preconditions.checkNotNull(method, "Request method should not be a null");

    AsyncRequest asyncRequest = newAsyncRequest(method, url, async);
    if (dtoBody != null) {
      if (dtoBody instanceof List) {
        asyncRequest.data(dtoFactory.toJson((List) dtoBody));
      } else if (dtoBody instanceof String) {
        asyncRequest.data((String) dtoBody);
      } else {
        asyncRequest.data(dtoFactory.toJson(dtoBody));
      }
      asyncRequest.header(HTTPHeader.CONTENT_TYPE, DTO_CONTENT_TYPE);
    } else if (method.equals(RequestBuilder.POST) || method.equals(RequestBuilder.PUT)) {

      /*
        Here we need to setup wildcard mime type in content-type header, because CORS filter
        responses with 403 error in case if user makes POST/PUT request with null body and without
        content-type header. Setting content-type header with wildcard mime type solves this problem.

        Note, this issue need to be investigated, because the problem may be occurred as a bug in
        CORS filter.
      */

      asyncRequest.header(HTTPHeader.CONTENT_TYPE, MimeType.WILDCARD);
    }
    return asyncRequest;
  }

  /**
   * A factory method which creates a new instance of {@link AsyncRequest}.
   *
   * @param method the request method
   * @param url the url to go to
   * @param async whether this request is asynchronous in terms of Everrest polling strategy
   */
  protected AsyncRequest newAsyncRequest(RequestBuilder.Method method, String url, boolean async) {
    return new AsyncRequest(method, url, async);
  }

  /**
   * Creates new GET request to the specified {@code url}.
   *
   * @param url request URL
   * @return new {@link AsyncRequest} instance to send GET request
   */
  public AsyncRequest createDeleteRequest(String url) {
    return doCreateRequest(RequestBuilder.DELETE, url, null, false);
  }
}
