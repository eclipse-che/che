<?php
/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */

if (class_exists('PHPUnit_TextUI_ResultPrinter')) {
    class_alias('PHPUnit_TextUI_ResultPrinter', 'Printer');
    class_alias('PHPUnit_Framework_TestListener', 'TestListener');
    class_alias('PHPUnit_Framework_Test', 'Test');
    class_alias('PHPUnit_Framework_TestSuite', 'TestSuite');
    class_alias('PHPUnit_Framework_TestCase', 'TestCase');
    class_alias('PHPUnit_Framework_AssertionFailedError', 'AssertionFailedError');
} else {
    class_alias('PHPUnit\TextUI\ResultPrinter', 'Printer');
    class_alias('PHPUnit\Framework\TestListener', 'TestListener');
    class_alias('PHPUnit\Framework\Test', 'Test');
    class_alias('PHPUnit\Framework\TestSuite', 'TestSuite');
    class_alias('PHPUnit\Framework\TestResult', 'TestResult');
    class_alias('PHPUnit\Framework\TestCase', 'TestCase');
    class_alias('PHPUnit\Framework\Warning', 'Warning');
    class_alias('PHPUnit\Framework\AssertionFailedError', 'AssertionFailedError');
    class_alias('PHPUnit\Framework\Exception', 'Exception2');
    class_alias('PHPUnit\Framework\ExpectationFailedException', 'ExpectationFailedException');
}

class ZendPHPUnitLogger extends Printer
{

    private $status;
    private $exception;
    private $time;
    private $warnings;
    private $varx;

    /**
     * data provider support - enumerates the test cases
     */
    private $dataProviderNumerator = -1;

    public function __construct()
    {
        $this->cleanTest();
        $this->out = null;
        printf("%s\n", $this->create("testReporterAttached", null));
        printf("%s\n", $this->create("rootName", array('name' => "PHPUnit Default Suite")));
    }

    public function startTestSuite(TestSuite $suite)
    {
        $this->dataProviderNumerator = 0;
        printf("%s\n", $this->create("testSuiteStarted",
            array('name' => $suite->getName(),
                'location' => $suite->getName())));
    }

    public function startTest(Test $test)
    {
        printf("%s\n", $this->create("testStarted",
            array('name' => $test->getName(),
                'location' => $this->buildLocation($test, 'start'))));
    }

    public function addError(Test $test, \Exception $e, $time)
    {
        printf("%s\n", $this->create("testFailed",
            array('name' => $test->getName(),
                'duration' => $time,
                'location' => $this->buildLocation($test, 'error'))));
    }

    public function addWarning(Test $test, Warning $e, $time)
    {
        printf("%s\n", $this->create("testFinished",
            array('name' => $test->getName(),
                'duration' => $time,
                'location' => $this->buildLocation($test, 'warning'))));
    }

    public function addFailure(Test $test, AssertionFailedError $e, $time)
    {
        printf("%s\n", $this->create("testFailed",
            array('name' => $test->getName(),
                'duration' => $time,
                'location' => $this->buildLocation($test, 'failure'))));
    }

    public function addIncompleteTest(Test $test, \Exception $e, $time)
    {
        $this->status = 'incomplete';
        $this->exception = $e;
    }

    public function addSkippedTest(Test $test, \Exception $e, $time)
    {
        printf("%s\n", $this->create("testIgnored",
            array('name' => $test->getName(),
                'duration' => $time,
                'location' => $this->buildLocation($test, 'skip'))));
    }

    public function endTest(Test $test, $time)
    {
        $hasPerformed = false;
        if (method_exists($test, 'hasPerformedExpectationsOnOutput')) {
            $hasPerformed = $test->hasPerformedExpectationsOnOutput();
        } else {
            $hasPerformed = $test->hasExpectationOnOutput();
        }

        if (!$hasPerformed && $test->getActualOutput() != null) {
            printf("%s\n", $test->getActualOutput());
        }
        printf("%s\n", $this->create("testFinished",
            array('name' => $test->getName(),
                'duration' => $time,
                'location' => $this->buildLocation($test, 'end'))));
    }

    public function endTestSuite(TestSuite $suite)
    {
        $this->dataProviderNumerator = -1;
        printf("%s\n", $this->create("testSuiteFinished",
            array('name' => $suite->getName(),
                'location' => $suite->getName())));
    }

    public function addRiskyTest(Test $test, \Exception $e, $time)
    {
    }

    public function flush()
    {
        parent::flush();
    }

    private function create($name, $attributes)
    {
        $result = "@@<{\"name\":";
        $result .= '"';
        $result .= $name;
        $result .= '"';
        if ($attributes !== null) {
            $result .= ", \"attributes\":{";
            foreach ($attributes as $key => $value) {
                $result .= sprintf('"%s":', $this->escapeString($key));
                if (is_array($value) || is_object($value))
                    $result .= sprintf('%s', $this->encodeJson($value));
                else
                    $result .= sprintf('"%s"', $this->escapeString($value));
                $result .= ',';
            }
            $result = substr($result, 0, -1);
            $result .= '}';
        }
        $result .= '}>';
        return $result;
    }

    private function buildLocation(Test $test, $event)
    {
        $class = new ReflectionClass($test);
        try {
            $method = $class->getMethod($test->getName());
            if ($this->dataProviderNumerator < 0) {
                $method_name = $method->getName();
            } else {
                $method_name = $method->getName() . "[" . $this->dataProviderNumerator . "]";
                if ($event == 'start') {
                    $this->dataProviderNumerator++;
                }
            }
            return $method->getFileName() . "." . $method_name . ":" . $method->getStartLine();
        } catch (ReflectionException $re) {
            return $test->getName();
        }
    }

    private function cleanTest()
    {
        $this->status = 'pass';
        $this->exception = null;
        $this->warnings = array();
        $this->time = 0;
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
            if (!$first)
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
            if (!isset($frame['file']))
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
        } else
            if ($errno == E_WARNING) {
                if (PHPUnit_Framework_Error_Warning::$enabled !== TRUE) {
                    return FALSE;
                }

                $exception = 'PHPUnit_Framework_Error_Warning';
            } else
                if ($errno == E_NOTICE) {
                    trigger_error($errstr, E_USER_NOTICE);
                    return FALSE;
                } else {
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
