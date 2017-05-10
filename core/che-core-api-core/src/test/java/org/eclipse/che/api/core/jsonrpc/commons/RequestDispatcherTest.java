package org.eclipse.che.api.core.jsonrpc.commons;

import org.eclipse.che.api.core.logger.commons.Logger;
import org.eclipse.che.api.core.logger.commons.LoggerFactory;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RequestDispatcher}
 */
@Listeners(MockitoTestNGListener.class)
public class RequestDispatcherTest {
    static final String ENDPOINT_ID = "endpoint-id";
    static final String REQUEST_ID = "request-id";
    static final String REQUEST_METHOD = "request-method";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    LoggerFactory         loggerFactory;
    @Mock
    RequestHandlerManager requestHandlerManager;
    @InjectMocks
    RequestDispatcher     requestDispatcher;

    @Mock
    JsonRpcRequest request;
    @Mock
    JsonRpcParams params;

    @BeforeMethod
    public void setUp() throws Exception {
        when(request.getId()).thenReturn(REQUEST_ID);
        when(request.getMethod()).thenReturn(REQUEST_METHOD);
        when(request.getParams()).thenReturn(params);
        when(requestHandlerManager.isRegistered(REQUEST_METHOD)).thenReturn(true);
    }

    @Test
    public void shouldHandleRequest() throws Exception {
        when(request.hasId()).thenReturn(true);

        requestDispatcher.dispatch(ENDPOINT_ID, request);

        verify(requestHandlerManager).handle(ENDPOINT_ID, REQUEST_ID, REQUEST_METHOD, params);
    }

    @Test
    public void shouldHandleNotification() throws Exception {
        when(request.hasId()).thenReturn(false);

        requestDispatcher.dispatch(ENDPOINT_ID, request);

        verify(requestHandlerManager).handle(ENDPOINT_ID, REQUEST_METHOD, params);
    }

    @Test(expectedExceptions = JsonRpcException.class)
    public void shouldThrowExceptionOnNotRegisteredRequestHandler() throws Exception {
        when(requestHandlerManager.isRegistered(REQUEST_METHOD)).thenReturn(false);

        requestDispatcher.dispatch(ENDPOINT_ID, request);
    }

    @Test(expectedExceptions = JsonRpcException.class)
    public void shouldThrowExceptionOnNotRegisteredNotificationHandler() throws Exception {
        when(requestHandlerManager.isRegistered(REQUEST_METHOD)).thenReturn(false);

        requestDispatcher.dispatch(ENDPOINT_ID, request);
    }
}
