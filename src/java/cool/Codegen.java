package cool;

import java.io.PrintWriter;
import java.util.*;

public class Codegen{
    private String globOut;

    // private Integer varCnt;
    // private Integer labCnt;
    private Integer globCnt;

    private HashMap<String,String> clNames = new HashMap<String,String>();

    private ArrayList<String> baseFns = new ArrayList<String>();

    private HashMap<String,ArrayList<AST.attr>> classAttrs = new HashMap<String,ArrayList<AST.attr>>();

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
        globOut += "; Global Constants\n";
        globOut += "@dStr = private constant [2 x i8] c\"%d\"\n";
        globOut += "@sStr = private constant [2 x i8] c\"%s\"\n";
        globOut += "@nullStr = private unnamed_addr constant [1 x i8] zeroinitializer\n";

        // varCnt = 0;
        // labCnt = 0;
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
            
            out.println("%class." + cl.name + " = type { i32, i8*, %class." + cl.name + ".Base }\n");

            classAttrs.put(cl.name,attrs);
        }

        out.println("; Class Initializtion Methods\n");
        for(AST.class_ cl: program.classes)
        {
            String clTyp = "%struct." + cl.name;
            out.println("define void @init_" + cl.name + "(" + clTyp + "* %a1) {\n");
            out.println("%v0 = getelementptr " + clTyp + ", " + clTyp + "* %a1, i32 0, i32 0\n");
            out.println("store i8 1, i8* %v0\n");

            Integer idx = 1;
            ArrayList<AST.attr> attrs = classAttrs.get(cl.name);
            for(; idx<=attrs.size(); idx++)
            {
                String atTyp = attrs.get(idx-1).typeid;
                if(atTyp.equals("SELF_TYPE"))
                    continue;

                out.println("%v" + idx + " = getelementptr " + clTyp + ", " + clTyp + "* %a1, i32 0, i32 " + idx + "\n");
                switch(atTyp)
                {
                    case "Bool" :
                        out.println("store i8 0, i8* %v" + idx + "\n");
                        break;
                    case "Int" :
                        out.println("store i32 0, i32* %v" + idx + "\n");
                        break;
                    case "String" :
                        out.println("%str" + idx + " = getelementptr inbounds [1 x i8], [1 x i8]* @nullStr, i32 0, i32 0\n");
                        out.println("store i8* %str" + idx + ", i8** %v" + idx + "\n");
                        break;
                    default :
                        out.println("%set" + idx + " = getelementptr %struct." + atTyp + ", %struct." + atTyp + "* %v" + idx + ", i32 0, i32 0\n");
                        out.println("store i8 0, i8* %set" + idx + "\n");
                }
            }

            out.println("ret void\n}\n\n");
        }
        out.println("\n");
        out.println("; Class Methods Definitions\n");
        
        String className;
        
        for(AST.class_ cl: program.classes)
        {
            if(!cl.name.equals("Main"))
            {
                className = cl.name;
                Visit(cl);  //class
            }
            else{
                className = "Main";
                for(AST.feature e :cl.features){
                    if(e.getClass() == AST.method.class){
                        // if(e.getValue().name.equals("main"))
                        //     continue;

                        // if(entry.getKey().equals("type_name"))
                        {
                            out.println("define i8* @"+getMangledName(className,(AST.method)e)+"() {\n");
                            String arStr = "[4 x i8]";
                            out.println("@g" + ++globCnt + " = private unnamed_addr constant " + arStr + " c\"Main\"\n"); 

                            out.println("%v1 = getelementptr inbounds " + arStr + ", " + arStr + "* @g" + globCnt + ", i32 0, i32 0\n");
                            out.println("ret i8* %v1\n}\n");
                        }
                        // else if(!baseFns.contains(entry.getKey()) && !entry.getKey().equals("copy"))  //dont understand
                        // {
                        //     out.println("define "+clNames.get(entry.getValue().typeid)+" @"+getMangledName(className,entry.getValue())+"(");
                        //     Visit(entry.getValue());
                        // }
                    }
                }
                out.println("\n; Main Function\n");
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
    }

    public void Visit(AST.class_ cl){

    }
    public void Visit(AST.method md){

    }
    public void Visit(AST.expression expr, ScopeTable<String> varNames){

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
        out.println("; Global Constants\n");
        out.println("@dStr = private constant [2 x i8] c\"%d\"\n");
        out.println("@sStr = private constant [2 x i8] c\"%s\"\n");
        out.println("@nullStr = private unnamed_addr constant [1 x i8] zeroinitializer\n");

        out.println("; C function\n");
        out.println("declare void @exit(i32)\n");
        out.println("; C function\n");
        out.println("declare i8* @malloc(i64)\n");
        out.println("; C function\n");
        out.println("declare i32 @printf(i8* , ...)\n");
        out.println("; C function\n");
        out.println("declare i32 @scanf(i8* , ..).)\n");
        out.println("; C function\n");
        out.println("declare i32 @strlen(i8*)\n");
        out.println("; C function\n");
        out.println("declare i8* @strcat(i8*, i8*)\n");
        out.println("; C function\n");
        out.println("declare i8* @strcpy(i8*, i8*)\n\n");

        out.println("; Cool Function\n");
        baseFns.add("abort");
        out.println("define void @abort(i32 %a1) {\n");
        out.println("call void @exit(i32 %a1)\n");
        out.println("ret void\n}\n\n");

        out.println("; Cool Function\n");
        baseFns.add("out_string");
        out.println("define void @out_string(i8* %a1) {\n");
        out.println("call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @sStr, i32 0, i32 0),i8* %a1)\n");
        out.println("ret void\n}\n\n");

        out.println("; Cool Function\n");
        baseFns.add("in_string");
        out.println("define void @in_string(i8* %a1) {\n");
        out.println("call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @sStr, i32 0, i32 0),i8* %a1)\n");
        out.println("ret void\n}\n\n");

        out.println("; Cool Function\n");
        baseFns.add("out_int");
        out.println("define void @out_int(i32 %a1) {\n");
        out.println("call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @dStr, i32 0, i32 0),i32 %a1)\n");
        out.println("ret void\n}\n\n");

        out.println("; Cool Function\n");
        baseFns.add("in_int");
        out.println("define i32 @in_int() {\n");
        out.println("%v1 = alloca i32\n");
        out.println("call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @sStr, i32 0, i32 0),i32* %v1)\n");
        out.println("%v2 = load i32, i32* %v1\n");
        out.println("ret i32 %v2\n}\n\n\n");

        out.println("; Cool Function\n");
        baseFns.add("length");
        out.println("define i32 @length(i8* %a1) {\n");
        out.println("%v1 = call i32 @strlen(i8* %a1)\n");
        out.println("ret i32 %v1\n}\n\n");

        out.println("; Cool Function\n");
        baseFns.add("concat");
        out.println("define i8* @concat(i8* %a1, i8* %a2){\n");
        out.println("call i8* @strcat(i8* %a1, i8* %a2)\n");
        out.println("ret i8* %a1\n}\n\n");

        out.println("; Cool Function\n");
        baseFns.add("substr");
        out.println("define i8* @substr(i8* %s, i32 %i, i32 %l){\n");
        out.println("%z = zext i32 %l to i64\n");
        out.println("%str = call noalias i8* @malloc(i64 %z)\n");
        out.println("%ptr = getelementptr inbounds i8, i8* %s, i32 %i\n");
        out.println("%foo = call i8* @strncpy(i8* %str, i8* %ptr, i64 %z)\n");
        out.println("ret i8* %str\n}\n\n");
      
        out.println("\n");
    }
}
