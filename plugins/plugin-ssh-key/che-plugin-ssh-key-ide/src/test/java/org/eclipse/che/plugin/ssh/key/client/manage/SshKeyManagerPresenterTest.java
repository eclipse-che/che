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
package org.eclipse.che.plugin.ssh.key.client.manage;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.InputCallback;
import org.eclipse.che.ide.api.dialogs.InputDialog;
import org.eclipse.che.ide.api.dialogs.InputValidator;
import org.eclipse.che.ide.api.dialogs.InputValidator.Violation;
import org.eclipse.che.ide.api.dialogs.MessageDialog;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode;
import org.eclipse.che.ide.api.ssh.SshServiceClient;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.plugin.ssh.key.client.SshKeyLocalizationConstant;
import org.eclipse.che.plugin.ssh.key.client.SshResources;
import org.eclipse.che.plugin.ssh.key.client.upload.UploadSshKeyPresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link SshKeyManagerPresenter} functionality.
 *
 * @author Roman Nikitenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class SshKeyManagerPresenterTest {
    public static final String GITHUB_HOST = "github.com";

    @Captor
    private ArgumentCaptor<AsyncCallback<Void>> asyncCallbackCaptor;

    @Captor
    private ArgumentCaptor<ConfirmCallback> confirmCallbackCaptor;

    @Captor
    private ArgumentCaptor<CancelCallback> cancelCallbackCaptor;

    @Captor
    private ArgumentCaptor<InputCallback> inputCallbackCaptor;

    @Captor
    private ArgumentCaptor<Operation<Void>> operationVoidCapture;

    @Captor
    private ArgumentCaptor<Operation<List<SshPairDto>>> operationSshPairDTOsCapture;

    @Captor
    private ArgumentCaptor<Operation<SshPairDto>> operationSshPairDTOCapture;

    @Captor
    private ArgumentCaptor<Operation<PromiseError>> operationErrorCapture;

    private Promise<Void>             voidPromise;
    private Promise<SshPairDto>       sshPairDTOPromise;
    private Promise<List<SshPairDto>> sshPairDTOsPromise;

    @Mock
    private AppContext                 appContext;
    @Mock
    private DtoUnmarshallerFactory     dtoUnmarshallerFactory;
    @Mock
    private DialogFactory              dialogFactory;
    @Mock
    private SshKeyManagerView          view;
    @Mock
    private SshServiceClient           service;
    @Mock
    private ShowSshKeyView             showSshKeyView;
    @Mock
    private SshKeyLocalizationConstant constant;
    @Mock
    private SshResources               resources;
    @Mock
    private UploadSshKeyPresenter      uploadSshKeyPresenter;
    @Mock
    private NotificationManager        notificationManager;
    @Mock
    private InputDialog                inputDialog;
    @InjectMocks
    private SshKeyManagerPresenter     presenter;

    @Mock
    SshPairDto sshPairDto;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        voidPromise = createPromiseMock();
        sshPairDTOsPromise = createPromiseMock();
        sshPairDTOPromise = createPromiseMock();

        when(service.getPairs(anyString())).thenReturn(sshPairDTOsPromise);
        when(service.deletePair(anyString(), anyString())).thenReturn(voidPromise);
        when(service.generatePair(anyString(), anyString())).thenReturn(sshPairDTOPromise);
        when(inputDialog.withValidator(any(InputValidator.class))).thenReturn(inputDialog);
    }

    private Promise createPromiseMock() {
        return mock(Promise.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (invocation.getMethod().getReturnType().isInstance(invocation.getMock())) {
                    return invocation.getMock();
                }
                return RETURNS_DEFAULTS.answer(invocation);
            }
        });
    }

    @Test
    public void testGo() {
        AcceptsOneWidget container = mock(AcceptsOneWidget.class);

        presenter.go(container);

        verify(service).getPairs(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE));
        verify(container).setWidget(eq(view));
    }

    @Test
    public void testOnViewClickedWhenGetPublicKeyIsSuccess() {
        when(sshPairDto.getPublicKey()).thenReturn("publicKey");
        when(sshPairDto.getName()).thenReturn("name");
        MessageDialog messageDialog = mock(MessageDialog.class);
        when(dialogFactory.createMessageDialog(anyString(), anyString(), (ConfirmCallback)anyObject())).thenReturn(messageDialog);

        presenter.onViewClicked(sshPairDto);

        verify(showSshKeyView).show("name", "publicKey");
    }

    @Test
    public void testOnDeleteClickedWhenDeleteKeyConfirmed() {
        when(sshPairDto.getService()).thenReturn(SshKeyManagerPresenter.VCS_SSH_SERVICE);
        when(sshPairDto.getName()).thenReturn(GITHUB_HOST);
        SafeHtml safeHtml = mock(SafeHtml.class);
        ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
        when(constant.deleteSshKeyQuestion(anyString())).thenReturn(safeHtml);
        when(safeHtml.asString()).thenReturn("");
        when(dialogFactory.createConfirmDialog(anyString(), anyString(), (ConfirmCallback)anyObject(), (CancelCallback)anyObject()))
                .thenReturn(confirmDialog);

        presenter.onDeleteClicked(sshPairDto);

        verify(dialogFactory).createConfirmDialog(anyString(), anyString(), confirmCallbackCaptor.capture(), (CancelCallback)anyObject());
        ConfirmCallback confirmCallback = confirmCallbackCaptor.getValue();
        confirmCallback.accepted();

        verify(confirmDialog).show();
        verify(service).deletePair(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE), eq(GITHUB_HOST));
    }

    @Test
    public void testOnDeleteClickedWhenDeleteKeyCanceled() {
        SafeHtml safeHtml = mock(SafeHtml.class);
        ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
        when(constant.deleteSshKeyQuestion(anyString())).thenReturn(safeHtml);
        when(safeHtml.asString()).thenReturn("");
        when(dialogFactory.createConfirmDialog(anyString(), anyString(), (ConfirmCallback)anyObject(), (CancelCallback)anyObject()))
                .thenReturn(confirmDialog);

        presenter.onDeleteClicked(sshPairDto);

        verify(dialogFactory).createConfirmDialog(anyString(), anyString(), (ConfirmCallback)anyObject(), cancelCallbackCaptor.capture());
        CancelCallback cancelCallback = cancelCallbackCaptor.getValue();
        cancelCallback.cancelled();

        verify(confirmDialog).show();
        verify(service, never()).deletePair(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE), anyString());
    }

    @Test
    public void testOnDeleteClickedWhenDeleteKeyIsSuccess() throws OperationException {
        when(sshPairDto.getService()).thenReturn(SshKeyManagerPresenter.VCS_SSH_SERVICE);
        when(sshPairDto.getName()).thenReturn(GITHUB_HOST);
        SafeHtml safeHtml = mock(SafeHtml.class);
        ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
        when(constant.deleteSshKeyQuestion(anyString())).thenReturn(safeHtml);
        when(safeHtml.asString()).thenReturn("");
        when(dialogFactory.createConfirmDialog(anyString(), anyString(), (ConfirmCallback)anyObject(), (CancelCallback)anyObject()))
                .thenReturn(confirmDialog);

        presenter.onDeleteClicked(sshPairDto);

        verify(dialogFactory).createConfirmDialog(anyString(), anyString(), confirmCallbackCaptor.capture(), (CancelCallback)anyObject());
        ConfirmCallback confirmCallback = confirmCallbackCaptor.getValue();
        confirmCallback.accepted();

        verify(voidPromise).then(operationVoidCapture.capture());
        operationVoidCapture.getValue().apply(null);

        verify(confirmDialog).show();
        verify(service).deletePair(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE), eq(GITHUB_HOST));
        verify(service).getPairs(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE));
    }

    @Test
    public void testOnDeleteClickedWhenDeleteKeyIsFailure() throws OperationException {
        when(sshPairDto.getService()).thenReturn(SshKeyManagerPresenter.VCS_SSH_SERVICE);
        when(sshPairDto.getName()).thenReturn(GITHUB_HOST);
        SafeHtml safeHtml = mock(SafeHtml.class);
        ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
        when(constant.deleteSshKeyQuestion(anyString())).thenReturn(safeHtml);
        when(safeHtml.asString()).thenReturn("");
        when(dialogFactory.createConfirmDialog(anyString(), anyString(), (ConfirmCallback)anyObject(), (CancelCallback)anyObject()))
                .thenReturn(confirmDialog);

        presenter.onDeleteClicked(sshPairDto);

        verify(dialogFactory).createConfirmDialog(anyString(), anyString(), confirmCallbackCaptor.capture(), (CancelCallback)anyObject());
        ConfirmCallback confirmCallback = confirmCallbackCaptor.getValue();
        confirmCallback.accepted();

        verify(voidPromise).catchError(operationErrorCapture.capture());
        operationErrorCapture.getValue().apply(JsPromiseError.create(""));

        verify(confirmDialog).show();
        verify(service).deletePair(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE), anyString());
        verify(notificationManager).notify(anyString(), eq(StatusNotification.Status.FAIL), eq(FLOAT_MODE));
        verify(service, never()).getPairs(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE));
    }

    @Test
    public void testShouldRefreshKeysAfterSuccessfulDeleteKey() throws OperationException {
        when(sshPairDto.getService()).thenReturn(SshKeyManagerPresenter.VCS_SSH_SERVICE);
        when(sshPairDto.getName()).thenReturn(GITHUB_HOST);
        SafeHtml safeHtml = mock(SafeHtml.class);
        ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
        List<SshPairDto> sshPairDtoArray = new ArrayList<>();
        when(constant.deleteSshKeyQuestion(anyString())).thenReturn(safeHtml);
        when(safeHtml.asString()).thenReturn("");
        when(dialogFactory.createConfirmDialog(anyString(), anyString(), (ConfirmCallback)anyObject(), (CancelCallback)anyObject()))
                .thenReturn(confirmDialog);

        presenter.onDeleteClicked(sshPairDto);

        verify(dialogFactory).createConfirmDialog(anyString(), anyString(), confirmCallbackCaptor.capture(), (CancelCallback)anyObject());
        ConfirmCallback confirmCallback = confirmCallbackCaptor.getValue();
        confirmCallback.accepted();

        verify(voidPromise).then(operationVoidCapture.capture());
        operationVoidCapture.getValue().apply(null);

        verify(sshPairDTOsPromise).then(operationSshPairDTOsCapture.capture());
        operationSshPairDTOsCapture.getValue().apply(sshPairDtoArray);

        verify(confirmDialog).show();
        verify(service).deletePair(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE), eq(GITHUB_HOST));
        verify(service).getPairs(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE));
        verify(view).setPairs(eq(sshPairDtoArray));
    }

    @Test
    public void testFailedRefreshKeysAfterSuccessfulDeleteKey() throws OperationException {
        when(sshPairDto.getService()).thenReturn(SshKeyManagerPresenter.VCS_SSH_SERVICE);
        when(sshPairDto.getName()).thenReturn(GITHUB_HOST);
        SafeHtml safeHtml = mock(SafeHtml.class);
        ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
        List<SshPairDto> sshPairDtoArray = new ArrayList<>();
        when(constant.deleteSshKeyQuestion(anyString())).thenReturn(safeHtml);
        when(safeHtml.asString()).thenReturn("");
        when(dialogFactory.createConfirmDialog(anyString(), anyString(), (ConfirmCallback)anyObject(), (CancelCallback)anyObject()))
                .thenReturn(confirmDialog);

        presenter.onDeleteClicked(sshPairDto);

        verify(dialogFactory).createConfirmDialog(anyString(), anyString(), confirmCallbackCaptor.capture(), (CancelCallback)anyObject());
        ConfirmCallback confirmCallback = confirmCallbackCaptor.getValue();
        confirmCallback.accepted();

        verify(voidPromise).then(operationVoidCapture.capture());
        operationVoidCapture.getValue().apply(null);

        verify(sshPairDTOsPromise).catchError(operationErrorCapture.capture());
        operationErrorCapture.getValue().apply(JsPromiseError.create(""));

        verify(confirmDialog).show();
        verify(service).deletePair(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE), eq(GITHUB_HOST));
        verify(service).getPairs(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE));
        verify(view, never()).setPairs(eq(sshPairDtoArray));
        verify(notificationManager).notify(anyString(), any(StatusNotification.Status.class), (DisplayMode)anyObject());
    }

    @Test
    public void testShouldRefreshKeysAfterSuccessfulUploadKey() throws OperationException {
        List<SshPairDto> sshPairDtoArray = new ArrayList<>();

        presenter.onUploadClicked();

        verify(uploadSshKeyPresenter).showDialog(asyncCallbackCaptor.capture());
        AsyncCallback<Void> asyncCallback = asyncCallbackCaptor.getValue();
        asyncCallback.onSuccess(null);

        verify(sshPairDTOsPromise).then(operationSshPairDTOsCapture.capture());
        operationSshPairDTOsCapture.getValue().apply(sshPairDtoArray);

        verify(service).getPairs(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE));
        verify(view).setPairs(eq(sshPairDtoArray));
    }


    @Test
    public void testFailedRefreshKeysAfterSuccessfulUploadKey() throws OperationException {
        List<SshPairDto> sshPairDtoArray = new ArrayList<>();

        presenter.onUploadClicked();

        verify(uploadSshKeyPresenter).showDialog(asyncCallbackCaptor.capture());
        AsyncCallback<Void> asyncCallback = asyncCallbackCaptor.getValue();
        asyncCallback.onSuccess(null);

        verify(sshPairDTOsPromise).catchError(operationErrorCapture.capture());
        operationErrorCapture.getValue().apply(JsPromiseError.create(""));

        verify(service).getPairs(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE));
        verify(view, never()).setPairs(eq(sshPairDtoArray));
    }


    @Test
    public void testOnGenerateClickedWhenUserConfirmGenerateKey() {
        when(dialogFactory.createInputDialog(anyString(), anyString(), (InputCallback)anyObject(), (CancelCallback)anyObject()))
                .thenReturn(inputDialog);

        presenter.onGenerateClicked();

        verify(dialogFactory).createInputDialog(anyString(), anyString(), inputCallbackCaptor.capture(), (CancelCallback)anyObject());
        InputCallback inputCallback = inputCallbackCaptor.getValue();
        inputCallback.accepted(GITHUB_HOST);

        verify(service).generatePair(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE), eq(GITHUB_HOST));
    }

    @Test
    public void testOnGenerateClickedWhenUserCancelGenerateKey() {
        when(dialogFactory.createInputDialog(anyString(), anyString(), (InputCallback)anyObject(), (CancelCallback)anyObject()))
                .thenReturn(inputDialog);

        presenter.onGenerateClicked();

        verify(dialogFactory).createInputDialog(anyString(), anyString(), (InputCallback)anyObject(), cancelCallbackCaptor.capture());
        CancelCallback cancelCallback = cancelCallbackCaptor.getValue();
        cancelCallback.cancelled();

        verify(service, never()).generatePair(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE), eq(GITHUB_HOST));
    }

    @Test
    public void testOnGenerateClickedWhenGenerateKeyIsFailed() throws OperationException {
        when(dialogFactory.createInputDialog(anyString(), anyString(), (InputCallback)anyObject(), (CancelCallback)anyObject()))
                .thenReturn(inputDialog);

        presenter.onGenerateClicked();

        verify(dialogFactory).createInputDialog(anyString(), anyString(),
                                                inputCallbackCaptor.capture(), cancelCallbackCaptor.capture());
        InputCallback inputCallback = inputCallbackCaptor.getValue();
        inputCallback.accepted(GITHUB_HOST);

        verify(sshPairDTOPromise).catchError(operationErrorCapture.capture());
        operationErrorCapture.getValue().apply(JsPromiseError.create(""));

        verify(service).generatePair(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE), eq(GITHUB_HOST));
        verify(service, never()).getPairs(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE));
        verify(view, never()).setPairs((List<SshPairDto>)anyObject());
        verify(notificationManager).notify(anyString(), anyString(), any(StatusNotification.Status.class), (DisplayMode)anyObject());
    }

    @Test
    public void testShouldRefreshKeysAfterSuccessfulGenerateKey() throws OperationException {
        List<SshPairDto> sshPairDtoArray = new ArrayList<>();
        when(dialogFactory.createInputDialog(anyString(), anyString(), (InputCallback)anyObject(), (CancelCallback)anyObject()))
                .thenReturn(inputDialog);

        presenter.onGenerateClicked();

        verify(dialogFactory).createInputDialog(anyString(), anyString(),
                                                inputCallbackCaptor.capture(), cancelCallbackCaptor.capture());
        InputCallback inputCallback = inputCallbackCaptor.getValue();
        inputCallback.accepted(GITHUB_HOST);

        verify(sshPairDTOPromise).then(operationSshPairDTOCapture.capture());
        operationSshPairDTOCapture.getValue().apply(null);

        verify(sshPairDTOsPromise).then(operationSshPairDTOsCapture.capture());
        operationSshPairDTOsCapture.getValue().apply(sshPairDtoArray);

        verify(service).generatePair(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE), eq(GITHUB_HOST));
        verify(service).getPairs(Matchers.eq(SshKeyManagerPresenter.VCS_SSH_SERVICE));
        verify(view).setPairs(eq(sshPairDtoArray));
    }

    @Test
    public void shouldReturnErrorOnHostNameWithHttpProtocolValidation() throws OperationException {
        String invalidHostname = "http://host.xz";
        when(constant.invalidHostName()).thenReturn("ErrorMessage");

        String errorMessage = ((InputValidator)presenter.hostNameValidator).validate(invalidHostname).getMessage();

        assertEquals("ErrorMessage", errorMessage);
    }

    @Test
    public void shouldReturnErrorOnHostNameWithHttpsProtocolValidation() throws OperationException {
        String invalidHostname = "https://host.xz";
        when(constant.invalidHostName()).thenReturn("ErrorMessage");

        String errorMessage = ((InputValidator)presenter.hostNameValidator).validate(invalidHostname).getMessage();

        assertEquals("ErrorMessage", errorMessage);
    }

    @Test
    public void shouldReturnErrorOnHostNameWithPortValidation() throws OperationException {
        String invalidHostname = "host:5005";
        when(constant.invalidHostName()).thenReturn("ErrorMessage");

        String errorMessage = ((InputValidator)presenter.hostNameValidator).validate(invalidHostname).getMessage();

        assertEquals("ErrorMessage", errorMessage);
    }

    @Test
    public void shouldReturnErrorOnHostNameThatStartsWithDotValidation() throws OperationException {
        String invalidHostname = ".host.com";
        when(constant.invalidHostName()).thenReturn("ErrorMessage");

        String errorMessage = ((InputValidator)presenter.hostNameValidator).validate(invalidHostname).getMessage();

        assertEquals("ErrorMessage", errorMessage);
    }

    @Test
    public void shouldReturnErrorOnHostNameThatStartsWithDashValidation() throws OperationException {
        String invalidHostname = "-host.com";
        when(constant.invalidHostName()).thenReturn("ErrorMessage");

        String errorMessage = ((InputValidator)presenter.hostNameValidator).validate(invalidHostname).getMessage();

        assertEquals("ErrorMessage", errorMessage);
    }

    @Test
    public void shouldReturnErrorOnHostNameThatEndsOnDotValidation() throws OperationException {
        String invalidHostname = "host.com.";
        when(constant.invalidHostName()).thenReturn("ErrorMessage");

        String errorMessage = ((InputValidator)presenter.hostNameValidator).validate(invalidHostname).getMessage();

        assertEquals("ErrorMessage", errorMessage);
    }

    @Test
    public void shouldReturnErrorOnHostNameThatEndsOnDashValidation() throws OperationException {
        String invalidHostname = "host.com-";
        when(constant.invalidHostName()).thenReturn("ErrorMessage");

        String errorMessage = ((InputValidator)presenter.hostNameValidator).validate(invalidHostname).getMessage();

        assertEquals("ErrorMessage", errorMessage);
    }

    @Test
    public void shouldReturnErrorOnHostNameWithDotAndDashTogetherValidation() throws OperationException {
        String invalidHostname = "host.-com";
        when(constant.invalidHostName()).thenReturn("ErrorMessage");

        String errorMessage = ((InputValidator)presenter.hostNameValidator).validate(invalidHostname).getMessage();

        assertEquals("ErrorMessage", errorMessage);
    }

    @Test
    public void shouldReturnNullOnValidHostNameValidation() throws OperationException {
        String validHostname = "hostname.com";

        Violation violation = ((InputValidator)presenter.hostNameValidator).validate(validHostname);

        assertNull(violation);
    }

    @Test
    public void shouldReturnNullOnHostNameWithDotAndDashValidation() throws OperationException {
        String validHostname = "host-name.com";

        Violation violation = ((InputValidator)presenter.hostNameValidator).validate(validHostname);

        assertNull(violation);
    }

    @Test
    public void shouldReturnNullOnHostNameWithSeveralDotsAndDashesValidation() throws OperationException {
        String validHostname = "ho-st.na-me.com";

        Violation violation = ((InputValidator)presenter.hostNameValidator).validate(validHostname);

        assertNull(violation);
    }
}
