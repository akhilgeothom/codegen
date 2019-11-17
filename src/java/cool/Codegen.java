package cool;

import java.io.PrintWriter;
import java.util.*;

public class Codegen{
    private String globOut;

    // private Integer varCnt;
    // private Integer labCnt;
    // private Integer globCnt;

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
        // globCnt = 0;
        
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
            classAttrs.put(cl.name,attrs);
        }


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
        globOut += "; Global Constants\n";
        globOut += "@dStr = private constant [2 x i8] c\"%d\"\n";
        globOut += "@sStr = private constant [2 x i8] c\"%s\"\n";
        globOut += "@nullStr = private unnamed_addr constant [1 x i8] zeroinitializer\n";

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
