package cool;

import java.util.*;

public class Visitor
{
	private ScopeTable<String> scopeTable;
	private String filename;
	private String clName;
	IRPrinter display = new IRPrinter; //Added
	OpType string_type = new OpType(OpTypeId.INT8_PTR); //Added
	OpType int_type = new OpType(OpTypeId.INT32); //Added
	OpType bool_type = new OpType(OpTypeId.INT1); //Added
	OpType void_type = new OpType(OpTypeId.VOID); //Added

	//Constructor
	public Visitor(){
		scopeTable = new ScopeTable<String>();
    	filename = "";
    	clName = "";
	}

	//Program Visitor
	public void Visit(AST.program program){
		for(AST.class_ newClass: program.classes){
			Semantic.inheritance.InsertClass(newClass);
		}

		//Checks Validity of Classes
		Semantic.inheritance.CheckClass();
		if(Semantic.GetErrorFlagInProgram())
			return;
		//Checks for Cycles in Graph
		Semantic.inheritance.CheckCycle();
		if(Semantic.GetErrorFlagInProgram())
			return;
		Semantic.inheritance.CheckInheritedFeatures();

		//Visit all Nodes of AST
		for(AST.class_ newClass: program.classes)
			Visit(newClass);
	}
	
	//Class Visitor
	public void Visit(AST.class_ cl){
		//New scope for each Class
		scopeTable.enterScope();
		filename = cl.filename;
		clName = cl.name;

		List<AST.attr> cur_class_attributes = new ArrayList<AST.attr>();
	    List<AST.method> cur_class_methods = new ArrayList<AST.method>();

	    for (AST.feature f : cur_class.features) {
	    	if (f instanceof AST.attr) {
	        	AST.attr cur_attr = (AST.attr)f;
	        	cur_class_attributes.add(cur_attr);
	      	} 
	      	else if (f instanceof AST.method) {
	        	AST.method cur_method = (AST.method)f;
	        	cur_class_methods.add(cur_method);
	      	}
	    }

	    if (cur_class.parent == null) {
	      	classList.put(cur_class.name, new ClassNode(cur_class.name, cur_class.name, cur_class_attributes, cur_class_methods));
	    } 
	    else {
	     	classList.put(cur_class.name, new ClassNode(cur_class.name, cur_class.parent, cur_class_attributes, cur_class_methods));
	    }

	    for (AST.feature f : cur_class.features) {
	    	if (clName.equals("Main")) {
	    		display.define(out, int_type, "main", new ArrayList<Operand>());
		        display.allocaOperand(out, get_optype("Main", true, 0), new Operand(get_optype("Main", true, 1), "obj"));
		        List<Operand> op_list = new ArrayList<Operand>();
		        op_list.add(new Operand(get_optype("Main", true, 1), "obj"));
		        display.callOperand(out, new ArrayList<OpType>(), "Main_Cons_Main", true, op_list, new Operand(get_optype("Main", true, 1), "obj1"));
		        op_list.set(0, new Operand(get_optype("Main", true, 1), "obj1"));
		        display.callOperand(out, new ArrayList<OpType>(), "Main_main", true, op_list, new Operand(void_type, "null"));
		        display.retOperand(out, (Operand)new IntValue(0));
	    	}

	    	if (clName.equals("Int") || clName.equals("String") || clName.equals("Bool") || clName.equals("Object") || clName.equals("IO")) {
	    		if (clName.equals("String")){
	    			out.print("String funcs"); //To be added
	    		}
	    		else if (clName.equals("Object")){
	    			out.print("Abort");
	    			display.typeDefine(out, clName, new ArrayList<OpType>());
	    			//build_constructor
	    		}
	    		else if (cl.name.equals("IO")) {
		        	out.print("IO funcs"); //To be added
		          	display.typeDefine(out, cl.name, new ArrayList<OpType>());
		          	//build_constructor(out, cl.name, new Tracker());
		        }
		        continue;
	    	}

	    	// Taking the attributes of the class first and generating code for it
	      	List<OpType> attribute_types = new ArrayList<OpType>();
	      	for (AST.attr attribute : classList.get(clName).attributes) {
	        	attribute_types.add(get_optype(attribute.typeid, true, 1));
	        	if (attribute.typeid.equals("String") && attribute.value instanceof AST.string_const) { // Getting existing string constants
	          		breakdown(out, attribute.value);
	        	}
	      	}

	      	
	    }

	}

	//Attribute Visitor
	public void Visit(AST.attr at){
		Visit(at.value);
		if(!"No_type".equals(at.value.type) && Semantic.inheritance.isConforming(at.typeid,at.value.type)==false)
			Semantic.reportError(filename,at.lineNo,"Type '"+at.value.type+"' in Assign statement cannot conform to Type '"+at.typeid+"' of Attribute '"+at.name+"'");
		if("No_type".equals(at.value.type)){
			AST.expression e = BaseExprInit(at.typeid,at.lineNo);
			if(e!=null)
				at.value = e;
		}
		scopeTable.insert(at.name,at.typeid);
	}

	//Method Visitor
	public void Visit(AST.method md){
		//New scope for each Method
		scopeTable.enterScope();

		//Inserting Formal Parameters into the Scope Table
		for(AST.formal fl: md.formals){
			//Checking for multiple declarations of formal parameters
			if(scopeTable.lookUpLocal(fl.name)==null)
				scopeTable.insert(fl.name,fl.typeid);
			else
				Semantic.reportError(filename,fl.lineNo,"Multiple declarations of the same formal parameter '"+fl.name+"'");
		}

		//Visit Body of Method
		Visit(md.body);

		//Exit scope before leaving Method
		scopeTable.exitScope();
	}

	//Expression Visitor
	public void Visit(AST.expression exp){
		//No_Expr
		if(exp instanceof AST.no_expr){
			AST.no_expr expr = (AST.no_expr)exp;
			expr.type = "No_type";
		}
		
		//Bool
		else if(exp instanceof AST.bool_const){
			AST.bool_const expr = (AST.bool_const)exp;
			expr.type = "Bool";
		}
		
		//String
		else if(exp instanceof AST.string_const){
			AST.string_const expr = (AST.string_const)exp;
			expr.type = "String";
		}
		
		//Int
		else if(exp instanceof AST.int_const){
			AST.int_const expr = (AST.int_const)exp;
			expr.type = "Int";
		}
		
		//Boolean complement
		else if(exp instanceof AST.comp){
			AST.comp expr = (AST.comp)exp;
			Visit(expr.e1);
			
			if(!"Bool".equals(expr.e1.type)){
				Semantic.reportError(filename,expr.lineNo,"Expression for 'not' is not of Bool type");	
			}
			expr.type = "Bool";
		}
		
		//Equal to
		else if(exp instanceof AST.eq){
			AST.eq expr = (AST.eq)exp;
			Visit(expr.e1);
			Visit(expr.e2);

			if(!expr.e1.type.equals(expr.e2.type)){
				Semantic.reportError(filename,expr.lineNo,"Type Mismatch of Operands in '=' Expression : "+expr.e1.type+", "+expr.e2.type);
			}
			else{
				expr.type = "Bool";
			}
		}
		
		//Less than or Equal to
		else if(exp instanceof AST.leq){
			AST.leq expr = (AST.leq)exp;
			Visit(expr.e1);
			Visit(expr.e2);

			if(!expr.e1.type.equals("Int"))
				Semantic.reportError(filename,expr.e1.lineNo,"Left-hand Expression for '<=' is not of Int type");
			if(!expr.e2.type.equals("Int"))
				Semantic.reportError(filename,expr.e2.lineNo,"Right-hand Expression for '<=' is not of Int type");
			expr.type = "Bool";
		}
		
		//Strictly less than
		else if(exp instanceof AST.lt){
			AST.lt expr = (AST.lt)exp;
			Visit(expr.e1);
			Visit(expr.e2);

			if(!expr.e1.type.equals("Int"))
				Semantic.reportError(filename,expr.e1.lineNo,"Left-hand Expression for '<' is not of Int type");
			if(!expr.e2.type.equals("Int"))
				Semantic.reportError(filename,expr.e2.lineNo,"Right-hand Expression for '<' is not of Int type");
			expr.type = "Bool";
		}
		
		//Integer Complement
		else if(exp instanceof AST.neg){
			AST.neg expr = (AST.neg)exp;
			Visit(expr.e1);

			if(!expr.e1.type.equals("Int"))
				Semantic.reportError(filename,expr.lineNo,"Expression for 'not' is not of Int type");
			expr.type = "Int";
		}
		
		//Division
		else if(exp instanceof AST.divide) {
			AST.divide expr = (AST.divide)exp;
			Visit(expr.e1);
			Visit(expr.e2);

	        if(!expr.e1.type.equals("Int"))
				Semantic.reportError(filename,expr.e1.lineNo,"Dividend is not of Int type");
			if(!expr.e2.type.equals("Int"))
				Semantic.reportError(filename,expr.e2.lineNo,"Divisor is not of Int type");
	        expr.type = "Int";
		}
		
		//Multiplication
		else if(exp instanceof AST.mul) {
			AST.mul expr = (AST.mul)exp;
			Visit(expr.e1);
			Visit(expr.e2);

	        if(!expr.e1.type.equals("Int"))
				Semantic.reportError(filename,expr.e1.lineNo,"Left-hand Multiplicand is not of Int type");
			if(!expr.e2.type.equals("Int"))
				Semantic.reportError(filename,expr.e2.lineNo,"Right-hand Multiplicand is not of Int type");
	        expr.type = "Int";
		}
		
		//Subtraction
		else if(exp instanceof AST.sub) {
			AST.sub expr = (AST.sub)exp;
			Visit(expr.e1);
			Visit(expr.e2);

	        if(!expr.e1.type.equals("Int"))
				Semantic.reportError(filename,expr.e1.lineNo,"Left-hand Operand for '-' is not of Int type");
			if(!expr.e2.type.equals("Int"))
				Semantic.reportError(filename,expr.e2.lineNo,"Right-hand Operand for '-' is not of Int type");
	        expr.type = "Int";
		}
		
		//Addition
		else if(exp instanceof AST.plus) {
			AST.plus expr = (AST.plus)exp;
			Visit(expr.e1);
			Visit(expr.e2);

	        if(!expr.e1.type.equals("Int"))
				Semantic.reportError(filename,expr.e1.lineNo,"Left-hand Operand for '+' is not of Int type");
			if(!expr.e2.type.equals("Int"))
				Semantic.reportError(filename,expr.e2.lineNo,"Right-hand Operand for '+' is not of Int type");
	    	expr.type = "Int";
		}
		
		//Isvoid
		else if(exp instanceof AST.isvoid) {
			AST.isvoid expr = (AST.isvoid)exp;
			expr.type = "Bool";
		}

		//New
		else if(exp instanceof AST.new_){
			AST.new_ expr = (AST.new_)exp;
			if(Semantic.inheritance.GetClassIndex(expr.typeid)!=null)
				expr.type = expr.typeid;
			else{
				Semantic.reportError(filename,expr.lineNo,"Undefined type '" + expr.typeid + "' for 'new' Expression");
				expr.type = "Object";
			}
		}

		//Block
		else if(exp instanceof AST.block){
			AST.block bl = (AST.block)exp;
			for(AST.expression e: bl.l1)
				Visit(e);

			bl.type = bl.l1.get(bl.l1.size()-1).type;
		}
		
		//Loop
		else if(exp instanceof AST.loop){
			AST.loop lp = (AST.loop)exp;
			Visit(lp.predicate);

			if(!lp.predicate.type.equals("Bool"))
				Semantic.reportError(filename,lp.lineNo,"Loop condition not of Bool type");
			Visit(lp.body);
			lp.type = "Object";
		}

	  	//If Else
		else if(exp instanceof AST.cond){
			AST.cond expr = (AST.cond)exp;
			Visit(expr.predicate);
			Visit(expr.ifbody);
			Visit(expr.elsebody);

			if(!expr.predicate.type.equals("Bool"))
				Semantic.reportError(filename, expr.lineNo,"If condition not of Bool type");
			String type1 = expr.ifbody.type;
			String type2 = expr.elsebody.type;

			//Deciding type of Conditional Expression
			if(type1.equals(type2)){
				expr.type = type1;
			} 
			else if(type1.equals("Object")||type2.equals("Object")){
				expr.type = "Object";
			}
			else{
				expr.type = (Semantic.inheritance.GetLCA(type1,type2));
			}
		
		}
		
		//Assign
		else if(exp instanceof AST.assign){
			AST.assign expr = (AST.assign)exp;
			Visit(expr.e1);

			if("self".equals(expr.name))
				Semantic.reportError(filename,expr.lineNo,"Assignment to self not possible");
			else{
				String type = scopeTable.lookUpGlobal(expr.name);
				if(type == null)
					Semantic.reportError(filename,expr.lineNo,expr.name+" undefined");
				else if(!Semantic.inheritance.isConforming(type,expr.e1.type)){
					Semantic.reportError(filename,expr.lineNo,"Type '"+expr.e1.type+"' cannot conform to Type '"+type+"' for Assign operation");
				}
				
			}
			expr.type = expr.e1.type;
		}
		
		//Static Dispatch
		else if(exp instanceof AST.static_dispatch){
			AST.static_dispatch expr = (AST.static_dispatch)exp;
			Visit(expr.caller);
			
			if(expr.caller.type.equals("SELF_TYPE"))
				expr.caller.type = clName;
			for(AST.expression e : expr.actuals)
				Visit(e);

			if(Semantic.inheritance.GetClassIndex(expr.typeid) == null){
				Semantic.reportError(filename,expr.lineNo, "Undefined Class type '"+expr.typeid+"'");
				expr.typeid = "Object";
				expr.type = "Object";
			}
			else if(Semantic.inheritance.isConforming(expr.typeid,expr.caller.type)==false){
				Semantic.reportError(filename,expr.lineNo,"Caller of Type '"+expr.caller.type+"' cannot conform to Static Dispatch Type '"+expr.typeid+"'");
				expr.type = "Object";
			
			}
			
			ArrayList<AST.formal> lf = new ArrayList<AST.formal>();
			for(AST.expression e: expr.actuals)
				lf.add(new AST.formal("",e.type,0));
			AST.method m = new AST.method(expr.name,lf,"",(AST.expression)new AST.no_expr(0),0);
			String s = Semantic.inheritance.GetMangledName(expr.caller.type,m);

			if(Semantic.inheritance.CheckMangledName(s)==false){
				Semantic.reportError(filename,expr.lineNo,"Undefined Method '"+expr.name+"' in Class '"+expr.caller.type+"'");
				expr.type = "Object";
			}
			else{
				expr.type = Semantic.inheritance.GetClassMethods(expr.caller.type).get(expr.name).typeid;	
			}
		}

		//Dispatch
		else if(exp instanceof AST.dispatch){
			AST.dispatch expr = (AST.dispatch)exp;
			Visit(expr.caller);

			if(expr.caller.type.equals("SELF_TYPE"))
				expr.caller.type = clName;
			for(AST.expression e : expr.actuals)
				Visit(e);
			
			ArrayList<AST.formal> lf = new ArrayList<AST.formal>();
			for(AST.expression e: expr.actuals)
				lf.add(new AST.formal("",e.type,0));
			AST.method m = new AST.method(expr.name,lf,"",(AST.expression)new AST.no_expr(0),0);
			String s = Semantic.inheritance.GetMangledName(expr.caller.type,m);

			if(Semantic.inheritance.CheckMangledName(s)==false){
				Semantic.reportError(filename,expr.lineNo,"Undefined Method '"+expr.name+"' in Class '"+expr.caller.type+"'");
				expr.type = "Object";
			}
			else{
				expr.type = Semantic.inheritance.GetClassMethods(expr.caller.type).get(expr.name).typeid;	
			}
		}
    
    	//Let
		else if(exp instanceof AST.let){
			AST.let expr = (AST.let)exp;
			scopeTable.enterScope();

			if(Semantic.inheritance.GetClassIndex(expr.typeid) == null){
				Semantic.reportError(filename,expr.lineNo,"Undefined type " + expr.typeid);
				expr.type = "Object";
			}
      
      		Visit(expr.value);
			if(!(expr.value instanceof AST.no_expr)){
				Visit(expr.value);
				if(!Semantic.inheritance.isConforming(expr.typeid,expr.value.type)){
					Semantic.reportError(filename,expr.lineNo,"Type '"+expr.value.type+"' cannot conform to the declared Type '"+expr.typeid+"'");
				}
			}
				
			Visit(expr.body);
			expr.type = expr.body.type;
			scopeTable.exitScope();
		}
  		
  		/*else if (exp instanceof AST.typcase){
			AST.typcase expr = (AST.typcase) exp;
			Visit(expr.predicate);
			Visit(expr.branches.get(0));
			expr.type = expr.branches.get(0).value.type;
			for(int i=1; i<expr.branches.size(); i++) {
            	Visit(expr.branches.get(i));
           	 	String type1 = expr.type;
           	 	String type2 = expr.branches.get(i).value.type;
           	 	if(type1.equals(type2))
           	 		expr.type = type1;
           	 	else if (type1.equals("Bool")||type1.equals("Int")||type1.equals("String")||type2.equals("Bool")||type2.equals("Int")||type2.equals("String"))
           	 		expr.type = "Object";
           	 	else{
           	 		expr.type = Semantic.inheritance.GetLCA(type1,type2);
           	 	}
        	}
		}*/
		
		else if(exp instanceof AST.object)
		{
			AST.object expr = (AST.object)exp;
			String t = scopeTable.lookUpGlobal(expr.name);
			if(t == null)
			{
				expr.type ="Object";
				Semantic.reportError(filename,expr.lineNo,"Attribute '" + expr.name + "' is not defined");
			}
			else
				expr.type = t;
			
		}

	}

	//Branch
	public void Visit(AST.branch brh){
		scopeTable.enterScope();
		if("self".equals(brh.name))
			Semantic.reportError(filename,brh.lineNo,"Bounding self in case not possible");
		else{
			if(Semantic.inheritance.GetClassIndex(brh.type) == null){
				Semantic.reportError(filename,brh.lineNo,"Undefined type " + brh.type);
				brh.type = "Object";
			}
		}	
		Visit(brh.value);
		scopeTable.exitScope();		
	}

	//Initialize Base Class Expressions
	public AST.expression BaseExprInit(String s, int l) {
		if("Int".equals(s))
			return new AST.int_const(0,l);
		if("Bool".equals(s))
			return new AST.bool_const(false,l);
		if("String".equals(s))
			return new AST.string_const("",l);
		return null;
	}

	public void breakdown(PrintWriter out, AST.expression expr) {
	    if (expr instanceof AST.string_const) {
		  	String cap_string = ((AST.string_const)expr).value;
		  	string_table.put(cap_string, string_counter);
		  	string_counter++;
		  	out.print("@.str." + string_table.get(cap_string) + " = private unnamed_addr constant [" + String.valueOf(cap_string.length() + 1) + " x i8] c\"");
		  	print_util.escapedString(out, cap_string);
		  	out.println("\\00\"");
	    } 
	    else if (expr instanceof AST.eq) {
	      	breakdown(out, ((AST.eq)expr).e1);
	      	breakdown(out, ((AST.eq)expr).e2);
	    } 
	    else if (expr instanceof AST.assign) {
	      	breakdown(out, ((AST.assign)expr).e1);
	    } 
	    else if (expr instanceof AST.block) {
	      	for (AST.expression e : ((AST.block)expr).l1) {
	        	breakdown(out, e);
	      	}
	    } 
	    else if (expr instanceof AST.loop) {
	      	breakdown(out, ((AST.loop)expr).predicate);
	      	breakdown(out, ((AST.loop)expr).body);
	    } 
	    else if (expr instanceof AST.cond) {
	      	breakdown(out, ((AST.cond)expr).predicate);
	      	breakdown(out, ((AST.cond)expr).ifbody);
	      	breakdown(out, ((AST.cond)expr).elsebody);
	    } 
	    else if (expr instanceof AST.static_dispatch) {
	      	breakdown(out, ((AST.static_dispatch)expr).caller);
	      	for (AST.expression e : ((AST.static_dispatch)expr).actuals) {
	       		breakdown(out, e);
	      	}
	    }
	    return ;
  	}
}
