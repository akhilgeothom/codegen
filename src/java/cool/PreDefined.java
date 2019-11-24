package cool;

import java.util.*;

public class PreDefined {

	public static void def_string(PrintWriter out, String f_name) {
	    String new_method_name = "String_" + f_name;
	    Operand return_val = null;
	    List<Operand> arguments = null;
	    IRPrinter display = new IRPrinter();

	    // Emitting code for length
	    if (f_name.equals("length")) {
			return_val = new Operand(int_type, "retval");
			arguments = new ArrayList<Operand>();
			arguments.add(new Operand(string_type, "this"));
			display.define(out, return_val.getType(), new_method_name, arguments);
			display.callOperand(out, new ArrayList<OpType>(), "strlen", true, arguments, return_val);
			display.retOperand(out, return_val);
	    }

	    // Emitting code for concat
	    else if (f_name.equals("concat")) {
			return_val = new Operand(string_type, "retval");
			arguments = new ArrayList<Operand>();
			arguments.add(new Operand(string_type, "this"));
			arguments.add(new Operand(string_type, "that"));
			display.define(out, return_val.getType(), new_method_name, arguments);

			return_val = new Operand(string_type, "memnew");
			arguments = new ArrayList<Operand>();
			arguments.add((Operand)new IntValue(1024));
			display.callOperand(out, new ArrayList<OpType>(), "malloc", true, arguments, return_val);

			return_val = new Operand(string_type, "copystring");
			arguments = new ArrayList<Operand>();
			arguments.add(new Operand(string_type, "memnew"));
			arguments.add(new Operand(string_type, "this"));
			display.callOperand(out, new ArrayList<OpType>(), "strcpy", true, arguments, return_val);

			return_val = new Operand(string_type, "retval");
			arguments = new ArrayList<Operand>();
			arguments.add(new Operand(string_type, "copystring"));
			arguments.add(new Operand(string_type, "that"));
			display.callOperand(out, new ArrayList<OpType>(), "strcat", true, arguments, return_val);

			display.retOperand(out, return_val);
	    }

	    // Emitting code for substr

	    else if (f_name.equals("substr")) {
			return_val = new Operand(string_type, "retval");
			arguments = new ArrayList<Operand>();
			arguments.add(new Operand(string_type, "this"));
			arguments.add(new Operand(int_type, "start"));
			arguments.add(new Operand(int_type, "len"));
			display.define(out, return_val.getType(), new_method_name, arguments);

			return_val = new Operand(string_type, "0");
			arguments = new ArrayList<Operand>();
			arguments.add((Operand)new IntValue(1024));
			display.callOperand(out, new ArrayList<OpType>(), "malloc", true, arguments, return_val);

			return_val = new Operand(string_type, "1");
			arguments = new ArrayList<Operand>();
			arguments.add(new Operand(string_type, "this"));
			arguments.add(new Operand(int_type, "start"));
			display.getElementPtr(out, new OpType(OpTypeId.INT8), arguments, return_val, true);

			return_val = new Operand(string_type, "2");
			arguments = new ArrayList<Operand>();
			arguments.add(new Operand(string_type, "0"));
			arguments.add(new Operand(string_type, "1"));
			arguments.add(new Operand(int_type, "len"));
			display.callOperand(out, new ArrayList<OpType>(), "strncpy", true, arguments, return_val);
			out.println("\t%3 = getelementptr inbounds [1 x i8], [1 x i8]* @.str.empty, i32 0, i32 0");
			out.println("\t%retval = call i8* @strcat( i8* %2, i8* %3 )");
			out.println("\tret i8* %retval\n}");
	    }

	    // Emitting code for strcmp
	    else if (f_name.equals("strcmp")) {
			return_val = new Operand(bool_type, "retval");
			arguments = new ArrayList<Operand>();
			arguments.add(new Operand(string_type, "this"));
			arguments.add(new Operand(string_type, "start"));
			display.define(out, return_val.getType(), new_method_name, arguments);

			return_val = new Operand(int_type, "0");
			arguments = new ArrayList<Operand>();
			arguments.add(new Operand(string_type, "this"));
			arguments.add(new Operand(string_type, "start"));
			display.callOperand(out, new ArrayList<OpType>(), "strcmp", true, arguments, return_val);

			display.compareOperand(out, "EQ", return_val, (Operand)new IntValue(0), new Operand(bool_type, "1"));

			display.retOperand(out, new Operand(bool_type, "1"));
	    }
	}

	public static void def_object(PrintWriter out, String f_name) {
	    String new_method_name = "Object_" + f_name;
	    Operand return_val = null;
	    List<Operand> arguments = null;

	    // Method for generating the abort method
	    if (f_name.equals("abort")) {
			return_val = new Operand(void_type, "null");
			arguments = new ArrayList<Operand>();
			display.define(out, return_val.getType(), new_method_name, arguments);

			out.println("call void (i32) @exit(i32 0)");
			out.println("ret void\n}");
	    }
	}

	public void def_io(PrintWriter out, String f_name) {
	    String new_method_name = "IO_" + f_name;
	    Operand return_val = null;
	    List<Operand> arguments = null;

	    // Method for generating the out_string method
	    if (f_name.equals("out_string")) {
			return_val = new Operand(void_type, "null");
			arguments = new ArrayList<Operand>();
			arguments.add(new Operand(string_type, "given"));
			display.define(out, return_val.getType(), new_method_name, arguments);

			out.println("\t%0 = getelementptr inbounds [3 x i8], [3 x i8]* @strfmt, i32 0, i32 0");
			out.println("%call = call i32 ( i8*, ... ) @printf(i8* %0, i8* %given)");
			out.println("ret void\n}");
	    }

	    // Method for generating the out_int method
	    else if (f_name.equals("out_int")) {
			return_val = new Operand(void_type, "null");
			arguments = new ArrayList<Operand>();
			arguments.add(new Operand(int_type, "given"));
			display.define(out, return_val.getType(), new_method_name, arguments);

			out.println("\t%0 = getelementptr inbounds [3 x i8], [3 x i8]* @intfmt, i32 0, i32 0");
			out.println("%call = call i32 ( i8*, ... ) @printf(i8* %0, i32 %given)");
			out.println("ret void\n}");
	    }

	    // Method for generating the in_string method
	    else if (f_name.equals("in_string")) {
			arguments = new ArrayList<Operand>();
			display.define(out, string_type, new_method_name, arguments);

			out.println("\t%0 = bitcast [3 x i8]* @strfmt to i8*");

			return_val = new Operand(string_type, "retval");
			arguments = new ArrayList<Operand>();
			arguments.add((Operand)new IntValue(1024));
			display.callOperand(out, new ArrayList<OpType>(), "malloc", true, arguments, return_val);

			return_val = new Operand(int_type, "1");
			arguments = new ArrayList<Operand>();
			arguments.add(new Operand(string_type, "0"));
			arguments.add(new Operand(string_type, "retval"));
			List<OpType> argTypes = new ArrayList<OpType>();
			argTypes.add(string_type);
			argTypes.add(new OpType(OpTypeId.VAR_ARG));
			display.callOperand(out, argTypes, "scanf", true, arguments, return_val);
			display.retOperand(out, arguments.get(1));
	    }

	    // Method for generating the in_int method
	    else if (f_name.equals("in_int")) {
			arguments = new ArrayList<Operand>();
			display.define(out, int_type, new_method_name, arguments);

			out.println("\t%0 = bitcast [3 x i8]* @intfmt to i8*");

			return_val = new Operand(string_type, "1");
			arguments = new ArrayList<Operand>();
			arguments.add((Operand)new IntValue(4));
			display.callOperand(out, new ArrayList<OpType>(), "malloc", true, arguments, return_val);

			out.println("\t%2 = bitcast i8* %1 to i32*");

			return_val = new Operand(int_type, "3");
			arguments = new ArrayList<Operand>();
			arguments.add(new Operand(string_type, "0"));
			arguments.add(new Operand(new OpType(OpTypeId.INT32_PTR), "2"));
			List<OpType> argTypes = new ArrayList<OpType>();
			argTypes.add(string_type);
			argTypes.add(new OpType(OpTypeId.VAR_ARG));
			display.callOperand(out, argTypes, "scanf", true, arguments, return_val);

			return_val = new Operand(int_type, "retval");
			display.loadOperand(out, int_type, arguments.get(1), return_val);
			display.retOperand(out, return_val);
	    }
	}
}