package org.eclipse.che.selenium.workspaces;

import static org.testng.AssertJUnit.assertEquals;

import com.google.inject.Inject;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CheckStoppingWsByTimeoutTest {

  private static int TOASTLOADER_WIDGET_LATECY_TIMEOUT_IN_MILLISEC = 20000;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private ToastLoader toastLoader;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestWorkspace testWorkspace;
  @Inject private TestUser testUser;

  @Inject
  @Named("che.workspace_agent_dev_inactive_stop_timeout_ms")
  private int stoppingTimeotInaciveWorkspace;

  @Inject
  @Named("active.state_activity_sheduler_period")
  int shedulerRequstTimeout;

  @BeforeClass
  public void setUp() throws Exception {
    int commonTimeout =
        stoppingTimeotInaciveWorkspace
            + shedulerRequstTimeout
            + TOASTLOADER_WIDGET_LATECY_TIMEOUT_IN_MILLISEC;
    ide.open(testWorkspace);
    projectExplorer.waitProjectExplorer();
    WaitUtils.sleepQuietly(commonTimeout, TimeUnit.MILLISECONDS);
  }

  @Test
  public void checkStoppingByApi() throws Exception {
    Workspace workspace =
        workspaceServiceClient.getByName(
            testWorkspace.getName(), testUser.getName(), testUser.getAuthToken());
    assertEquals(workspace.getStatus(), WorkspaceStatus.STOPPED);
  }

  @Test
  public void checkLoadToasterAfterStopping() {
    toastLoader.waitStartButtonInToastLoader();
  }
}
