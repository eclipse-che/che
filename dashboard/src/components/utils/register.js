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

/**
 * A helper class to simplify registering Angular components and provide a consistent syntax for doing so.
 * https://raw.githubusercontent.com/michaelbromley/angular-es6/master/src/app/utils/register.js
 * Florent: Add userDashboard module and transform it to a class
 */
export class Register {
  constructor(moduleApp) {
    this.app = moduleApp;
  }



  directive(name, constructorFn) {

    constructorFn = this._normalizeConstructor(constructorFn);

    if (!constructorFn.prototype.compile) {
      // create an empty compile function if none was defined.
      constructorFn.prototype.compile = () => {};
    }

    var originalCompileFn = this._cloneFunction(constructorFn.prototype.compile);

    // Decorate the compile method to automatically return the link method (if it exists)
    // and bind it to the context of the constructor (so `this` works correctly).
    // This gets around the problem of a non-lexical "this" which occurs when the directive class itself
    // returns `this.link` from within the compile function.
    this._override(constructorFn.prototype, 'compile', function () {
      return function () {
        originalCompileFn.apply(this, arguments);

        if (constructorFn.prototype.link) {
          return constructorFn.prototype.link.bind(this);
        }
      };
    });

    var factoryArray = this._createFactoryArray(constructorFn);

    this.app.directive(name, factoryArray);
    return this;
  }


  filter(name, contructorFn) {
    this.app.filter(name, contructorFn);
    return this;
  }

  controller(name, contructorFn) {
    this.app.controller(name, contructorFn);
    return this;
  }

  service(name, contructorFn) {
    this.app.service(name, contructorFn);
    return this;
  }

  provider(name, constructorFn) {
    this.app.provider(name, constructorFn);
    return this;
  }

   factory(name, constructorFn) {
    constructorFn = this._normalizeConstructor(constructorFn);
    var factoryArray = this._createFactoryArray(constructorFn);
    this.app.factory(name, factoryArray);
    return this;
  }

  /**
   * If the constructorFn is an array of type ['dep1', 'dep2', ..., constructor() {}]
   * we need to pull out the array of dependencies and add it as an $inject property of the
   * actual constructor function.
   * @param input
   * @returns {*}
   * @private
   */
  _normalizeConstructor(input) {
    var constructorFn;

    if (!input) {
      var stack = new Error().stack;
      console.log('Invalid constructor', stack);
    }

    if (input.constructor === Array) {
      //
      var injected = input.slice(0, input.length - 1);
      constructorFn = input[input.length - 1];
      constructorFn.$inject = injected;
    } else {
      constructorFn = input;
    }

    return constructorFn;
  }

  /**
   * Convert a constructor function into a factory function which returns a new instance of that
   * constructor, with the correct dependencies automatically injected as arguments.
   *
   * In order to inject the dependencies, they must be attached to the constructor function with the
   * `$inject` property annotation.
   *
   * @param ConstructorFn
   * @returns {Array.<T>}
   * @private
   */
  _createFactoryArray(ConstructorFn) {
    // get the array of dependencies that are needed by this component (as contained in the `$inject` array)
    var args = ConstructorFn.$inject || [];
    var factoryArray = args.slice(); // create a copy of the array
    // The factoryArray uses Angular's array notation whereby each element of the array is the name of a
    // dependency, and the final item is the factory function itself.
    factoryArray.push((...args) => {
      //return new constructorFn(...args);
      var instance = new ConstructorFn(...args);
      for (var key in instance) {
        instance[key] = instance[key];
      }
      return instance;
    });

    return factoryArray;
  }

  /**
   * Clone a function
   * @param original
   * @returns {Function}
   */
  _cloneFunction(original) {
    return function() {
      return original.apply(this, arguments);
    };
  }

  /**
   * Override an object's method with a new one specified by `callback`.
   * @param object
   * @param methodName
   * @param callback
   */
  _override(object, methodName, callback) {
    object[methodName] = callback(object[methodName]);
  }

}

//export default Register;
