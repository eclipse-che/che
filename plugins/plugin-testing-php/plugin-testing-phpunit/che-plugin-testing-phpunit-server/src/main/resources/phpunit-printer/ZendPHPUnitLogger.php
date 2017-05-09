<?php
/* 
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */

if (class_exists('PHPUnit_Util_Printer')) {
    class_alias('PHPUnit_Util_Printer', 'Printer');
    class_alias('PHPUnit_Framework_TestListener', 'TestListener');
    class_alias('PHPUnit_Framework_Test', 'Test');
    class_alias('PHPUnit_Framework_TestSuite', 'TestSuite');
    class_alias('PHPUnit_Framework_TestCase', 'TestCase');
    class_alias('PHPUnit_Framework_AssertionFailedError', 'AssertionFailedError');
} else {
    class_alias('PHPUnit\Util\Printer', 'Printer');
    class_alias('PHPUnit\Framework\TestListener', 'TestListener');
    class_alias('PHPUnit\Framework\Test', 'Test');
    class_alias('PHPUnit\Framework\TestSuite', 'TestSuite');
    class_alias('PHPUnit\Framework\TestCase', 'TestCase');
    class_alias('PHPUnit\Framework\Warning', 'Warning');
    class_alias('PHPUnit\Framework\AssertionFailedError', 'AssertionFailedError');
    class_alias('PHPUnit\Framework\Exception', 'Exception2');
    class_alias('PHPUnit\Framework\ExpectationFailedException', 'ExpectationFailedException');
}

class ZendPHPUnitLogger extends Printer implements TestListener
{
    
    private $status;
    private $exception;
    private $time;
    private $warnings;
    private $varx;
    
    /**
     * data provider support - enumerates the test cases
     */
    private $dataProviderNumerator = - 1;
    
    public function __construct()
    {
        $this->cleanTest();
        
        $port = $_SERVER['ZEND_PHPUNIT_PORT'];
        if (! isset($port)) {
            $port = 7478;
        }
        $this->out = fsockopen('127.0.0.1', $port, $errno, $errstr, 5);
    }
    
    public function startTestSuite(TestSuite $suite)
    {
        $this->writeTest($suite, 'start');
    }
    
    public function startTest(Test $test)
    {
        $this->cleanTest();
        $this->writeTest($test, 'start');
        ZendPHPUnitErrorHandlerTracer::getInstance()->start();
    }
    
    public function addError(Test $test, \Exception $e, $time)
    {
        $this->status = 'error';
        $this->exception = $e;
    }
    
    public function addWarning(Test $test, Warning $e, $time) {
        $this->status = 'warning';
        $this->exception = $e;
    }
    
    public function addFailure(Test $test, AssertionFailedError $e, $time)
    {
        $this->status = 'fail';
        $this->exception = $e;
    }
    
    public function addIncompleteTest(Test $test, \Exception $e, $time)
    {
        $this->status = 'incomplete';
        $this->exception = $e;
    }
    
    public function addSkippedTest(Test $test, \Exception $e, $time)
    {
        $this->status = 'skip';
        $this->exception = $e;
    }
    
    public function endTest(Test $test, $time)
    {
        $this->warnings = ZendPHPUnitErrorHandlerTracer::getInstance()->stop();
        $this->time = $time;
        $this->writeTest($test, $this->status);
    }
    
    public function endTestSuite(TestSuite $suite)
    {
        $this->writeTest($suite, 'end');
    }
    
    public function addRiskyTest(Test $test, \Exception $e, $time)
    {}
    
    public function flush()
    {
        parent::flush();
    }
    
    private function cleanTest()
    {
        $this->status = 'pass';
        $this->exception = null;
        $this->warnings = array();
        $this->time = 0;
    }
    
    private function writeArray($array)
    {
        $result = $this->writeJson($this->encodeJson($array));
        return $result;
    }
    
    private function writeTest(Test $test, $event)
    {
        // echo out test output
        if ($test instanceof TestCase) {
            $hasPerformed = false;
            if (method_exists($test, 'hasPerformedExpectationsOnOutput')) {
                $hasPerformed = $test->hasPerformedExpectationsOnOutput();
            } else {
                $hasPerformed = $test->hasExpectationOnOutput();
            }
            
            if (! $hasPerformed && $test->getActualOutput() != null) {
                echo $test->getActualOutput();
            }
        }
        // write log
        $result = array(
            'event' => $event
        );
        if ($test instanceof TestSuite) {
            if (preg_match("*::*", $test->getName()) != 0) { // if it is a dataprovider test suite
                // $result['target'] = 'testsuite-dataprovider';
                $result['target'] = 'testsuite';
                if ($event == 'start')
                    $this->dataProviderNumerator = 0;
                    elseif ($event == 'end')
                    $this->dataProviderNumerator = - 1;
            } else {
                $result['target'] = 'testsuite';
                $this->dataProviderNumerator = - 1;
            }
            try {
                $class = new ReflectionClass($test->getName());
                $name = $class->getName();
                $file = $class->getFileName();
                $line = $class->getStartLine();
                $result['test'] = array(
                    'name' => $name,
                    'tests' => $test->count(),
                    'file' => $file,
                    'line' => $line
                );
            } catch (ReflectionException $re) {
                $name = $test->getName();
                $result['test'] = array(
                    'name' => $name,
                    'tests' => $test->count()
                );
            }
        } else { // If we're dealing with TestCase
            $result['target'] = 'testcase';
            $result['time'] = $this->time;
            $class = new ReflectionClass($test);
            try {
                $method = $class->getMethod($test->getName());
                if ($this->dataProviderNumerator < 0) {
                    $method_name = $method->getName();
                } else {
                    $method_name = $method->getName() . "[" . $this->dataProviderNumerator . "]";
                    if ($event == 'start') {
                        $this->dataProviderNumerator ++;
                    }
                }
                $result['test'] = array(
                    'name' => $method_name,
                    'file' => $method->getFileName(),
                    'line' => $method->getStartLine()
                );
            } catch (ReflectionException $re) {
                $result['test'] = array(
                    'name' => $test->getName()
                );
            }
        }
        if ($this->exception !== null) {
            $message = $this->exception->getMessage();
            $diff = "";
            if ($this->exception instanceof ExpectationFailedException) {
                if (method_exists($this->exception, "getDescription")) {
                    $message = $this->exception->getDescription();
                } else
                    if (method_exists($this->exception, "getMessage")) { // PHPUnit 3.6.3
                        $message = $this->exception->getMessage();
                }
                if (method_exists($this->exception, "getComparisonFailure") && method_exists($this->exception->getComparisonFailure(), "getDiff")) {
                    $diff = $this->exception->getComparisonFailure()->getDiff();
                }
            }
            $message = trim(preg_replace('/\s+/m', ' ', $message));
            $result += array(
                'exception' => array(
                    'message' => $message,
                    'diff' => $diff,
                    'class' => get_class($this->exception),
                    'file' => $this->exception->getFile(),
                    'line' => $this->exception->getLine(),
                    'trace' => ZendPHPUnitLogger::filterTrace($this->exception->getTrace())
                )
            );
            if (! isset($result['exception']['file'])) {
                $result['exception']['filtered'] = true;
            }
        }
        if (! empty($this->warnings)) {
            $result += array(
                'warnings' => $this->warnings
            );
        }
        if (! $this->writeArray($result)) {
            die();
        }
    }
    
    private function writeJson($buffer)
    {
        if ($this->out && ! @feof($this->out)) {
            return @fwrite($this->out, "$buffer\n");
        }
    }
    
    private function escapeString($string)
    {
        return str_replace(array(
            "\\",
            "\"",
            '/',
            "\b",
            "\f",
            "\n",
            "\r",
            "\t"
        ), array(
            '\\\\',
            '\"',
            '\/',
            '\b',
            '\f',
            '\n',
            '\r',
            '\t'
        ), $string);
    }
    
    private function encodeJson($array)
    {
        $result = '';
        if (is_scalar($array))
            $array = array(
                $array
            );
            $first = true;
            foreach ($array as $key => $value) {
                if (! $first)
                    $result .= ',';
                    else
                        $first = false;
                        $result .= sprintf('"%s":', $this->escapeString($key));
                        if (is_array($value) || is_object($value))
                            $result .= sprintf('%s', $this->encodeJson($value));
                            else
                                $result .= sprintf('"%s"', $this->escapeString($value));
            }
            return '{' . $result . '}';
    }

    public static function filterTrace($trace)
    {
        $filteredTrace = array();
        foreach ($trace as $frame) {
            if (! isset($frame['file']))
                continue;
            $filteredFrame = array(
                'file' => $frame['file'],
                'line' => $frame['line'],
                'function' => $frame['function']
            );
            if (isset($frame['class']))
                $filteredFrame += array(
                    'class' => $frame['class'],
                    'type' => $frame['type']
                );
            $filteredTrace[] = $filteredFrame;
        }
        return $filteredTrace;
    }
}

class ZendPHPUnitErrorHandlerTracer extends ZendPHPUnitErrorHandler
{
    
    private static $ZendPHPUnitErrorHandlerTracer;
    
    /**
     *
     * @return ZendPHPUnitErrorHandlerTracer
     */
    public static function getInstance()
    {
        if (self::$ZendPHPUnitErrorHandlerTracer === null) {
            self::$ZendPHPUnitErrorHandlerTracer = new self();
        }
        return self::$ZendPHPUnitErrorHandlerTracer;
    }
    
    public static $errorCodes = array(
        E_ERROR => 'Error',
        E_WARNING => 'Warning',
        E_PARSE => 'Parsing Error',
        E_NOTICE => 'Notice',
        E_CORE_ERROR => 'Core Error',
        E_CORE_WARNING => 'Core Warning',
        E_COMPILE_ERROR => 'Compile Error',
        E_COMPILE_WARNING => 'Compile Warning',
        E_USER_ERROR => 'User Error',
        E_USER_WARNING => 'User Warning',
        E_USER_NOTICE => 'User Notice',
        E_STRICT => 'Runtime Notice',
        E_RECOVERABLE_ERROR => 'Recoverable Error',
        E_DEPRECATED => 'Deprecated',
        E_USER_DEPRECATED => 'User Deprecated'
    );
    
    protected $warnings;
    
    public function handle($errno, $errstr, $errfile, $errline)
    {
        parent::handle($errno, $errstr, $errfile, $errline);
        $warning = array(
            'code' => isset(self::$errorCodes[$errno]) ? self::$errorCodes[$errno] : $errno,
            'message' => $errstr,
            'file' => $errfile,
            'line' => $errline,
            'trace' => ZendPHPUnitLogger::filterTrace(debug_backtrace()),
            'time' => PHP_Timer::resourceUsage()
        );
        $return = false;
        switch ($errno) { // ignoring user abort
            case E_USER_ERROR:
            case E_RECOVERABLE_ERROR:
                throw new ZendPHPUnitUserErrorException($warning['message'], $errno);
        }
        $this->warnings[] = $warning;
        return $return;
    }
    
    public function start()
    {
        $this->warnings = array();
        parent::start();
    }
    
    public function stop()
    {
        parent::stop();
        $return = $this->warnings;
        $this->warnings = array();
        return $return;
    }
}

class ZendPHPUnitErrorHandler
{
    
    private static $ZendPHPUnitErrorHandler;
    
    /**
     *
     * @return ZendPHPUnitErrorHandler
     */
    public static function getInstance()
    {
        if (self::$ZendPHPUnitErrorHandler === null) {
            self::$ZendPHPUnitErrorHandler = new self();
        }
        return self::$ZendPHPUnitErrorHandler;
    }
    
    public function handle($errno, $errstr, $errfile, $errline)
    {
        if (error_reporting() === 0) {
            return false;
        }
        
        if ($errfile === __FILE__ || (stripos($errfile, dirname(dirname(__FILE__))) === 0 && $errno !== E_USER_NOTICE))
            return true;
            
            // handle errors same as PHPUnit_Util_ErrorHandler
            if ($errno == E_STRICT) {
                if (PHPUnit_Framework_Error_Notice::$enabled !== TRUE) {
                    return FALSE;
                }
                
                $exception = 'PHPUnit_Framework_Error_Notice';
            }
            
            else
                if ($errno == E_WARNING) {
                    if (PHPUnit_Framework_Error_Warning::$enabled !== TRUE) {
                        return FALSE;
                    }
                    
                    $exception = 'PHPUnit_Framework_Error_Warning';
                }
            
            else
                if ($errno == E_NOTICE) {
                    trigger_error($errstr, E_USER_NOTICE);
                    return FALSE;
                }
            
            else {
                $exception = 'PHPUnit_Framework_Error';
            }
            
            throw new $exception($errstr, $errno, $errfile, $errline, $trace = null);
    }
    
    public function start()
    {
        set_error_handler(array(
            &$this,
            'handle'
        ));
    }
    
    public function stop()
    {
        restore_error_handler();
    }
}

class ZendPHPUnitUserErrorException extends Exception
{
}
