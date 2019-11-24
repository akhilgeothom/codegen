class Main {
	a : Bool <- true;
	b : A;
	main(): Int {
		{
			(new IO).out_string("Hello World\n");
			isvoid b;
			1;
		}
	};
};

class A {
	a : Int <- 5;
	b : String <- "Hello world";
	c : Bool;
	
	foo(a : Int) : Int {
		{	
			if (a = 5) then	while(0 <= a) loop a <- a -1 pool else a <- a/5 + 10 - 4*2 fi;
			1;	
		}
	};	
};

class B inherits A {
	d : Int;
	
	bar() : Int {2};

};
