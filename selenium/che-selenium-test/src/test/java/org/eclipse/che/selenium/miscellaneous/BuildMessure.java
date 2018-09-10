package org.eclipse.che.selenium.miscellaneous;

import com.google.inject.Inject;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.IncompleteArgumentException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.debugger.ChangeVariableWithEvaluatingTest;
import org.eclipse.che.selenium.debugger.DebuggerUtils;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.debug.DebugPanel;
import org.eclipse.che.selenium.pageobject.debug.JavaDebugConfig;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BuildMessure {
  private static final String PROJECT_NAME_CHANGE_VARIABLE =
      NameGenerator.generate(ChangeVariableWithEvaluatingTest.class.getSimpleName(), 2);
  private static final Logger LOG = LoggerFactory.getLogger(ChangeVariableWithEvaluatingTest.class);
  private static final String START_DEBUG_COMMAND_NAME = "startDebug";
  private static final String CLEAN_TOMCAT_COMMAND_NAME = "cleanTomcat";
  private static final String BUILD_COMMAND_NAME = "build";

  private static final String COMMAND_LAUNCHING_TOMCAT_IN_JPDA =
      "cp /projects/"
          + PROJECT_NAME_CHANGE_VARIABLE
          + "/target/qa-spring-sample-1.0-SNAPSHOT.war /home/user/tomcat8/webapps/ROOT.war"
          + " && "
          + "/home/user/tomcat8/bin/catalina.sh jpda run";

  private static final String MAVEN_BUILD_COMMAND =
      "mvn clean install -f /projects/" + PROJECT_NAME_CHANGE_VARIABLE;

  private DebuggerUtils debuggerUtils = new DebuggerUtils();

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private JavaDebugConfig debugConfig;
  @Inject private DebugPanel debugPanel;
  @Inject private ToastLoader toastLoader;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private CommandsPalette commandsPalette;
  @Inject private CheTerminal machineTerminal;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/debug-spring-project");
    testProjectServiceClient.importProject(
        ws.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME_CHANGE_VARIABLE,
        ProjectTemplates.MAVEN_SPRING);

    testCommandServiceClient.createCommand(
        COMMAND_LAUNCHING_TOMCAT_IN_JPDA,
        START_DEBUG_COMMAND_NAME,
        TestCommandsConstants.CUSTOM,
        ws.getId());

    testCommandServiceClient.createCommand(
        MAVEN_BUILD_COMMAND, BUILD_COMMAND_NAME, TestCommandsConstants.CUSTOM, ws.getId());

    String stopTomcatAndCleanWebAppDir =
        "/home/user/tomcat8/bin/shutdown.sh && rm -rf /home/user/tomcat8/webapps/*";
    testCommandServiceClient.createCommand(
        stopTomcatAndCleanWebAppDir,
        CLEAN_TOMCAT_COMMAND_NAME,
        TestCommandsConstants.CUSTOM,
        ws.getId());
    ide.open(ws);
  }

  @Test
  public void changeVariableTest() throws Exception {
    buildProjectAndOpenMainClass();
  }

  private void buildProjectAndOpenMainClass() throws IOException {
    projectExplorer.waitItem(PROJECT_NAME_CHANGE_VARIABLE);
    toastLoader.waitAppeareanceAndClosing();
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(BUILD_COMMAND_NAME);
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.BUILD_SUCCESS);
    String timeResult = getBuildTimeFromConsole().replace("[INFO] Total time: ", "") + "\n";
    Files.write(Paths.get("/home/mmusiien/tmp/results.txt"),timeResult.getBytes(), StandardOpenOption.APPEND);

  }

  private String getBuildTimeFromConsole() {
    Pattern pattern = Pattern.compile(".*Total time: \\d.*\\d");
    Matcher matcher = pattern.matcher(consoles.getVisibleTextFromCommandConsole());
    if (matcher.find()){
      return  matcher.group();
    }
    else {
      throw new RuntimeException("the build time is not found");
    }
    }





}
