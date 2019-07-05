# Zend Debugger for PHP

Support for debugging applications that runs on PHP with Zend Debugger on board.

### Requirements

Zend Debugger is a PHP extension that needs to be installed and enabled in the PHP distribution available in the Che stack. If you decide to use _ZEND_ stack to create your workspace you can skip "Zend Debugger Installation" part as Zend Debugger is already installed in _ZEND_ stack's PHP. If you decide to use default _PHP_ stack to create a workspace, you will have to follow the instructions from the next chapter to install Zend Debugger extension.

### Zend Debugger Installation

The following steps have to be carried out to install Zend Debugger in Che's default PHP stack.
* Create new workspace with the use of _PHP_ stack from the _Ready-to-go Stacks_ list
* Download _Zend Studio Web Debugger - PHP 5.5 and PHP 5.6 (64 bit)_ package from [Zend Downloads](http://www.zend.com/en/products/studio/downloads-studio#Linux)
* Add _ZendDebugger.so_ file from downloaded package to _/usr/lib/php5/20131226_
* To enable debugging applications in _CLI_ mode, add the following entry in _/etc/php5/cli/php.ini_ file:<br>
`zend_extension=ZendDebugger.so;`
* To enable debugging applications in _WEB_ mode, add the following entries in _/etc/php5/apache2/php.ini_ file:<br>
`zend_extension=ZendDebugger.so;`<br>
`opcache.enable=0;`

### Getting Started

To be able to debug PHP code, you have to start Zend Debugger client in Che and run your PHP Web/CLI application with the use of some additional parameters that are required to trigger debug session in PHP. In case of debugging PHP Web applications you can use the tools like _Z-Ray_ (available in _ZEND_ stack) or _Zend Debugger Toolbar_ for Firefox to trigger debug session without need to add debug parameters to URL that runs your PHP application.

##### Starting debug client in Che:
* Go to _Run -> Edit Debug Configurations..._
* Create new *PHP - ZEND DEBUGGER* configuration with default settings
* Press *Debug* button to start Zend Debugger client

##### Starting debug session in CLI mode:
* Use `cd <your-php-app-dir>` command in terminal
* Run PHP script in terminal with the use of the following command:<br>
`QUERY_STRING="start_debug=1&debug_host=localhost&debug_port=10137" php <your-php-app>.php`

##### Starting debug session in Web mode:
* Start Che's local PHP server with your project on board
* Open the following URL in a browser:<br>
`<your-php-app-URL>?start_debug=1&debug_host=localhost&debug_port=10137`

##### Starting debug session in Web mode with Z-Ray (ZEND stack only):
* Open an URL in a browser that runs your PHP application in the Che's local Zend Server
* Use one of the commands for starting debug session available in Z-Ray toolbar (bug icon) i.e. _Debug Current Page_

##### Starting debug session in Web mode with Zend Debugger Toolbar for Firefox:
* Install _Zend Studio Browser Toolbars - Firefox_ from [Zend Downloads](http://www.zend.com/en/products/studio/downloads-studio#Linux)
* Open an URL in a Firefox browser, that runs your PHP application in the Che's local server
* Use one of the commands for starting debug session available in Zend Debugger toolbar (bug icon) i.e. _Debug Current Page_

Happy PHPing!