package org.eclipse.che.api.activity;

import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link WorkspaceActivityNotifier}
 *
 * @author Mihail Kuznyetsov
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceActivityNotifierTest {

    @Mock
    private HttpJsonRequestFactory requestFactory;

    private WorkspaceActivityNotifier activityNotifier;

    @BeforeMethod
    public void setUp() {
        activityNotifier = new WorkspaceActivityNotifier(requestFactory,
                                                         "localhost:8081/api",
                                                         "workspace123",
                                                         200L);
    }

    @Test
    public void shouldSendActivityRequest() {
        activityNotifier.onActivity();
        verify(requestFactory).fromUrl("localhost:8081/api/activity/workspace123");
    }

    @Test
    public void shouldSendActivityRequestOnlyAfterThreshold() throws InterruptedException {
        activityNotifier.onActivity();
        verify(requestFactory).fromUrl("localhost:8081/api/activity/workspace123");

        Thread.sleep(50L);
        activityNotifier.onActivity();

        verify(requestFactory).fromUrl("localhost:8081/api/activity/workspace123");

        Thread.sleep(200L);
        activityNotifier.onActivity();

        verify(requestFactory, times(2)).fromUrl("localhost:8081/api/activity/workspace123");
    }
}
