package cool;

import java.util.*;

public class Visitor
{
	private ScopeTable<String> scopeTable;
	private String filename;
	private String clName;

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

		//Insert all declared Class attributes into Scope Table
		for(Map.Entry<String,AST.attr> entry: Semantic.inheritance.GetClassAttrs(cl.name).entrySet()){
			Visit(entry.getValue());
			scopeTable.insert(entry.getKey(),entry.getValue().typeid);
		}

		//Visit all the Class methods
		for(Map.Entry<String,AST.method> entry: Semantic.inheritance.GetClassMethods(cl.name).entrySet())
			Visit(entry.getValue());

		//Exit scope before leaving Class
		scopeTable.exitScope();
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
	public AST.expression BaseExprInit(String s, int l){
		if("Int".equals(s))
			return new AST.int_const(0,l);
		if("Bool".equals(s))
			return new AST.bool_const(false,l);
		if("String".equals(s))
			return new AST.string_const("",l);
		return null;
	}
}
