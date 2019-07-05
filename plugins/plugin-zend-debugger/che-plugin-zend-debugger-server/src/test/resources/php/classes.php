<?php
class A
{
	public $fa;
	protected $fb;
	private $fc;
	
	public function A() {
	    $this->fa = "A";
	    $this->fb = 123;
	    $this->fc = array(1, 2, 3);
	}

	public function hello()
	{
		echo("Hello from A!");
	}
}

class B
{
	public function hello($p)
	{
		$v = "B";
		echo("Hello from ".$v."!");
	}
}
?>
