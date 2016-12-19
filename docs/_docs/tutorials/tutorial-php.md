---
tags: [ "eclipse" , "che" ]
title: PHP in Che
excerpt: ""
layout: tutorials
permalink: /:categories/php/
---
# 1. Create a PHP Project  
Start Che, create a PHP project using a sample app:
![php.png]({{ base }}/assets/imgs/php.png)

# 2. Start Apache Server  
This project has 3 custom commands to start, stop and restart apache2.

`start apache` will start the server and tail apache error and access logs. It will also produce a preview URL that will point to your current project directory that Apache is already listening (Document root is changed to `projects`).

You should see a Hello World page. Open `index.php`, edit it, refresh the preview page in your browser to see live changes.
# Composer and Unit Tests  
A PHP stack has composer already installed and configured. Composer is used to manage project dependencies, i.e. makes it easy to use 3rd party libs and frameworks.

Let's add a PHPUnit framework and white a simple Unit test.

In project root, create `composer.json` file with the following contents:
```json  
 {
        "require": {
            "phpunit/phpunit": "4.3.*"
    }
```
In the `Consoles` panel, click New Terminal (+) button. This will open up a bash terminal:
```shell  
cd /projects/web-php-simple
composer update --no-dev\
```
This will install a `phpunit` framework into `vendor` directory in the project. Click `refresh` button on the project explorer panel to see this new directory.

Now, that a new framework is installed, let's write a simple test. Create `test.php` file:
```php  
<?php

require_once 'vendor/autoload.php';

class SimpleTest extends PHPUnit_Framework_TestCase {

public function testTrueIsTrue() {
    $foo = true;
    $this->assertTrue($foo);
    }
}

?>\
```
This test basically checks nothing but demonstrates use of composer-provided frameworks. Having required `vendor/autoload.php` it is possible to use any functions of `phpunit`.

Run the test:
```php  
user@460447f3f849:/projects/web-php-simple$ vendor/bin/phpunit test.php

PHPUnit 4.3.5 by Sebastian Bergmann.
Time: 17 ms, Memory: 2.50Mb                                                      
OK (1 test, 1 assertion)            \
```

# 3. Create a REST Service With Slim  
Slim makes it possible to create REST services. Let's add this framework to `composer.json`:
```json  
 {
        "require": {
            "phpunit/phpunit": "4.3.*\n            "slim/slim": "2.*"
        }
    }
```
Run `composer update --no-dev` to download all `slim` dependencies. The framework is ready to be used now. Let's modify `index.php` so that it looks like this:
```php  
<?php

require 'vendor/autoload.php';

$app = new \Slim\Slim();

$app->get('/:name', function ($name) {
   echo "Hello $name";
});

$app->run();

?>
```
This script creates a REST service that takes a path parameter and returns it as `Hello $pathParam`. So, `http://<your-che-host>:$port/$appName/Che` should return `Hello Che`.

This app will need mod_rewrite enabled to avoid using an ugly URL with `index.php` in the path. In the project root, create a `.htaccess` file:
```text  
RewriteEngine on             
RewriteRule ^ index.php [QSA,L]\
```
Apache needs to be restarted. Run `apache restart` command in CMD command widget.

Now, navigate to `http://<your-che-host>:$port/$project/Che` to find `Hello Che` in a response:
![slim.png]({{ base }}/assets/imgs/slim.png)
