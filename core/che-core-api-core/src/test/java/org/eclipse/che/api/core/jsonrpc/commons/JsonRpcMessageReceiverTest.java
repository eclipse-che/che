package org.eclipse.che.api.core.jsonrpc.commons;

import org.eclipse.che.api.core.logger.commons.LoggerFactory;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JsonRpcMessageReceiver}
 */
@Listeners(MockitoTestNGListener.class)
public class JsonRpcMessageReceiverTest {

    static final String MESSAGE     = "message";
    static final String ENDPOINT_ID = "endpoint-id";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    LoggerFactory           loggerFactory;
    @Mock
    RequestDispatcher       requestDispatcher;
    @Mock
    ResponseDispatcher      responseDispatcher;
    @Mock
    JsonRpcErrorTransmitter errorTransmitter;
    @Mock
    JsonRpcQualifier        jsonRpcQualifier;
    @Mock
    JsonRpcUnmarshaller     jsonRpcUnmarshaller;
    @InjectMocks
    JsonRpcMessageReceiver  jsonRpcMessageReceiver;

    @Test
    public void shouldValidateMessage() throws Exception {
        jsonRpcMessageReceiver.receive(ENDPOINT_ID, MESSAGE);

        verify(jsonRpcQualifier).isValidJson(MESSAGE);
    }

    @Test
    public void shouldTransmitErrorWhenValidationFailed() throws Exception {
        when(jsonRpcQualifier.isValidJson(MESSAGE)).thenReturn(false);

        jsonRpcMessageReceiver.receive(ENDPOINT_ID, MESSAGE);

        verify(errorTransmitter).transmit(eq(ENDPOINT_ID), any(JsonRpcException.class));
    }

    @Test
    public void shouldNotTransmitErrorWhenValidationSucceeded() throws Exception {
        when(jsonRpcQualifier.isValidJson(MESSAGE)).thenReturn(true);

        jsonRpcMessageReceiver.receive(ENDPOINT_ID, MESSAGE);

        verify(errorTransmitter, never()).transmit(eq(ENDPOINT_ID), any(JsonRpcException.class));
    }

    @Test
    public void shouldUnmarshalArray() throws Exception {
        jsonRpcMessageReceiver.receive(ENDPOINT_ID, MESSAGE);

        verify(jsonRpcUnmarshaller).unmarshalArray(MESSAGE);
    }

    @Test
    public void shouldDispatchResponseIfResponseReceived() throws Exception {
        when(jsonRpcQualifier.isJsonRpcResponse(MESSAGE)).thenReturn(true);
        when(jsonRpcQualifier.isJsonRpcRequest(MESSAGE)).thenReturn(false);
        when(jsonRpcUnmarshaller.unmarshalArray(any())).thenReturn(singletonList(MESSAGE));

        jsonRpcMessageReceiver.receive(ENDPOINT_ID, MESSAGE);

        verify(responseDispatcher).dispatch(eq(ENDPOINT_ID), any(JsonRpcResponse.class));
    }

    @Test
    public void shouldDispatchRequestIfRequestReceived() throws Exception {
        when(jsonRpcQualifier.isJsonRpcRequest(MESSAGE)).thenReturn(true);
        when(jsonRpcQualifier.isJsonRpcResponse(MESSAGE)).thenReturn(false);
        when(jsonRpcUnmarshaller.unmarshalArray(any())).thenReturn(singletonList(MESSAGE));

        jsonRpcMessageReceiver.receive(ENDPOINT_ID, MESSAGE);

        verify(requestDispatcher).dispatch(eq(ENDPOINT_ID), any(JsonRpcRequest.class));
    }
}
