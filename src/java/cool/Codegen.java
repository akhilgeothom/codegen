package cool;

import java.io.PrintWriter;
import java.util.*;

public class Codegen{
    private String globOut;

    private Integer labCnt;
    private Integer varCnt;
    private Integer globCnt;

    private HashMap<String,String> clNames = new HashMap<String,String>();

    private ArrayList<String> baseFns = new ArrayList<String>();

    private HashMap<String,ArrayList<AST.attr>> classAttrs = new HashMap<String,ArrayList<AST.attr>>();

    private String className;

    public Codegen(AST.program program, PrintWriter out){
        // out.println("; I am a comment in LLVM-IR. Feel free to remove me.");

        out.println("; ModuleID = \'"+program.classes.get(0).filename+"\'");
        out.println("source_filename = \""+program.classes.get(0).filename+"\"");
        out.println("target datalayout = \"e-m:e-i64:64-f80:128-n8:16:32:64-S128\"");
        out.println("target triple = \"x86_64-pc-linux-gnu\"\n");

        // progOut = "";
        //Write Code generator code here
        // className = "";
        // indent = "  ";

        globOut = "";
        globOut += "; global\n";
        globOut += "@dStr = private constant [2 x i8] c\"%d\"\n";
        globOut += "@sStr = private constant [2 x i8] c\"%s\"\n";
        globOut += "@nullStr = private unnamed_addr constant [1 x i8] zeroinitializer\n";

        labCnt = 0;
        varCnt = 0;
        globCnt = 0;
        
        clNames.put("Int","i32");
        clNames.put("String","i8*");
        clNames.put("Bool","i8");




        builtInFns(out);
        out.println("\n");

        for(AST.class_ cl: program.classes) {
            out.println("%class."+cl.name+" = type { i8,");
            
            clNames.put(cl.name,"%class."+cl.name);
            ArrayList<AST.attr> attrs = new ArrayList<AST.attr>();

            // for(Map.Entry<String,AST.attr> entry: Semantic.inheritance.GetClassAttrs(cl.name).entrySet()) {
            //     if(entry.getValue().name.equals("self"))
            //         continue;

            //     attrs.add(entry.getValue());
            //     String tp = clNames.get(entry.getValue().typeid);
            //     if(tp == null)
            //         tp = "%class."+entry.getValue().typeid;
            //     out.println(" "+tp+",");
            // }
            // Codegen.progOut = Codegen.progOut.substring(0,Codegen.progOut.length()-1);
            out.println(" }\n");
            
            out.println("%class." + cl.name + " = type { i32, i8*, %class." + cl.name + ".Base }");

            classAttrs.put(cl.name,attrs);
        }

        out.println("; Class Definitions");
        for(AST.class_ cl: program.classes)
        {
            String clTyp = "%struct." + cl.name;
            out.println("define void @init_" + cl.name + "(" + clTyp + "* %a1) {");
            out.println("%v0 = getelementptr " + clTyp + ", " + clTyp + "* %a1, i32 0, i32 0");
            out.println("store i8 1, i8* %v0");

            Integer idx = 1;
            ArrayList<AST.attr> attrs = classAttrs.get(cl.name);
            for(; idx<=attrs.size(); idx++)
            {
                String atTyp = attrs.get(idx-1).typeid;
                if(atTyp.equals("SELF_TYPE"))
                    continue;

                out.println("%v" + idx + " = getelementptr " + clTyp + ", " + clTyp + "* %a1, i32 0, i32 " + idx);
                switch(atTyp)
                {
                    case "Bool" :
                        out.println("store i8 0, i8* %v" + idx);
                        break;
                    case "Int" :
                        out.println("store i32 0, i32* %v" + idx);
                        break;
                    case "String" :
                        out.println("%str" + idx + " = getelementptr inbounds [1 x i8], [1 x i8]* @nullStr, i32 0, i32 0");
                        out.println("store i8* %str" + idx + ", i8** %v" + idx);
                        break;
                    default :
                        out.println("%set" + idx + " = getelementptr %struct." + atTyp + ", %struct." + atTyp + "* %v" + idx + ", i32 0, i32 0");
                        out.println("store i8 0, i8* %set" + idx);
                }
            }

            out.println("ret void\n}\n");
        }
        out.println("\n");
        out.println("; Class Methods\n");
        
        
        for(AST.class_ cl: program.classes)
        {
            if(!cl.name.equals("Main"))
            {
                className = cl.name;
                Visit(cl, out);  //class
            }
            else{
                className = "Main";
                for(AST.feature e :cl.features){
                    if(e.getClass() == AST.method.class){
                        // if(e.getValue().name.equals("main"))
                        //     continue;

                        // if(entry.getKey().equals("type_name"))
                        {
                            out.println("define i8* @"+getMangledName(className,(AST.method)e)+"() {");
                            String arStr = "[4 x i8]";
                            out.println("@g" + ++globCnt + " = private unnamed_addr constant " + arStr + " c\"Main\""); 

                            out.println("%v1 = getelementptr inbounds " + arStr + ", " + arStr + "* @g" + globCnt + ", i32 0, i32 0");
                            out.println("ret i8* %v1\n}");
                        }
                        // else if(!baseFns.contains(entry.getKey()) && !entry.getKey().equals("copy"))  //dont understand
                        // {
                        //     out.println("define "+clNames.get(entry.getValue().typeid)+" @"+getMangledName(className,entry.getValue())+"(");
                        //     Visit(entry.getValue());
                        // }
                    }
                }
                out.println("\n; Main ");
                // AST.method md = mainClassMethods.get("main");
                // out.println("define "+clNames.get(((AST.method)md).typeid)+" @main (");
                // Visit(md);  //method
            }
        }
        out.println("\n");

        //Insert Main Methods
        // HashMap<String,AST.method> mainClassMethods = Semantic.inheritance.GetClassMethods("Main");
        // className = "Main";
        // for(Map.Entry<String,AST.method> entry: mainClassMethods.entrySet()){
        //     if(entry.getValue().name.equals("main"))
        //         continue;

        //     if(entry.getKey().equals("type_name"))
        //     {
        //         out.println("define i8* @"+getMangledName(className,entry.getValue())+"() {\n");
        //         String arStr = "[4 x i8]";
        //         out.println("@g" + ++globCnt + " = private unnamed_addr constant " + arStr + " c\"Main\"\n"); 

        //         out.println("%v1 = getelementptr inbounds " + arStr + ", " + arStr + "* @g" + globCnt + ", i32 0, i32 0\n");
        //         out.println("ret i8* %v1\n}\n");
        //     }
        //     else if(!baseFns.contains(entry.getKey()) && !entry.getKey().equals("copy"))  //dont understand
        //     {
        //         out.println("define "+clNames.get(entry.getValue().typeid)+" @"+getMangledName(className,entry.getValue())+"(");
        //         Visit(entry.getValue());
        //     }
        // }

        //main() Function
        // out.println("\n; Main Function\n");
        // AST.method md = mainClassMethods.get("main");
        // out.println("define "+clNames.get(md.typeid)+" @main (");
        // Visit(md);  //method

        //Concat Global variables to Output
        // Codegen.progOut = globOut + "\n" + Codegen.progOut;
        out.println(globOut);
    }

    public void Visit(AST.class_ cl, PrintWriter out){
        // for(Map.Entry<String,AST.method> entry: Semantic.inheritance.GetClassMethods(cl.name).entrySet())
        for(AST.feature entry: cl.features)
        {
            if(((AST.method)entry).name.equals("type_name"))
            {
                out.println("define i8* @"+getMangledName(className,(AST.method)entry)+"() {");
                String arStr = "[" + className.length() + " x i8]";
                out.println("@g" + ++globCnt + " = private unnamed_addr constant " + arStr + " c\"" + className + "\""); 

                out.println("%v1 = getelementptr inbounds " + arStr + ", " + arStr + "* @g" + globCnt + ", i32 0, i32 0");
                out.println("ret i8* %v1\n}\n");
            }
            else if(!baseFns.contains(((AST.method)entry).name) && !((AST.method)entry).name.equals("copy"))
            {
                out.println("define "+clNames.get(((AST.method)entry).typeid)+" @"+getMangledName(className,(AST.method)entry)+"(");
                Visit((AST.method)entry, out);  //methods
            }
        }
    }
    public void Visit(AST.method md, PrintWriter out){
        // TODO: reuse scopeTable from Semantic, but must take care of scope
        ScopeTable<String> varNames = new ScopeTable<String>();
        Integer idx = 1;
        for(AST.attr at: classAttrs.get(className))
            varNames.insert(at.name,Integer.toString(idx++));

        if(!className.equals("Main") || !md.name.equals("main"))
        {
            idx = 2;
            out.println("%struct." + className + "* %a1, ");
            varNames.insert("self","%a1");
            for(AST.formal fl: md.formals)
            {
                String varId = "%a"+Integer.toString(idx++);
                varNames.insert(fl.name,varId);
                out.println(clNames.get(fl.typeid)+" "+varId+", ");
            }
        }
        out.println(") {\nentry:");

        varCnt = 0;
        labCnt = 0;
        if(md.name.equals("main"))
        {
            out.println("%a1 = alloca %struct.Main");
            varNames.insert("self","%a1");
            out.println("call void @init_Main(%struct.Main* %a1)");
        }

        Visit(md.body,varNames,out);  //expression

        out.println("ret " + clNames.get(md.typeid) + " " + md.body.type);
        out.println("}\n");
    }
    public void Visit(AST.expression expr, ScopeTable<String> varNames, PrintWriter out){

        //Bool
        if(expr instanceof AST.bool_const)
        {
            AST.bool_const b = (AST.bool_const)expr;
            int value = 0;
            if(b.value)
                value = 1;
            b.type = Integer.toString(value);
        }

        //Int
        else if(expr instanceof AST.int_const)
        {
            AST.int_const ic = (AST.int_const)expr;
            ic.type = Integer.toString(ic.value);
        }

        //String
        else if(expr instanceof AST.string_const)
        {
            AST.string_const str = (AST.string_const)expr;

            String escStr = "";
            //Escape Characters in 'str'
            for(char c : str.value.toCharArray())
            {
                switch(c)
                {
                    case '\\' :
                        escStr += "\\5C";
                        break;
                    case '\"' :
                        escStr += "\\22";
                        break;
                    case '\n' :
                        escStr += "\\0A";
                        break;
                    case '\t' :
                        escStr += "\\09";
                        break;
                    case '\b' :
                        escStr += "\\08";
                        break;
                    case '\f' :
                        escStr += "\\0C";
                        break;
                    default :
                        escStr += c;
                }
            }

            String arStr = "[" + str.value.length() + " x i8]";
            globOut += "@g" + ++globCnt + " = private unnamed_addr constant " + arStr + " c\"" + escStr + "\"\n"; 
            out.println("%v" + ++varCnt + " = getelementptr inbounds " + arStr + ", " + arStr + "* @g" + globCnt + ", i32 0, i32 0\n");
            str.type = "%v" + varCnt;
        }

        //Object
        else if(expr instanceof AST.object)
        {
            AST.object obj = (AST.object)expr;
            obj.type = varNames.lookUpGlobal(obj.name);
        }

        //Assign
        else if(expr instanceof AST.assign)
        {
            AST.assign asgn = (AST.assign)expr;
            Visit(asgn.e1,varNames, out);   //expression
            String vname = varNames.lookUpGlobal(asgn.name);

            //If Assignment expression is a Class Attribute
            if(isFstDgt(vname) == true)
            {
                String clTyp = "%struct." + className;
                varCnt++;
                out.println("%v" + varCnt + " = getelementptr " + clTyp + ", " + clTyp + "* %a1, i32 0, i32 " + vname);
                out.println("store " + clNames.get(asgn.type) + " " + asgn.e1.type + ", " + clNames.get(asgn.type) + "* %v" + varCnt);
            }
            else
                out.println("store " + clNames.get(asgn.type) + " " + asgn.e1.type + ", " + clNames.get(asgn.type) + "* " + vname);
        }

        //New
        else if(expr instanceof AST.new_)
        {
            AST.new_ nw = (AST.new_)expr;
            varCnt++;
            String vname1 = "%v" + Integer.toString(varCnt);
            varCnt++;
            String vname2 = "%v" + Integer.toString(varCnt);
            
            out.println(vname1 + " = alloca %struct." + nw.typeid);
            out.println("call void @init_" + nw.typeid + "(%struct." + nw.typeid + "* " + vname1 );
            
            out.println(vname2 + " = load %struct." + nw.typeid + ", %struct." + nw.typeid + "* " + vname1);
            nw.type = vname2; 

        }

        //IsVoid
        else if(expr instanceof AST.isvoid)
        {
            AST.isvoid iv = (AST.isvoid)expr;
            Visit(iv.e1, varNames, out);  //expression
            AST.attr at = classAttrs.get(className).get(Integer.valueOf(iv.e1.type)-1);
            String type = at.typeid;
            
            if(type.equals("Int") || type.equals("Bool") || type.equals("String"))
                iv.type = "0";
            else
            {
                varCnt++;
                String vname = "%v" + Integer.toString(varCnt); 
                out.println(vname + " = getelementptr %struct." + className + ", %struct." + className + "* %a1, i32 0, i32 " + iv.e1.type );
                varCnt++;
                String vname1 = "%v" + Integer.toString(varCnt); 
                out.println(vname1 + " = getelementptr %struct." + type + ", %struct." + type + "* " + vname + ", i32 0, i32 0");

                varCnt++;
                String vname2 = "%v" + Integer.toString(varCnt);
                out.println(vname2 + " = load i8, i8* " + vname1 + "\n");

                varCnt++;
                String vname3 = "%v" + Integer.toString(varCnt);
                out.println(vname3 + " = trunc i8 " + vname2 + " to i1 \n");

                iv.type = vname3;
            }
        }

        //Add
        else if(expr instanceof AST.plus)
        {
            AST.plus pl = (AST.plus)expr;
            Visit(pl.e1,varNames, out);  //expression
            Visit(pl.e2,varNames, out);  //expression

            if(isFstDgt(pl.e1.type) && isFstDgt(pl.e2.type))
            {
                pl.type = Integer.toString(Integer.valueOf(pl.e1.type) + Integer.valueOf(pl.e2.type));
                return;
            }
            
            varCnt++;
            String vname = "%v" + Integer.toString(varCnt);
            out.println(vname + " = add " +  clNames.get(expr.type));
            out.println(" " + pl.e1.type + ", " + pl.e2.type);
            pl.type = vname;
        }

        //Subtract
        else if(expr instanceof AST.sub)
        {
            AST.sub s = (AST.sub)expr;
            Visit(s.e1,varNames, out);  //expression
            Visit(s.e2,varNames, out);  //expression

            if(isFstDgt(s.e1.type) && isFstDgt(s.e2.type))
            {
                s.type = Integer.toString(Integer.valueOf(s.e1.type) - Integer.valueOf(s.e2.type));
                return;
            }
            
            varCnt++;
            s.type = "%v" + Integer.toString(varCnt);
            out.println(s.type + " = sub " +  clNames.get(expr.type) + " " + s.e1.type + ", " + s.e2.type);
        }

        //Multiply
        else if(expr instanceof AST.mul)
        {
            AST.mul m = (AST.mul)expr;
            Visit(m.e1,varNames, out);  //expression
            Visit(m.e2,varNames, out);  //expression

            if(isFstDgt(m.e1.type) && isFstDgt(m.e2.type))
            {
                m.type = Integer.toString(Integer.valueOf(m.e1.type) * Integer.valueOf(m.e2.type));
                return;
            }
            
            varCnt++;
            m.type = "%v" + Integer.toString(varCnt);
            out.println(m.type + " = mul " +  clNames.get(expr.type) + " " + m.e1.type + ", " + m.e2.type + "\n");
        }

        //Divide
        else if(expr instanceof AST.divide)
        {
            AST.divide div = (AST.divide)expr;
            Visit(div.e1,varNames, out);  //expression
            Visit(div.e2,varNames, out);  //expression
            
            //handling division by zero
            String vname = "%v" + Integer.toString(varCnt);
            varCnt++;
            labCnt++;
            String abortLabel = "abort" + Integer.toString(labCnt);
            String contLabel = "continue" + Integer.toString(labCnt);

            out.println(vname + " = icmp eq i32 " + div.e2.type + ", 0");
            out.println("br i1 " + vname + ", label %" + abortLabel + ", label %" + contLabel);
            out.println( abortLabel + ":");
            out.println("call void @abort()");
            out.println(contLabel + ":");


            if(isFstDgt(div.e1.type) && isFstDgt(div.e2.type) && Integer.valueOf(div.e2.type) != 0)
            {
                div.type = Integer.toString(Integer.valueOf(div.e1.type) / Integer.valueOf(div.e2.type));
                return;
            }
            
            varCnt++;
            div.type = "%v" + Integer.toString(varCnt);
            out.println(div.type + " = sdiv " +  expr.type + " " + div.e1.type + ", " + div.e2.type);
        }

        //Block
        else if(expr instanceof AST.block)
        {
            AST.block bk = (AST.block)expr;

            int idx = 0;
            for(; idx<bk.l1.size()-1; idx++)
                Visit(bk.l1.get(idx),varNames,out);  //expression
            Visit(bk.l1.get(idx),varNames,out);  //expression

            bk.type = bk.l1.get(idx).type;
        }

        //Less Than
        else if(expr instanceof AST.lt)
        {
            AST.lt l = (AST.lt)expr;
            varCnt++;
            String vname = "%v" + Integer.toString(varCnt);
            Visit(l.e1,varNames, out);  //expression
            Visit(l.e2,varNames, out);  //expression

            if(isBool(l.e1.type) >= 0 && isBool(l.e2.type) >= 0)
            {
                Integer value = 0;
                if(Integer.valueOf(l.e1.type) < Integer.valueOf(l.e2.type))
                    value = 1;
                l.type = Integer.toString(value);
            }
            else
            {
                out.println(vname + " = icmp " + "slt " + "i32 " + l.e1.type +","+ l.e2.type);
                l.type = vname;
            }
        }

        //Less Than or Equal To
        else if(expr instanceof AST.leq)
        {
            AST.leq l = (AST.leq)expr;
            varCnt++;
            String vname = "%v" + Integer.toString(varCnt);
            Visit(l.e1,varNames, out);  //expression
            Visit(l.e2,varNames, out);  //expression
            if(isBool(l.e1.type) >= 0 && isBool(l.e2.type) >= 0)
            {
                Integer value = 0;
                if(Integer.valueOf(l.e1.type) <= Integer.valueOf(l.e2.type))
                    value = 1;
                l.type = Integer.toString(value);
            }
            else
            {
                out.println(vname + " = icmp " + "sle " + "i32 " + l.e1.type +","+ l.e2.type);
                l.type = vname;
            }
        }

        //Equal To
        else if(expr instanceof AST.eq)
        {
            AST.eq l = (AST.eq)expr;
            varCnt++;
            String vname = "%v" + Integer.toString(varCnt);
            Visit(l.e1,varNames, out);  //expression
            Visit(l.e2,varNames, out);  //expression
            if(isBool(l.e1.type) >= 0 && isBool(l.e2.type) >= 0)
            {
                Integer value = 0;
                if(Integer.valueOf(l.e1.type) == Integer.valueOf(l.e2.type))
                    value = 1;
                l.type = Integer.toString(value);
            }
            else
            {
                out.println(vname + " = icmp " + "eq " + "i32 " + l.e1.type +","+ l.e2.type);
                l.type = vname;
            }
        }

        //If Else
        else if(expr instanceof AST.cond)
        {
            AST.cond cd = (AST.cond)expr;
            Visit(cd.predicate, varNames, out);  //expression

            //if predicate is always false, execute else body
            if(isBool(cd.predicate.type) == 0)
            {
                Visit(cd.elsebody, varNames, out);  //expression
                cd.type = cd.elsebody.type;
            }
            //if predicate is always ture, execute if body
            else if(isBool(cd.predicate.type) == 1)
            {
                Visit(cd.ifbody, varNames, out);  //expression
                cd.type = cd.ifbody.type;
            }
            else
            {
                labCnt++;
                String ifLabel = "if.then" + Integer.toString(labCnt);
                String elseLabel = "if.else" + Integer.toString(labCnt);
                String endLabel = "if.end" + Integer.toString(labCnt);

                out.println("br i1 " + cd.predicate.type + ", " + "label %" + ifLabel + ", " + "label %" + elseLabel + "\n"); 

                out.println(ifLabel + ":");
                Visit(cd.ifbody, varNames, out);  //expression
                out.println("br label %" + endLabel + "\n");
                
                out.println( elseLabel + ":\n");
                Visit(cd.elsebody, varNames, out);  //expression
                out.println("br label %" + endLabel + "\n");

                out.println(endLabel + ":");
                varCnt++;
                String vname = "%v" + Integer.toString(varCnt);
                out.println(vname + " = phi " + clNames.get(cd.type) + " [ " + cd.ifbody.type + ", %" + ifLabel + " ], [ " + cd.elsebody.type + ", %" + elseLabel + " ]");
                cd.type = vname;
            }
        }

        //While Loop
        else if(expr instanceof AST.loop)
        {
            AST.loop lp = (AST.loop)expr;
            Visit(lp.predicate, varNames,out);  //expression

            //if predicate is always false
            if(isBool(lp.predicate.type) == 0)
            {
                //do nothing
            }
            //if predicate is always true
            else if(isBool(lp.predicate.type) == 1)
            {
                //infinite loop
                labCnt++;
                String body = "while.body" + Integer.toString(labCnt);;
                String end = "return" + Integer.toString(labCnt);

                out.println("br label %" + body + "\n");
                out.println(body + ":");
                Visit(lp.body, varNames,out);  //expression
                out.println("br label %" + body + "\n");
                out.println(end + ":");
            }
            else
            {
                labCnt++;
                String cond = "while.cond" + Integer.toString(labCnt);
                String body = "while.body" + Integer.toString(labCnt);
                String end = "while.end" + Integer.toString(labCnt);

                out.println("br label %" + cond + "\n");
                out.println( cond + ":");
                Visit(lp.predicate, varNames,out);  //expression
                out.println("br i1 " + lp.predicate.type + ", label %" + body + ", label %" + end + "\n");
                
                out.println(body + ":");
                Visit(lp.body, varNames,out);  //expression
                out.println("br label %" + cond + "\n");

                out.println( end + ":");
            }
        }

        //Static Dispatch
        else if(expr instanceof AST.static_dispatch)
        {
            AST.static_dispatch sd = (AST.static_dispatch)expr;
            Visit(sd.caller, varNames,out);  //expression

            //System.out.println(sd.caller.type+sd.typeid+sd.name);
            //String vname = "%v" + ++varCnt;
            //String clTyp = clNames.get(sd.typeid);
            //Codegen.progOut += indent + vname + " = alloca " + clTyp + "\n";
            //AST.method md = Semantic.inheritance.GetClassMethods(sd.type).get(sd.name);
            //String vname1 = "%v" + ++varCnt;
            //Codegen.progOut += "%v" + vname1 + " = call " + clNames.get(md.typeid) + "@" + Semantic.inheritance.GetMangledName(sd.type,md) + "(";

            //Codegen.progOut += clTyp + "* " + vname + ", ";
            //for(AST.expression exp : sd.actuals)
            //{
            //    Codegen.progOut += clNames.get(exp.type);
            //    Visit(exp, varNames);
            //    Codegen.progOut += exp.type;
            //}
            //Codegen.progOut = Codegen.progOut.substring(0,Codegen.progOut.length()-2) + ")";
        }
        else{
            // out.println("really!!!");
        }
        
    }
    private Boolean isFstDgt(String s)
    {
        if(s.charAt(0)>='0' && s.charAt(0)<='9')
            return true;
        else if(s.charAt(0)=='-' && s.charAt(1)>='0' && s.charAt(1)<='9')
            return true;
        return false;
    }

    //Check if First Char of String is Boolean
    private int isBool(String s)
    {
        if(s.charAt(0) == '0')
            return 0;
        else if(s.charAt(0) == '1')
            return 1;
        else
            return -1;
    }

    public String getMangledName(String className,AST.method md) {
        String temp = "";
        temp += "_CN";
        String funcName = md.name;
        temp += (Integer.toString(className.length()));
        temp += "_";
        temp += (className);
        temp += ("_FN");
        temp += (Integer.toString(funcName.length()));
        temp += (funcName);
        temp += ("_AL");
        temp += (Integer.toString(md.formals.size()));
        if(md.formals.size() == 0)
            temp += ("_NP_");
        else
        {
            for(int j = 0; j < md.formals.size(); j++)
            {
                temp += (Integer.toString(j));
                temp += ("N");
                temp += (Integer.toString(md.formals.get(j).typeid.length()));
                temp.concat(md.formals.get(j).typeid);
            }
        }

        return temp;
    }

    void builtInFns(PrintWriter out) {
        out.println("; global ");

        out.println("@dStr = private constant [2 x i8] c\"%d\"");
        out.println("@sStr = private constant [2 x i8] c\"%s\"");
        out.println("@nullStr = private unnamed_addr constant [1 x i8] zeroinitializer");

        out.println("; C function");
        out.println("declare void @exit(i32)");
        out.println("; C function");
        out.println("declare i8* @malloc(i64)");
        out.println("; C function");
        out.println("declare i32 @printf(i8* , ...)");
        out.println("; C function");
        out.println("declare i32 @scanf(i8* , ..).)");
        out.println("; C function");
        out.println("declare i32 @strlen(i8*)");
        out.println("; C function");
        out.println("declare i8* @strcat(i8*, i8*)");
        out.println("; C function");
        out.println("declare i8* @strcpy(i8*, i8*)\n");

        out.println("; Cool Function");
        baseFns.add("abort");
        out.println("define void @abort(i32 %a1) {");
        out.println("call void @exit(i32 %a1)");
        out.println("ret void\n}\n");

        out.println("; Cool Function");
        baseFns.add("out_string");
        out.println("define void @out_string(i8* %a1) {");
        out.println("call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @sStr, i32 0, i32 0),i8* %a1)");
        out.println("ret void\n}\n");

        out.println("; Cool Function");
        baseFns.add("in_string");
        out.println("define void @in_string(i8* %a1) {");
        out.println("call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @sStr, i32 0, i32 0),i8* %a1)");
        out.println("ret void\n}\n");

        out.println("; Cool Function");
        baseFns.add("out_int");
        out.println("define void @out_int(i32 %a1) {");
        out.println("call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @dStr, i32 0, i32 0),i32 %a1)");
        out.println("ret void\n}\n");

        out.println("; Cool Function");
        baseFns.add("in_int");
        out.println("define i32 @in_int() {");
        out.println("%v1 = alloca i32");
        out.println("call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @sStr, i32 0, i32 0),i32* %v1)");
        out.println("%v2 = load i32, i32* %v1");
        out.println("ret i32 %v2\n}\n\n");

        out.println("; Cool Function");
        baseFns.add("length");
        out.println("define i32 @length(i8* %a1) {");
        out.println("%v1 = call i32 @strlen(i8* %a1)");
        out.println("ret i32 %v1\n}\n");

        out.println("; Cool Function");
        baseFns.add("concat");
        out.println("define i8* @concat(i8* %a1, i8* %a2){");
        out.println("call i8* @strcat(i8* %a1, i8* %a2)");
        out.println("ret i8* %a1\n}\n");

        out.println("; Cool Function");
        baseFns.add("substr");
        out.println("define i8* @substr(i8* %s, i32 %i, i32 %l){");
        out.println("%z = zext i32 %l to i64");
        out.println("%str = call noalias i8* @malloc(i64 %z)");
        out.println("%ptr = getelementptr inbounds i8, i8* %s, i32 %i");
        out.println("%foo = call i8* @strncpy(i8* %str, i8* %ptr, i64 %z)");
        out.println("ret i8* %str\n}\n");
      
        out.println("\n");
    }
}
