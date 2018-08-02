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

import static com.google.gwt.http.client.Response.SC_ACCEPTED;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;

import com.google.gwt.core.client.Callback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.ide.commons.exception.JobNotFoundException;
import org.eclipse.che.ide.commons.exception.ServerException;

/**
 * Wrapper under {@link RequestBuilder} to simplify the stuffs.
 *
 * @author Artem Zatsarynnyi
 */
public class AsyncRequest {
  protected RequestBuilder requestBuilder;
  protected int asyncTaskCheckingPeriodMillis = 5000;

  private AsyncRequestCallback<?> callback;
  private boolean async;
  private Timer checkAsyncTaskStatusTimer;
  private AsyncRequestCallback<String> asyncRequestCallback;
  private String asyncTaskStatusURL;
  private AsyncRequestLoader loader;
  private RequestStatusHandler asyncTaskStatusHandler;

  /**
   * Create new {@link AsyncRequest} instance.
   *
   * @param method request method
   * @param url request URL
   * @param async if {@code true} - request will be send in asynchronous mode (as asynchronous
   *     EverRest task).<br>
   *     See <a href="https://github.com/codenvy/everrest/wiki/Asynchronous-Requests">EverRest
   *     Asynchronous requests</a> for details.
   */
  protected AsyncRequest(Method method, String url, boolean async) {
    if (async) {
      if (url.contains("?")) {
        url += "&async=true";
      } else {
        url += "?async=true";
      }
    }

    this.requestBuilder = new RequestBuilder(method, url);
    this.async = async;
    this.checkAsyncTaskStatusTimer = new CheckEverRestTaskStatusTimer();
    this.asyncRequestCallback = new EverRestAsyncRequestCallback();
  }

  public AsyncRequest header(String header, String value) {
    requestBuilder.setHeader(header, value);
    return this;
  }

  public AsyncRequest user(String user) {
    requestBuilder.setUser(user);
    return this;
  }

  public AsyncRequest password(String password) {
    requestBuilder.setPassword(password);
    return this;
  }

  public AsyncRequest data(String requestData) {
    requestBuilder.setRequestData(requestData);
    return this;
  }

  public AsyncRequest loader(AsyncRequestLoader loader) {
    this.loader = loader;
    return this;
  }

  /**
   * Set period for checking asynchronous task status. (5000 ms by default).<br>
   * Makes sense for request sent in async mode only.
   *
   * @param period period of checking asynchronous task status (in milliseconds)
   * @return this {@link AsyncRequest}
   */
  public AsyncRequest period(int period) {
    this.asyncTaskCheckingPeriodMillis = period;
    return this;
  }

  /**
   * Set handler of async task status. Makes sense for request sent in async mode only.
   *
   * @param handler handler to set
   * @return this {@link AsyncRequest}
   */
  public AsyncRequest requestStatusHandler(RequestStatusHandler handler) {
    this.asyncTaskStatusHandler = handler;
    return this;
  }

  /**
   * Sends an HTTP request based on the current {@link AsyncRequest} configuration.
   *
   * @return promise that may be resolved with the {@link Void} or rejected in case request error
   */
  public Promise<Void> send() {
    return CallbackPromiseHelper.createFromCallback(
        new CallbackPromiseHelper.Call<Void, Throwable>() {
          @Override
          public void makeCall(final Callback<Void, Throwable> callback) {
            send(
                new AsyncRequestCallback<Void>() {
                  @Override
                  protected void onSuccess(Void result) {
                    callback.onSuccess(null);
                  }

                  @Override
                  protected void onFailure(Throwable exception) {
                    callback.onFailure(exception);
                  }
                });
          }
        });
  }

  /**
   * Sends an HTTP request based on the current {@link AsyncRequest} configuration.
   *
   * @param unmarshaller unmarshaller that should be used to deserialize a response
   * @return promise that may be resolved with the deserialized response value or rejected in case
   *     request error
   */
  public <R> Promise<R> send(final Unmarshallable<R> unmarshaller) {
    return CallbackPromiseHelper.createFromCallback(
        new CallbackPromiseHelper.Call<R, Throwable>() {
          @Override
          public void makeCall(final Callback<R, Throwable> callback) {
            send(
                new AsyncRequestCallback<R>(unmarshaller) {
                  @Override
                  protected void onSuccess(R result) {
                    callback.onSuccess(result);
                  }

                  @Override
                  protected void onFailure(Throwable exception) {
                    callback.onFailure(exception);
                  }
                });
          }
        });
  }

  /**
   * Sends an HTTP request based on the current {@link AsyncRequest} configuration.
   *
   * @param callback the response handler to be notified when the request fails or completes
   */
  public void send(AsyncRequestCallback<?> callback) {
    this.callback = callback;
    try {
      if (async) {
        this.callback.setRequest(this);
        sendRequest(asyncRequestCallback);
      } else {
        sendRequest(callback);
      }
    } catch (RequestException e) {
      callback.onFailure(e);
    }
  }

  private void sendRequest(AsyncRequestCallback<?> callback) throws RequestException {
    callback.setLoader(loader);
    callback.setRequest(this);
    requestBuilder.setCallback(callback);

    if (loader != null) {
      loader.show();
    }

    requestBuilder.send();
  }

  /**
   * Returns the callback of current {@link AsyncRequest}, or null if no callback was set.
   *
   * @return the callback that to be notified when the request fails or completes
   */
  public AsyncRequestCallback<?> getCallback() {
    return callback;
  }

  public RequestBuilder getRequestBuilder() {
    return requestBuilder;
  }

  /** Timer that checks status of the EverRest asynchronous task caused by this request. */
  private class CheckEverRestTaskStatusTimer extends Timer {
    @Override
    public void run() {
      final RequestBuilder requestBuilder =
          new RequestBuilder(RequestBuilder.GET, asyncTaskStatusURL);
      requestBuilder.setCallback(
          new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
              if (SC_NOT_FOUND == response.getStatusCode()) {
                callback.onError(request, new JobNotFoundException(response));
                if (asyncTaskStatusHandler != null) {
                  asyncTaskStatusHandler.requestError(
                      asyncTaskStatusURL, new JobNotFoundException(response));
                }
              } else if (response.getStatusCode() != SC_ACCEPTED) {
                callback.onResponseReceived(request, response);
                if (asyncTaskStatusHandler != null) {
                  // check is response successful, for correct handling failed responses
                  if (callback.isSuccessful(response)) {
                    asyncTaskStatusHandler.requestFinished(asyncTaskStatusURL);
                  } else {
                    asyncTaskStatusHandler.requestError(
                        asyncTaskStatusURL, new ServerException(response));
                  }
                }
              } else {
                if (asyncTaskStatusHandler != null) {
                  asyncTaskStatusHandler.requestInProgress(asyncTaskStatusURL);
                }
                CheckEverRestTaskStatusTimer.this.schedule(asyncTaskCheckingPeriodMillis);
              }
            }

            @Override
            public void onError(Request request, Throwable exception) {
              if (asyncTaskStatusHandler != null) {
                asyncTaskStatusHandler.requestError(asyncTaskStatusURL, exception);
              }

              callback.onError(request, exception);
            }
          });

      try {
        requestBuilder.send();
      } catch (RequestException e) {
        if (asyncTaskStatusHandler != null) {
          asyncTaskStatusHandler.requestError(asyncTaskStatusURL, e);
        }
        callback.onFailure(e);
      }
    }
  }

  /** Callback that will be called on response to a request that was sent in EverRest async mode. */
  private class EverRestAsyncRequestCallback extends AsyncRequestCallback<String> {

    EverRestAsyncRequestCallback() {
      super(new LocationUnmarshaller());
      setSuccessCodes(new int[] {SC_ACCEPTED});
    }

    @Override
    protected void onSuccess(String result) {
      asyncTaskStatusURL = result;
      if (asyncTaskStatusHandler != null) {
        asyncTaskStatusHandler.requestInProgress(asyncTaskStatusURL);
      }
      checkAsyncTaskStatusTimer.schedule(asyncTaskCheckingPeriodMillis);
    }

    @Override
    protected void onFailure(Throwable exception) {
      if (asyncTaskStatusHandler != null) {
        asyncTaskStatusHandler.requestError(asyncTaskStatusURL, exception);
      }
      callback.onError(null, exception);
    }
  }
}
