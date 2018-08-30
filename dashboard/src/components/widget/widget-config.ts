/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
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
import {CheButtonDropdownDirective} from './button-dropdown/che-button-dropdown.directive';
import {CheClipboard} from './copy-clipboard/che-clipboard.directive';
import {CheCompile} from './compile/che-compile.directive';
import {CheDescription} from './description/che-description.directive';
import {CheDropZoneCtrl} from './dropzone/che-dropzone.controller';
import {CheDropZone} from './dropzone/che-dropzone.directive';
import {CheEmptyState} from './empty-state/che-empty-state.directive';
import {CheFilterSelector} from './filter-selector/che-filter-selector.directive';
import {CheFilterSelectorController} from './filter-selector/che-filter-selector.controller';
import {CheFrame} from './frame/che-frame.directive';
import {CheFooter} from './footer/che-footer.directive';
import {CheFooterController} from './footer/che-footer.controller';
import {CheHtmlSource} from './html-source/che-html-source.directive';
import {CheInput} from './input/che-input.directive';
import {CheInputBox} from './input/che-input-box.directive';
import {CheTextarea} from './input/che-textarea.directive';
import {CheNumberSpinner} from './input/che-number-spinner.directive';
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
import {CheListHelperFactory} from './list/che-list-helper.factory';
import {CheListHeaderAdditionalParts} from './list/list-header/additional-parts/che-list-header-additional-parts.directive';
import {CheListHeaderAddButton} from './list/list-header/additional-parts/parts/che-list-header-add-button.directive';
import {CheListHeaderImportButton} from './list/list-header/additional-parts/parts/che-list-header-import-button.directive';
import {CheListHeaderSearch} from './list/list-header/additional-parts/parts/che-list-header-search.directive';
import {CheListHeaderFilter} from './list/list-header/additional-parts/parts/che-list-header-filter.directive';
import {CheListHeaderDeleteButton} from './list/list-header/additional-parts/parts/che-list-header-delete-button.directive';
import {CheLoader} from './loader/che-loader.directive';
import {CheLoaderCrane} from './loader/che-loader-crane.directive';
import {ChePanelCtrl} from './panel/che-panel.controller';
import {ChePanel} from './panel/che-panel.directive';
import {CheSearch} from './search/che-search.directive';
import {SearchInput} from './search/search-input.directive';
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
import {DemoSourceRender} from './html-source/demo-source-render.directive';
import {ToggleSingleButton} from './toggle-button/toggle-single-button.directive';
import {CheToggleJoinedButton} from './toggle-button/che-toggle-joined-button.directive';
import {CheToggleJoined} from './toggle-button/che-toggle-joined.directive';
import {CheChipsList} from './chips-list/chips-list.directive';
import {CheToggleButtonPopover} from './popover/che-toggle-button-popover.directive';
import {CheTogglePopover} from './popover/che-toggle-popover.directive';
import {CheEditor} from './editor/che-editor.directive';
import {CheEditorController} from './editor/che-editor.controller';
import {PagingButtons} from './paging-button/paging-button.directive';
import {CheRowToolbar} from './toolbar/che-row-toolbar.directive';
import {CheEditModeOverlay} from './edit-mode-overlay/che-edit-mode-overlay.directive';

export class WidgetConfig {

  constructor(register: che.IRegisterService) {

    // accordion
    register.directive('cheAccordion', CheAccordion);

    // button
    register.directive('cheButtonPrimary', CheButtonPrimary);
    register.directive('cheButtonPrimaryFlat', CheButtonPrimaryFlat);
    register.directive('cheButtonDanger', CheButtonDanger);
    register.directive('cheButtonDefault', CheButtonDefault);
    register.directive('cheButtonNotice', CheButtonNotice);
    register.directive('cheButtonWarning', CheButtonWarning);
    register.directive('cheButtonSaveFlat', CheButtonSaveFlat);
    register.directive('cheButtonCancelFlat', CheButtonCancelFlat);
    // paging buttons
    register.directive('chePagingButtons', PagingButtons);
    // dropdown
    register.directive('cheButtonDropdown', CheButtonDropdownDirective);
    // clipboard
    register.directive('cheClipboard', CheClipboard);
    register.directive('cheCompile', CheCompile);
    register.directive('cheDescription', CheDescription);
    // dropzone
    register.controller('CheDropZoneCtrl', CheDropZoneCtrl);
    register.directive('cheDropzone', CheDropZone);
    register.directive('cheEmptyState', CheEmptyState);
    register.directive('cheFilterSelector', CheFilterSelector);
    register.controller('CheFilterSelectorController', CheFilterSelectorController);
    register.directive('cheFrame', CheFrame);
    register.directive('cheFooter', CheFooter);
    register.controller('CheFooterController', CheFooterController);
    register.directive('cheHtmlSource', CheHtmlSource);
    register.directive('demoSourceRender', DemoSourceRender);
    register.directive('cheInput', CheInput);
    register.directive('cheInputBox', CheInputBox);
    register.directive('cheTextarea', CheTextarea);
    register.directive('cheNumberSpinner', CheNumberSpinner);
    register.directive('cheLabel', CheLabel);
    register.directive('cheLabelContainer', CheLabelContainer);

    register.controller('CheLearnMoreCtrl', CheLearnMoreCtrl);
    register.directive('cheLearnMore', CheLearnMore);
    register.directive('cheLearnMoreItem', CheLearnMoreItem);
    register.directive('cheLearnMoreTemplate', CheLearnMoreTemplate);

    register.directive('cheLink', CheLink);

    register.directive('cheListItemChecked', CheListItemChecked);
    register.directive('cheListTitle', CheListTitle);
    register.directive('cheList', CheList);
    register.directive('cheListItem', CheListItem);
    register.directive('cheListHeader', CheListHeader);
    register.directive('cheListHeaderColumn', CheListHeaderColumn);
    register.factory('cheListHelperFactory', CheListHelperFactory);

    register.directive('cheListHeaderAdditionalParts', CheListHeaderAdditionalParts);
    register.directive('cheListHeaderAddButton', CheListHeaderAddButton);
    register.directive('cheListHeaderImportButton', CheListHeaderImportButton);
    register.directive('cheListHeaderDeleteButton', CheListHeaderDeleteButton);
    register.directive('cheListHeaderSearch', CheListHeaderSearch);
    register.directive('cheListHeaderFilter', CheListHeaderFilter);

    register.directive('cheLoader', CheLoader);
    register.directive('cheLoaderCrane', CheLoaderCrane);

    register.controller('ChePanelCtrl', ChePanelCtrl);
    register.directive('chePanel', ChePanel);

    register.directive('cheSearch', CheSearch);
    register.directive('searchInput', SearchInput);

    register.directive('cheSelect', CheSelect);

    register.controller('CheSelecterCtrl', CheSelecterCtrl);
    register.directive('cheSelecter', CheSelecter);

    register.directive('cheSlider', CheSlider);

    register.directive('cheLogsOutput', CheLogsOutput);

    register.directive('cheTextInfo', CheTextInfo);

    register.controller('CheToggleController', CheToggleController);
    register.directive('cheToggleButton', CheToggleButton);
    register.directive('cheToggle', CheToggle);
    register.directive('cheToggleJoined', CheToggleJoined);
    register.directive('cheToggleJoinedButton', CheToggleJoinedButton);
    register.directive('toggleSingleButton', ToggleSingleButton);

    register.directive('cheToolbar', CheToolbar);
    // notifications
    register.directive('cheErrorNotification', CheErrorNotification);
    register.directive('cheInfoNotification', CheInfoNotification);
    // wrapper for popup
    register.directive('chePopup', ChePopup);
    register.directive('cheModalPopup', CheModalPopup);
    // show area
    register.directive('cheShowArea', CheShowArea);
    // tags
    register.directive('cheChipsList', CheChipsList);
    // editor
    register.controller('CheEditorController', CheEditorController);
    register.directive('cheEditor', CheEditor);
    // row toolbar
    register.directive('cheRowToolbar', CheRowToolbar);
    // popover
    register.directive('cheTogglePopover', CheTogglePopover);
    register.directive('toggleButtonPopover', CheToggleButtonPopover);
    // edit overlay
    register.directive('cheEditModeOverlay', CheEditModeOverlay);
  }
}
