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
package org.eclipse.che.ide.api.theme;

/**
 * This class contains constants for style. Fields initialized from user preferences. Static methods
 * used for bridging with CssResources
 *
 * @author Evgen Vidolob
 */
public class Style {

  public static Theme theme;

  public static String getEditorTabIconColor() {
    return theme.getEditorTabIconColor();
  }

  public static String getLogoFill() {
    return theme.getLogoFill();
  }

  public static String getMainFontColor() {
    return theme.getMainFontColor();
  }

  public static String getLoaderProgressStatusColor() {
    return theme.loaderProgressStatusColor();
  }

  public static String getRadioButtonBackgroundColor() {
    return theme.getRadioButtonBackgroundColor();
  }

  public static String getCommandsToolbarBackgroundColor() {
    return theme.getCommandsToolbarBackgroundColor();
  }

  public static String getCommandsToolbarProcessesLabelBackground() {
    return theme.getCommandsToolbarProcessesLabelBackground();
  }

  public static String getCommandsToolbarProcessesLabelBorder() {
    return theme.getCommandsToolbarProcessesLabelBorder();
  }

  public static String getCommandsToolbarMachineNameColor() {
    return theme.getCommandsToolbarMachineNameColor();
  }

  public static String getCommandsToolbarCommandNameColor() {
    return theme.getCommandsToolbarCommandNameColor();
  }

  public static String getCommandEditorProjectsTableHeaderColor() {
    return theme.getCommandEditorProjectsTableHeaderColor();
  }

  public static String getCommandEditorProjectsTableRowColor() {
    return theme.getCommandEditorProjectsTableRowColor();
  }

  public static String getCommandEditorProjectSwitcherBorder() {
    return theme.getCommandEditorProjectSwitcherBorder();
  }

  public static String getDialogContentBackground() {
    return theme.getDialogContentBackground();
  }

  public static String getDropdownListBackground() {
    return theme.getDropdownListBackground();
  }

  public static String getHoveredDropdownListBackground() {
    return theme.getHoveredDropdownListBackground();
  }

  public static String getActiveDropdownListBackground() {
    return theme.getActiveDropdownListBackground();
  }

  public static String getDropdownListBorder() {
    return theme.getDropdownListBorder();
  }

  public static String getDropdownListButtonColor() {
    return theme.getDropdownListButtonColor();
  }

  public static String getMenuButtonBackground() {
    return theme.getMenuButtonBackground();
  }

  public static String getHoveredMenuButtonBackground() {
    return theme.getHoveredMenuButtonBackground();
  }

  public static String getActiveMenuButtonBackground() {
    return theme.getActiveMenuButtonBackground();
  }

  public static String getActiveMenuButtonBorder() {
    return theme.getMenuButtonBorder();
  }

  public static String getButtonBackground() {
    return theme.getButtonBackground();
  }

  public static String getButtonBorderColor() {
    return theme.getButtonBorderColor();
  }

  public static String getButtonFontColor() {
    return theme.getButtonFontColor();
  }

  public static String getButtonHoverBackground() {
    return theme.getButtonHoverBackground();
  }

  public static String getButtonHoverBorderColor() {
    return theme.getButtonHoverBorderColor();
  }

  public static String getButtonHoverFontColor() {
    return theme.getButtonHoverFontColor();
  }

  public static String getButtonClickedBackground() {
    return theme.getButtonClickedBackground();
  }

  public static String getButtonClickedBorderColor() {
    return theme.getButtonClickedBorderColor();
  }

  public static String getButtonClickedFontColor() {
    return theme.getButtonClickedFontColor();
  }

  public static String getButtonDisabledBackground() {
    return theme.getButtonDisabledBackground();
  }

  public static String getButtonDisabledBorderColor() {
    return theme.getButtonDisabledBorderColor();
  }

  public static String getButtonDisabledFontColor() {
    return theme.getButtonDisabledFontColor();
  }

  public static String getPrimaryButtonBackground() {
    return theme.getPrimaryButtonBackground();
  }

  public static String getPrimaryButtonBorderColor() {
    return theme.getPrimaryButtonBorderColor();
  }

  public static String getPrimaryButtonFontColor() {
    return theme.getPrimaryButtonFontColor();
  }

  public static String getPrimaryButtonHoverBackground() {
    return theme.getPrimaryButtonHoverBackground();
  }

  public static String getPrimaryButtonHoverBorderColor() {
    return theme.getPrimaryButtonHoverBorderColor();
  }

  public static String getPrimaryButtonHoverFontColor() {
    return theme.getPrimaryButtonHoverFontColor();
  }

  public static String getPrimaryButtonClickedBackground() {
    return theme.getPrimaryButtonClickedBackground();
  }

  public static String getPrimaryButtonClickedBorderColor() {
    return theme.getPrimaryButtonClickedBorderColor();
  }

  public static String getPrimaryButtonClickedFontColor() {
    return theme.getPrimaryButtonClickedFontColor();
  }

  public static String getPrimaryButtonDisabledBackground() {
    return theme.getPrimaryButtonDisabledBackground();
  }

  public static String getPrimaryButtonDisabledBorderColor() {
    return theme.getPrimaryButtonDisabledBorderColor();
  }

  public static String getPrimaryButtonDisabledFontColor() {
    return theme.getPrimaryButtonDisabledFontColor();
  }

  public static String getNotableButtonTopColor() {
    return theme.getNotableButtonTopColor();
  }

  public static String getNotableButtonColor() {
    return theme.getNotableButtonColor();
  }

  public static String getSocialButtonColor() {
    return theme.getSocialButtonColor();
  }

  public static String getEditorBackgroundColor() {
    return theme.getEditorBackgroundColor();
  }

  public static String getEditorCurrentLineColor() {
    return theme.getEditorCurrentLineColor();
  }

  public static String getEditorDefaultFontColor() {
    return theme.getEditorDefaultFontColor();
  }

  public static String getEditorSelectionColor() {
    return theme.getEditorSelectionColor();
  }

  public static String getEditorInactiveSelectionColor() {
    return theme.getEditorInactiveSelectionColor();
  }

  public static String getEditorCursorColor() {
    return theme.getEditorCursorColor();
  }

  public static String getEditorGutterColor() {
    return theme.getEditorGutterColor();
  }

  // syntax
  public static String getEditorKeyWord() {
    return theme.getEditorKeyWord();
  }

  public static String getEditorAtom() {
    return theme.getEditorAtom();
  }

  public static String getEditorNumber() {
    return theme.getEditorNumber();
  }

  public static String getEditorDef() {
    return theme.getEditorDef();
  }

  public static String getEditorVariable() {
    return theme.getEditorVariable();
  }

  public static String getEditorVariable2() {
    return theme.getEditorVariable2();
  }

  public static String getEditorProperty() {
    return theme.getEditorProperty();
  }

  public static String getEditorOperator() {
    return theme.getEditorOperator();
  }

  public static String getEditorComment() {
    return theme.getEditorComment();
  }

  public static String getEditorString() {
    return theme.getEditorString();
  }

  public static String getEditorString2() {
    return theme.getEditorString2();
  }

  public static String getEditorMeta() {
    return theme.getEditorMeta();
  }

  public static String getEditorError() {
    return theme.getEditorError();
  }

  public static String getEditorBuiltin() {
    return theme.getEditorBuiltin();
  }

  public static String getEditorTag() {
    return theme.getEditorTag();
  }

  public static String getEditorAttribute() {
    return theme.getEditorAttribute();
  }

  public static String getWindowContentBackground() {
    return theme.getWindowContentBackground();
  }

  public static String getWindowContentFontColor() {
    return theme.getWindowContentFontColor();
  }

  public static String getWindowShadowColor() {
    return theme.getWindowShadowColor();
  }

  public static String getWindowHeaderBackground() {
    return theme.getWindowHeaderBackground();
  }

  public static String getWindowHeaderBorderColor() {
    return theme.getWindowHeaderBorderColor();
  }

  public static String getWindowFooterBackground() {
    return theme.getWindowFooterBackground();
  }

  public static String getWindowFooterBorderColor() {
    return theme.getWindowFooterBorderColor();
  }

  public static String getWindowSeparatorColor() {
    return theme.getWindowSeparatorColor();
  }

  public static String getWindowTitleFontColor() {
    return theme.getWindowTitleFontColor();
  }

  public static String getWizardStepsColor() {
    return theme.getWizardStepsColor();
  }

  public static String getWizardStepsBorderColor() {
    return theme.getWizardStepsBorderColor();
  }

  public static String getWelcomeFontColor() {
    return theme.getWelcomeFontColor();
  }

  public static String getCaptionFontColor() {
    return theme.getCaptionFontColor();
  }

  public static String getFactoryLinkColor() {
    return theme.getFactoryLinkColor();
  }

  public static String getStatusPanelColor() {
    return theme.getStatusPanelColor();
  }

  public static String getCellOddRow() {
    return theme.getCellOddRowColor();
  }

  public static String getCellEvenRow() {
    return theme.getCellOddEvenColor();
  }

  public static String getCellKeyboardSelectedRow() {
    return theme.getCellKeyboardSelectedRowColor();
  }

  public static String getCellHoveredRow() {
    return theme.getCellHoveredRow();
  }

  public static String getMainMenuBkgColor() {
    return theme.getMainMenuBkgColor();
  }

  public static String getMainMenuSelectedBkgColor() {
    return theme.getMainMenuSelectedBkgColor();
  }

  public static String getMainMenuSelectedBorderColor() {
    return theme.getMainMenuSelectedBorderColor();
  }

  public static String getMainMenuFontColor() {
    return theme.getMainMenuFontColor();
  }

  public static String getMainMenuFontHoverColor() {
    return theme.getMainMenuFontHoverColor();
  }

  public static String getMainMenuFontSelectedColor() {
    return theme.getMainMenuFontSelectedColor();
  }

  public static String getToolbarBackgroundImage() {
    return theme.getToolbarBackgroundImage();
  }

  public static String getToolbarActionGroupShadowColor() {
    return theme.getToolbarActionGroupShadowColor();
  }

  public static String getToolbarActionGroupBackgroundColor() {
    return theme.getToolbarActionGroupBackgroundColor();
  }

  public static String getToolbarActionGroupBorderColor() {
    return theme.getToolbarActionGroupBorderColor();
  }

  public static String getToolbarBackgroundColor() {
    return theme.getToolbarBackgroundColor();
  }

  public static String getToolbarIconColor() {
    return theme.getToolbarIconColor();
  }

  public static String getToolbarHoverIconColor() {
    return theme.getToolbarHoverIconColor();
  }

  public static String getToolbarSelectedIconFilter() {
    return theme.getToolbarSelectedIconFilter();
  }

  public static String getTooltipBackgroundColor() {
    return theme.getTooltipBackgroundColor();
  }

  public static String getPerspectiveSwitcherBackgroundColor() {
    return theme.getPerspectiveSwitcherBackgroundColor();
  }

  public static String getSelectCommandActionIconColor() {
    return theme.getSelectCommandActionIconColor();
  }

  public static String getSelectCommandActionIconBackgroundColor() {
    return theme.getSelectCommandActionIconBackgroundColor();
  }

  public static String getSelectCommandActionColor() {
    return theme.getSelectCommandActionColor();
  }

  public static String getSelectCommandActionHoverColor() {
    return theme.getSelectCommandActionHoverColor();
  }

  public static String getSuccessEventColor() {
    return theme.getSuccessEventColor();
  }

  public static String getErrorEventColor() {
    return theme.getErrorEventColor();
  }

  public static String getLinkColor() {
    return theme.getLinkColor();
  }

  public static String getDelimeterColor() {
    return theme.getDelimeterColor();
  }

  public static String getOutputLinkColor() {
    return theme.getOutputLinkColor();
  }

  public static String getEditorInfoBackgroundColor() {
    return theme.getEditorInfoBackgroundColor();
  }

  public static String getEditorInfoTextColor() {
    return theme.editorInfoTextColor();
  }

  public static String getEditorInfoBorderColor() {
    return theme.getEditorInfoBorderColor();
  }

  public static String getEditorInfoBorderShadowColor() {
    return theme.getEditorInfoBorderShadowColor();
  }

  public static String getEditorLineNumberColor() {
    return theme.getEditorLineNumberColor();
  }

  public static String getEditorSeparatorColor() {
    return theme.getEditorSeparatorColor();
  }

  public static String getBlueIconColor() {
    return theme.getBlueIconColor();
  }

  public static String getSplitterSmallBorderColor() {
    return theme.getSplitterSmallBorderColor();
  }

  public static String getSplitterLargeBorderColor() {
    return theme.getSplitterLargeBorderColor();
  }

  public static String getBadgeBackgroundColor() {
    return theme.getBadgeBackgroundColor();
  }

  public static String getBadgeFontColor() {
    return theme.getBadgeFontColor();
  }

  public static String getPopupBkgColor() {
    return theme.getPopupBkgColor();
  }

  public static String getPopupBorderColor() {
    return theme.getPopupBorderColor();
  }

  public static String getPopupShadowColor() {
    return theme.getPopupShadowColor();
  }

  public static String getPopupHoverColor() {
    return theme.getPopupHoverColor();
  }

  public static String getPopupHotKeyColor() {
    return theme.getPopupHotKeyColor();
  }

  public static String getTextFieldTitleColor() {
    return theme.getTextFieldTitleColor();
  }

  public static String getTextFieldColor() {
    return theme.getTextFieldColor();
  }

  public static String getTextFieldBackgroundColor() {
    return theme.getTextFieldBackgroundColor();
  }

  public static String getTextFieldFocusedColor() {
    return theme.getTextFieldFocusedColor();
  }

  public static String getTextFieldFocusedBackgroundColor() {
    return theme.getTextFieldFocusedBackgroundColor();
  }

  public static String getTextFieldDisabledColor() {
    return theme.getTextFieldDisabledColor();
  }

  public static String getTextFieldDisabledBackgroundColor() {
    return theme.getTextFieldDisabledBackgroundColor();
  }

  public static String getTextFieldBorderColor() {
    return theme.getTextFieldBorderColor();
  }

  public static String getMenuBackgroundColor() {
    return theme.getMenuBackgroundColor();
  }

  public static String getMenuBackgroundImage() {
    return theme.getMenuBackgroundImage();
  }

  public static String getPanelBackgroundColor() {
    return theme.getPanelBackgroundColor();
  }

  public static String getPrimaryHighlightsColor() {
    return theme.getPrimaryHighlightColor();
  }

  public static String getSeparatorColor() {
    return theme.getSeparatorColor();
  }

  public static String getErrorColor() {
    return theme.getErrorColor();
  }

  public static String getSuccessColor() {
    return theme.getSuccessColor();
  }

  public static String getListBoxHoverBackgroundColor() {
    return theme.getListBoxHoverBackgroundColor();
  }

  public static String getListBoxColor() {
    return theme.getListBoxColor();
  }

  public static String getListBoxDisabledColor() {
    return theme.getListBoxDisabledColor();
  }

  public static String getListBoxDisabledBackgroundColor() {
    return theme.getListBoxDisabledBackgroundColor();
  }

  public static String getListBoxDropdownBackgroundColor() {
    return theme.getListBoxDropdownBackgroundColor();
  }

  public static String getVcsConsoleStagedFilesColor() {
    return theme.vcsConsoleStagedFilesColor();
  }

  public static String getVcsConsoleUnstagedFilesColor() {
    return theme.vcsConsoleUnstagedFilesColor();
  }

  public static String getVcsConsoleErrorColor() {
    return theme.vcsConsoleErrorColor();
  }

  public static String getVcsConsoleModifiedFilesColor() {
    return theme.vcsConsoleModifiedFilesColor();
  }

  public static String getVcsConsoleChangesLineNumbersColor() {
    return theme.vcsConsoleChangesLineNumbersColor();
  }

  public static String getVcsStatusAddedColor() {
    return theme.vcsStatusAddedColor();
  }

  public static String getVcsStatusModifiedColor() {
    return theme.vcsStatusModifiedColor();
  }

  public static String getVcsStatusUntrackedColor() {
    return theme.vcsStatusUntrackedColor();
  }
}
