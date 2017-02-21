'use strict';

let Utils = function() {

  this.getVisibleInputElement = (containerElement) => {
    return containerElement.$$('input').filter((elem, index) => {
      return elem.isDisplayed().then((isDisplayed) => {
        return isDisplayed;
      });
    }).get(0);
  };

  this.getRandomName = (name) => {
    return name + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4));
  };

};

module.exports = new Utils();
