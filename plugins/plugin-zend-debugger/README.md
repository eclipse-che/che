# Zend Debugger for PHP

Support for debugging applications that runs on PHP with Zend Debugger on board.

### Requirements

Currently only local debugging is supported so Zend Debugger extension needs to be installed and enabled in PHP distribution available in Che runtime.

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

To be able to debug PHP code, you have to start Zend Debugger client in Che and run PHP application with the use of some additional parameters that are required to trigger debug session in PHP.

##### Starting debug client in Che:
* Go to _Run -> Edit Debug Configurations..._
* Create new *PHP - ZEND DEBUGGER* configuration with default settings
* Press *Debug* button to start Zend Debugger client

##### Starting debug session in CLI mode:
* Use `cd <your-php-script-dir>` command in terminal
* Run PHP script in terminal with the use of the following command:<br>
`QUERY_STRING="start_debug=1&debug_host=localhost&debug_port=10137" php <your-php-script>.php`

##### Starting debug session in WEB mode:
* Use _start apache_ command available in Che's predefined commands list to start server with your PHP project
* Open the following URL in a browser:<br>
`<your-php-script-URL>?start_debug=1&debug_host=localhost&debug_port=10137`

Happy PHPing!