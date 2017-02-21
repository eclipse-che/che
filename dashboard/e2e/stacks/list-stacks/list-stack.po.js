'use strict';

let ListStacks = function() {

  this.listElement = $('.stacks-list-content');

  // header buttons
  this.addStackButtonElement = this.listElement.$('.che-list-add-button');
  this.importButtonElement = this.listElement.$('.che-list-import-button');

  // stacks list
  this.listItemElements = this.listElement.$$('.che-list-item');
  let _splitStackItemByCell = (elem) => {
    if (!elem) {
      return null;
    }
    return {
      title: elem.$('.stack-item-name'),
      description: elem.$('.stack-item-description'),
      components: elem.$('.stack-item-description'),
      actions: elem.$('.stack-item-actions')
    };
  };
  this.getListItemElementByName = (name) => {
    let stackRowElement =  this.listItemElements.filter((elem, index) => {
      return elem.element(by.cssContainingText('.stack-item-name', name)).isPresent().then(isPresent => isPresent);
    }).get(0);
    return _splitStackItemByCell(stackRowElement);
  };
};

module.exports = new ListStacks();
