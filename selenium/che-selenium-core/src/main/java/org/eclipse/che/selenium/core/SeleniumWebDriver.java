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
package org.eclipse.che.selenium.core;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.APPLICATION_START_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ATTACHING_ELEM_TO_DOM_SEC;
import static org.eclipse.che.selenium.core.utils.WaitUtils.sleepQuietly;
import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import javax.inject.Named;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.selenium.core.constant.TestBrowser;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for {@link WebDriver} to have ability to use in Guice container.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class SeleniumWebDriver
    implements Closeable, WebDriver, JavascriptExecutor, TakesScreenshot, HasInputDevices {
  private static final Logger LOG = LoggerFactory.getLogger(SeleniumWebDriver.class);
  private static final int MAX_ATTEMPTS = 5;
  private static final int DELAY_IN_SECONDS = 5;

  private TestBrowser browser;
  private boolean gridMode;
  private String webDriverVersion;

  private final RemoteWebDriver driver;

  @Inject
  public SeleniumWebDriver(
      @Named("sys.browser") TestBrowser browser,
      @Named("sys.driver.port") String webDriverPort,
      @Named("sys.grid.mode") boolean gridMode,
      @Named("sys.driver.version") String webDriverVersion) {
    this.browser = browser;
    this.gridMode = gridMode;
    this.webDriverVersion = webDriverVersion;

    try {
      URL webDriverUrl =
          new URL(format("http://localhost:%s%s", webDriverPort, gridMode ? "/wd/hub" : ""));
      this.driver = createDriver(webDriverUrl);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error of construction URL to web driver.", e);
    }
  }

  @Override
  public void get(String url) {
    driver.get(url);
  }

  @Override
  public String getCurrentUrl() {
    return driver.getCurrentUrl();
  }

  @Override
  public String getTitle() {
    return driver.getTitle();
  }

  @Override
  public List<WebElement> findElements(By by) {
    return driver.findElements(by);
  }

  @Override
  public WebElement findElement(By by) {
    return driver.findElement(by);
  }

  @Override
  public String getPageSource() {
    return driver.getPageSource();
  }

  @Override
  public void close() {
    driver.close();
  }

  @Override
  public void quit() {
    driver.quit();
  }

  @Override
  public Set<String> getWindowHandles() {
    return driver.getWindowHandles();
  }

  @Override
  public String getWindowHandle() {
    return driver.getWindowHandle();
  }

  @Override
  public TargetLocator switchTo() {
    return driver.switchTo();
  }

  @Override
  public Navigation navigate() {
    return driver.navigate();
  }

  @Override
  public Options manage() {
    return driver.manage();
  }

  @Override
  public Object executeScript(String script, Object... args) {
    return driver.executeScript(script, args);
  }

  @Override
  public Object executeAsyncScript(String script, Object... args) {
    return driver.executeAsyncScript(script, args);
  }

  @Override
  public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
    return driver.getScreenshotAs(target);
  }

  @Override
  public Keyboard getKeyboard() {
    return driver.getKeyboard();
  }

  @Override
  public Mouse getMouse() {
    return driver.getMouse();
  }

  private RemoteWebDriver createDriver(URL webDriverUrl) {
    for (int i = 1; ; ) {
      try {
        return doCreateDriver(webDriverUrl);
      } catch (WebDriverException e) {
        if (i++ >= MAX_ATTEMPTS) {
          String tip = getWebDriverInitTip();
          LOG.error(format("%s.%s", e.getMessage(), tip), e);
          throw new RuntimeException(format("Web driver initialization failed.%s", tip), e);
        }
      }

      sleepQuietly(DELAY_IN_SECONDS);
    }
  }

  private String getWebDriverInitTip() {
    if (!gridMode && browser.equals(TestBrowser.GOOGLE_CHROME)) {
      return getGoogleChromeTip();
    }

    return "";
  }

  private String getGoogleChromeTip() {
    try {
      URL webDriverNotes =
          new URL(
              String.format(
                  "http://chromedriver.storage.googleapis.com/%s/notes.txt", webDriverVersion));
      String supportedVersions = readSupportedVersionInfoForGoogleDriver(webDriverNotes);
      if (supportedVersions != null) {
        return format(
            "%n(Tip: there is Chrome Driver v.%s used, and it requires local Google Chrome of v.%s)",
            webDriverVersion, supportedVersions);
      }
    } catch (java.io.IOException e) {
      LOG.warn(
          "It's impossible to read info about versions of browser which Chrome Driver supports.",
          e);
    }

    return format(
        "%n(Tip: check manually if local Google Chrome browser supported by Chrome Driver v.%s at official site)",
        webDriverVersion);
  }

  /**
   * Read supported version info from official site.
   *
   * @param webDriverOfficialNotes address of official page with Google driver info
   * @return string with supported version range (for example, "36-40"), or null if version info
   *     doesn't found inside the official notes.
   * @throws IOException
   */
  @Nullable
  private String readSupportedVersionInfoForGoogleDriver(URL webDriverOfficialNotes)
      throws IOException {
    try (Scanner scanner = new Scanner(webDriverOfficialNotes.openStream(), "UTF-8")) {
      while (scanner.hasNextLine()) {
        String versionLine = scanner.findInLine("Supports Chrome v([\\d-]+)");
        if (versionLine != null) {
          return scanner.match().group(1);
        }

        scanner.nextLine();
      }
    }

    return null;
  }

  private RemoteWebDriver doCreateDriver(URL webDriverUrl) {
    DesiredCapabilities capability;

    switch (browser) {
      case GOOGLE_CHROME:
        LoggingPreferences loggingPreferences = new LoggingPreferences();
        loggingPreferences.enable(LogType.PERFORMANCE, Level.ALL);
        loggingPreferences.enable(LogType.BROWSER, Level.ALL);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--dns-prefetch-disable");

        capability = DesiredCapabilities.chrome();
        capability.setCapability(ChromeOptions.CAPABILITY, options);
        capability.setCapability(CapabilityType.LOGGING_PREFS, loggingPreferences);
        break;

      default:
        capability = DesiredCapabilities.firefox();
        capability.setCapability("dom.max_script_run_time", 240);
        capability.setCapability("dom.max_chrome_script_run_time", 240);
    }

    RemoteWebDriver driver = new RemoteWebDriver(webDriverUrl, capability);
    driver.manage().window().setSize(new Dimension(1920, 1080));

    return driver;
  }

  /** wait while in a browser appears more the 1 window */
  public void waitOpenedSomeWin() {
    new WebDriverWait(this, 30)
        .until(
            (ExpectedCondition<Boolean>)
                input -> {
                  Set<String> driverWindows = getWindowHandles();
                  return (driverWindows.size() > 1);
                });
  }

  /**
   * calculate name of workspace from browser url cut symbols from end of slash symbol ("/") to end
   *
   * @return
   */
  public String getWorkspaceNameFromBrowserUrl() {
    String currentUrl = getCurrentUrl();
    return currentUrl.substring(currentUrl.lastIndexOf("/") + 1, currentUrl.length());
  }

  /**
   * switch to the next browser window (this means that if opened 2 windows, and we are in the
   * window 1, we will be switched into the window 2 )
   *
   * @param currentWindowHandler
   */
  public void switchToNoneCurrentWindow(String currentWindowHandler) {
    waitOpenedSomeWin();
    for (String handle : getWindowHandles()) {
      if (!currentWindowHandler.equals(handle)) {
        switchTo().window(handle);
        break;
      }
    }
  }

  public void switchFromDashboardIframeToIde() {
    switchFromDashboardIframeToIde(APPLICATION_START_TIMEOUT_SEC);
  }

  public void switchFromDashboardIframeToIde(int timeout) {
    wait(timeout).until(visibilityOfElementLocated(By.id("ide-application-iframe")));

    wait(ATTACHING_ELEM_TO_DOM_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                driver ->
                    (((JavascriptExecutor) driver)
                            .executeScript("return angular.element('body').scope().showIDE"))
                        .toString()
                        .equals("true"));

    wait(timeout).until(frameToBeAvailableAndSwitchToIt(By.id("ide-application-iframe")));
  }

  public WebDriverWait wait(int timeOutInSeconds) {
    return new WebDriverWait(this, timeOutInSeconds);
  }
}
