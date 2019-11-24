package cool;

import java.io.PrintWriter;
import java.util.*;

public class Printer {
  void escapedString(PrintWriter out, String str) {
    for(int i = 0; i < str.length(); i++) {
      if (str.charAt(i) == '\\') {
        out.print("\\5C");
      } 
      else if (str.charAt(i) == '\"') {
        out.print("\\22");
      } 
      else if (str.charAt(i) == '\n') {
        out.print("\\0A");
      } 
      else if (str.charAt(i) == '\t') {
        out.print("\\09");
      } 
      else {
        out.print(str.charAt(i));
      }
    }   
  }

  void initConstant(PrintWriter out, String name, ConstValue op) {
    out.print("@" + name + " = ");
    out.print("constant " + op.getTypename() + " ");
    if (op.getType().getId() == OpTypeId.INT8) {
      out.print("c\"");
      escapedString(out, op.getValue());
      out.print("\\00\"\n");
    } 
    else {
      out.print(op.getValue() + "\n");
    }
  }

  void define(PrintWriter out, OpType retType, String name, List<Operand> args) {
    out.print("define " + retType.getName() + " @" + name + "( ");
    for(int i = 0; i < args.size(); i++) {
      if (i != args.size() - 1) {
        out.print(args.get(i).getTypename() + " " + args.get(i).getName() + ", ");
      } 
      else {
        out.print(args.get(i).getTypename() + " " + args.get(i).getName() + "");
      }
    }
    out.println(" ) {\nentry:");
  }

  void declare(PrintWriter out, OpType retType, String name, List<OpType> args) {
    out.print("declare " + retType.getName() + " @" + name + "( ");
    for(int i = 0; i < args.size(); i++) {
      if (i != args.size() - 1) {
        out.print(args.get(i).getName() + ", ");
      } 
      else {
        out.print(args.get(i).getName() + "");
      }
    }
    out.print(" )\n");
  }

  void typeDefine(PrintWriter out, String className, List<OpType> attributes) {
    out.print("%class." + className + " = type { ");
    for(int i = 0; i < attributes.size(); i++) {
      if (i != attributes.size() - 1) { 
        out.print(attributes.get(i).getName() + ", ");
      } 
      else {
        out.print(attributes.get(i).getName() + "");
      }
    }
    out.print(" }\n");
  }

  void arithOperand(PrintWriter out, String operation, Operand op1, Operand op2, Operand result) {
    out.print("\t");
    if (result.getType().getId() != OpTypeId.VOID) {
      out.print(result.getName() + " = ");
    }
    out.print(operation + " " + op1.getTypename() + " " + op1.getName() + ", "  + op2.getName() + "\n");
  }

  void allocaOperand(PrintWriter out, OpType type, Operand result) {
    out.print("\t" + result.getName() + " = alloca " + type.getName() + "\n");
  }

  void loadOperand(PrintWriter out, OpType type, Operand op, Operand result) {
    out.print("\t" + result.getName() + " = load " + type.getName() + ", " + op.getTypename() + " "
          + op.getName() + "\n");
  }

  void storeOperand(PrintWriter out, Operand op, Operand result) {
    out.print("\tstore " + op.getTypename() + " " + op.getName() + ", " + result.getTypename() + " "
          + result.getName() + "\n");
  }
  
  void getElementPtr(PrintWriter out, OpType type, List<Operand> operandList, Operand result, boolean inbounds) {
    out.print("\t");
    if (result.getType().getId() != OpTypeId.VOID) {
      out.print(result.getName() + " = ");
    }
    out.print("getelementptr ");
    if (inbounds == true) {
      out.print("inbounds ");
    } 
    out.print(type.getName() + ", ");
    for(int i = 0; i < operandList.size(); i++) {    
      if (i != operandList.size() - 1) {
         out.print(operandList.get(i).getTypename() + " " + operandList.get(i).getName() + ", ");
      } 
      else {
         out.print(operandList.get(i).getTypename() + " " + operandList.get(i).getName() + "\n");
      }
    }
  }
  
  void getElementPtrEmbed(PrintWriter out, OpType type, Operand op1, Operand op2, Operand op3, boolean inbounds) {
    out.print("\tgetelementptr ");
    if (inbounds == true) {    
      out.print("inbounds ");
    }
    out.print("( " + type.getName() + ", " + op1.getTypename() + "* " + op1.getName() + ", "
          + op2.getTypename() + " " + op2.getName() + ", " + op3.getTypename() + " " + op3.getName() + ")\n");
  }

  void branchCondOperand(PrintWriter out, Operand op, String labelTrue, String labelFalse) {
    out.print("\tbr " + op.getTypename() + " " + op.getName() + ", label %" + labelTrue
          + ", label %" + labelFalse + "\n");
  }

  void branchUncondOperand(PrintWriter out, String label) {
    out.print("\tbr label %" + label + "\n");
  }

  void compareOperand(PrintWriter out, String cond, Operand op1, Operand op2, Operand result) {
    out.print("\t" + result.getName() + " = icmp ");
    if (cond.equals("EQ")) {
      out.print("eq ");
    } 
    else if (cond.equals("LT")) {
      out.print("slt ");
    } 
    else if (cond.equals("LE")) {
      out.print("sle ");
    }
    out.print(op1.getTypename() + " " + op1.getName() + ", " + op2.getName() + "\n");
  }

  void callOperand(PrintWriter out, List<OpType> argTypes, String funcName, boolean isGlobal, List<Operand> args, Operand resultOp) {
    out.print("\t");
    if (resultOp.getType().getId() != OpTypeId.VOID) {
      out.print(resultOp.getName() + " = ");
    }
    out.print("call " + resultOp.getTypename());
    if (argTypes.size() > 0) {
      out.print(" (");
      for (int i = 0; i < argTypes.size(); i++) {
        if (i != argTypes.size() - 1) {
          out.print(argTypes.get(i).getName() + ", ");
        } 
        else {
          out.print(argTypes.get(i).getName() + ") ");
        }
      }
    }
    if (isGlobal == true) {
      out.print(" @");
    } 
    else {
      out.print(" %");
    } 
    out.print(funcName + "( ");
    for (int i = 0; i < args.size(); i++) {
      if (i != args.size() - 1) {      
        out.print(args.get(i).getTypename() + " " + args.get(i).getName() + ", ");
      } 
      else {
        out.print(args.get(i).getTypename() + " " + args.get(i).getName() + "");
      }
    }
    out.print(" )\n");
  }

  void retOperand(PrintWriter out, Operand op) {
    out.print("\tret ");
    if (op.getType().getId() != OpTypeId.VOID) {
      out.print(op.getTypename() + " " + op.getName() + "\n");
    } 
    else {
      out.print("void\n");
    }  
    out.print("}\n");
  }
}
