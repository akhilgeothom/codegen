package cool;

import java.io.PrintWriter;

public class Codegen{
    public Codegen(AST.program program, PrintWriter out){
        // out.println("; I am a comment in LLVM-IR. Feel free to remove me.");

        out.println("; ModuleID = \'"+program.classes.get(0).filename+"\'");
        out.println("source_filename = \""+program.classes.get(0).filename+"\"");
        out.println("target datalayout = \"e-m:e-i64:64-f80:128-n8:16:32:64-S128\"")
        out.println("target triple = \"x86_64-pc-linux-gnu\"\n");

        progOut = "";
        //Write Code generator code here
        className = "";
        indent = "  ";

        globOut = "";
        globOut += "; Global Constants\n";
        globOut += "@dStr = private constant [2 x i8] c\"%d\"\n";
        globOut += "@sStr = private constant [2 x i8] c\"%s\"\n";
        globOut += "@nullStr = private unnamed_addr constant [1 x i8] zeroinitializer\n";

        varCnt = 0;
        labCnt = 0;
        globCnt = 0;
        
        clNames = new HashMap<String,String>();
        clNames.put("Int","i32");
        clNames.put("String","i8*");
        clNames.put("Bool","i8");

        baseFns = new ArrayList<String>();


        classAttrs = new HashMap<String,ArrayList<AST.attr>>();




    }
}
