/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

import {CheAccordion} from './accordion/che-accordion.directive';
import {CheButtonPrimary} from './button/che-button-primary.directive';
import {CheButtonPrimaryFlat} from './button/che-button-primary-flat.directive';
import {CheButtonDanger} from './button/che-button-danger.directive';
import {CheButtonDefault} from './button/che-button-default.directive';
import {CheButtonNotice} from './button/che-button-notice.directive';
import {CheButtonWarning} from './button/che-button-warning.directive';
import {CheButtonSaveFlat} from './button/che-button-save-flat.directive';
import {CheButtonCancelFlat} from './button/che-button-cancel-flat.directive';
import {CheButtonDropdownCtrl} from './button-dropdown/che-button-dropdown.controller';
import {CheButtonDropdown} from './button-dropdown/che-button-dropdown.directive';
import {CheClipboard} from './copy-clipboard/che-clipboard.directive';
import {CheCompile} from './compile/che-compile.directive';
import {CheDescription} from './description/che-description.directive';
import {CheDropZoneCtrl} from './dropzone/che-dropzone.controller';
import {CheDropZone} from './dropzone/che-dropzone.directive';
import {CheEmptyState} from './empty-state/che-empty-state.directive';
import {CheFrame} from './frame/che-frame.directive';
import {CheFooter} from './footer/che-footer.directive';
import {CheHtmlSource} from './html-source/che-html-source.directive';
import {CheInput} from './input/che-input.directive';
import {CheInputBox} from './input/che-input-box.directive';
import {CheTextarea} from './input/che-textarea.directive';
import {CheLabel} from './label/che-label.directive';
import {CheLabelContainer} from './label-container/che-label-container.directive';
import {CheLearnMoreCtrl} from './learn-more/che-learn-more.controller';
import {CheLearnMore} from './learn-more/che-learn-more.directive';
import {CheLearnMoreItem} from './learn-more/che-learn-more-item.directive';
import {CheLearnMoreTemplate} from './learn-more/che-learn-more-template.directive';
import {CheLink} from './link/che-link.directive';
import {CheList} from './list/che-list.directive';
import {CheListItem} from './list/che-list-item.directive';
import {CheListHeader} from './list/che-list-header.directive';
import {CheListHeaderColumn} from './list/che-list-header-column.directive';
import {CheListTitle} from './list/che-list-title.directive';
import {CheListItemChecked} from './list/che-list-item-checked.directive';
import {CheLoader} from './loader/che-loader.directive';
import {CheLoaderCrane} from './loader/che-loader-crane.directive';
import {ChePanelCtrl} from './panel/che-panel.controller';
import {ChePanel} from './panel/che-panel.directive';
import {CheSearch} from './search/che-search.directive';
import {CheSelect} from './select/che-select.directive';
import {CheSelecterCtrl} from './selecter/che-selecter.controller';
import {CheSelecter} from './selecter/che-selecter.directive';
import {CheSlider} from './slider/che-slider.directive';
import {CheLogsOutput} from './logs-output/che-logs-output.directive';
import {CheTextInfo} from './text-info/che-text-info.directive';
import {CheToggleController} from './toggle-button/che-toggle.controller';
import {CheToggleButton} from './toggle-button/che-toggle-button.directive';
import {CheToggle} from './toggle-button/che-toggle.directive';
import {CheToolbar} from './toolbar/che-toolbar.directive';
import {CheErrorNotification} from './notification/che-error-notification.directive';
import {CheInfoNotification} from './notification/che-info-notification.directive';
import {ChePopup} from './popup/che-popup.directive';
import {CheModalPopup} from './popup/che-modal-popup.directive';
import {CheShowArea} from './show-area/che-show-area.directive';


export class WidgetConfig {

  constructor(register) {

    // accordion
    register.directive('cheAccordion', CheAccordion)

    // button
      .directive('cheButtonPrimary', CheButtonPrimary)
      .directive('cheButtonPrimaryFlat', CheButtonPrimaryFlat)
      .directive('cheButtonDanger', CheButtonDanger)
      .directive('cheButtonDefault', CheButtonDefault)
      .directive('cheButtonNotice', CheButtonNotice)
      .directive('cheButtonWarning', CheButtonWarning)
      .directive('cheButtonSaveFlat', CheButtonSaveFlat)
      .directive('cheButtonCancelFlat', CheButtonCancelFlat)
      // dropdown
      .controller('CheButtonDropdownCtrl', CheButtonDropdownCtrl)
      .directive('cheButtonDropdown', CheButtonDropdown)
      // clipboard
      .directive('cheClipboard', CheClipboard)
      .directive('cheCompile', CheCompile)
      .directive('cheDescription', CheDescription)
      // dropzone
      .controller('CheDropZoneCtrl', CheDropZoneCtrl)
      .directive('cheDropzone', CheDropZone)
      .directive('cheEmptyState', CheEmptyState)
      .directive('cheFrame', CheFrame)
      .directive('cheFooter', CheFooter)
      .directive('cheHtmlSource', CheHtmlSource)
      .directive('cheInput', CheInput)
      .directive('cheInputBox', CheInputBox)
      .directive('cheTextarea', CheTextarea)
      .directive('cheLabel', CheLabel)
      .directive('cheLabelContainer', CheLabelContainer)

      .controller('CheLearnMoreCtrl', CheLearnMoreCtrl)
      .directive('cheLearnMore', CheLearnMore)
      .directive('cheLearnMoreItem', CheLearnMoreItem)
      .directive('cheLearnMoreTemplate', CheLearnMoreTemplate)

      .directive('cheLink', CheLink)

      .directive('cheListItemChecked', CheListItemChecked)
      .directive('cheListTitle', CheListTitle)
      .directive('cheList', CheList)
      .directive('cheListItem', CheListItem)
      .directive('cheListHeader', CheListHeader)
      .directive('cheListHeaderColumn', CheListHeaderColumn)

      .directive('cheLoader', CheLoader)
      .directive('cheLoaderCrane', CheLoaderCrane)

      .controller('ChePanelCtrl', ChePanelCtrl)
      .directive('chePanel', ChePanel)

      .directive('cheSearch', CheSearch)

      .directive('cheSelect', CheSelect)

      .controller('CheSelecterCtrl', CheSelecterCtrl)
      .directive('cheSelecter', CheSelecter)

      .directive('cheSlider', CheSlider)

      .directive('cheLogsOutput', CheLogsOutput)

      .directive('cheTextInfo', CheTextInfo)

      .controller('CheToggleController', CheToggleController)
      .directive('cheToggleButton', CheToggleButton)
      .directive('cheToggle', CheToggle)

      .directive('cheToolbar', CheToolbar)
      // notifications
      .directive('cheErrorNotification', CheErrorNotification)
      .directive('cheInfoNotification', CheInfoNotification)
      // wrapper for popup
      .directive('chePopup', ChePopup)
      .directive('cheModalPopup', CheModalPopup)
      // show area
      .directive('cheShowArea', CheShowArea)
  }
}
