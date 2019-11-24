-- Description in Readme.md
class Main{
	i : Int <- 1;
	main() : Int{
		{
			i <- i / 0;
			1; 
		}
	};
};

