-- Program to demonstrate weird loop constructs with and without if-then-else statements in them
class Main{
	a : Looper <- new Looper;
	b : IO <- new IO;
	main() : Object {
		{
			b@IO.out_int(a@Looper.loop_trial_1(1, 12, 2));
			b@IO.out_int(a@Looper.loop_trial_2(11, 3, 10));
			b@IO.out_int(a@Looper.loop_trial_3());
		}
	};
};

class Looper {
	i : IO <- new IO;
	loop_trial_1(a : Int, b : Int, c : Int) : Int {
		{
			while a <= 10 
			loop {
				i@IO.out_string("a : ");
				i@IO.out_int(a);
				i@IO.out_string("\n");

				i@IO.out_string("b : ");
				i@IO.out_int(b);
				i@IO.out_string("\n");

				i@IO.out_string("c : ");
				i@IO.out_int(c);
				i@IO.out_string("\n");

				a <- a + 1;
				b <- b + a;
			} pool;
			b;
		}
	};

	loop_trial_2(a : Int, b : Int, c : Int) : Int {
        {
			while a <= 10 
	        loop {
    	        while a < 8
    	        loop {
    	            if a = c
    	            then
    	                if a <= c
    	                then {
    	                	i@IO.out_string("case1\n");
    	            	}
    	                else 
							i@IO.out_string("case2\n")
    	                fi
    	            else 
    	                if a = b
    	                then  i@IO.out_string("case3\n")
    	                else  i@IO.out_string("case4\n")
    	                fi
    	            fi;
    	        a <- a + 1;
    	        }
    	        pool;
    	        a <- a + 1;
    	    }
    	    pool;
        	5;
		}
	};

	loop_trial_3() : Int {
		{
			while 1 = 1 loop {
				i@IO.out_string("woohoo\n");
			} pool;
			1729;
		}
	};
};

