package org.eclipse.che.selenium.pageobject.theia;

import static org.eclipse.che.selenium.pageobject.theia.TheiaTerminal.Locators.TERMINAL_TEXT_LAYER;
import static org.eclipse.che.selenium.pageobject.theia.TheiaTerminal.Locators.TERMINAL_UPPEST_LAYER;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.INSERT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@Singleton
public class TheiaTerminal {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  private TheiaTerminal(
      SeleniumWebDriverHelper seleniumWebDriverHelper, SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.seleniumWebDriver = seleniumWebDriver;
  }

  public interface Locators {
    String TERMINAL_BODY_ID_TEMPLATE = "terminal-%s";
    String TERMINAL_TEXT_LAYER = "//canvas[@class='xterm-text-layer']";
    String TERMINAL_UPPEST_LAYER = "//canvas[@class='xterm-cursor-layer']";
  }

  private WebElement getTerminalTextLayer() {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(TERMINAL_TEXT_LAYER));
  }

  public void waitTerminal() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(TERMINAL_TEXT_LAYER));
  }

  public void clickOnTerminalTextArea() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(TERMINAL_UPPEST_LAYER));
  }

  public void type(String expectedText) {
    seleniumWebDriverHelper.sendKeys(expectedText);
  }

  private void copyTerminalTextToClipboard() {
    Dimension textLayerSize = getTerminalTextLayer().getSize();
    final Actions action = seleniumWebDriverHelper.getAction();

    final int xBeginCoordinateShift = -(textLayerSize.getWidth() / 2);
    final int yBeginCoordinateShift = -(textLayerSize.getHeight() / 2);

    seleniumWebDriverHelper.moveCursorTo(getTerminalTextLayer());

    WaitUtils.sleepQuietly(3);

    seleniumWebDriverHelper
        .getAction()
        .moveByOffset(xBeginCoordinateShift, yBeginCoordinateShift)
        .perform();

    WaitUtils.sleepQuietly(3);

    action.clickAndHold().perform();
    seleniumWebDriverHelper
        .getAction()
        .moveByOffset(textLayerSize.getWidth(), textLayerSize.getHeight())
        .perform();

    WaitUtils.sleepQuietly(3);

    action.release().perform();

    String keysCombination = Keys.chord(CONTROL, INSERT);
    seleniumWebDriverHelper.sendKeys(keysCombination);

    clickOnTerminalTextArea();
  }

  public void waitTerminalText(String expectedText) {
    copyTerminalTextToClipboard();
  }

  public String getClipboardText() {

    String result = "";

    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Clipboard clipboard = toolkit.getSystemClipboard();
    try {
      result = (String) clipboard.getData(DataFlavor.stringFlavor);
    } catch (UnsupportedFlavorException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }
}
