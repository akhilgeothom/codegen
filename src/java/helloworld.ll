; I am a comment in LLVM-IR. Feel free to remove me.
target datalayout = "e-m:e-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-unknown-linux-gnu"
@Abortdivby0 = private unnamed_addr constant [22 x i8] c"Error: Division by 0\0A\00", align 1
@Abortdispvoid = private unnamed_addr constant [25 x i8] c"Error: Dispatch on void\0A\00", align 1

declare i32 @printf(i8*, ...)
declare i32 @scanf(i8*, ...)
declare i32 @strcmp(i8*, i8*)
declare i8* @strcat(i8*, i8*)
declare i8* @strcpy(i8*, i8*)
declare i8* @strncpy(i8*, i8*, i32)
declare i64 @strlen(i8*)
declare i8* @malloc(i64)
declare void @exit(i32)
@strformatstr = private unnamed_addr constant [3 x i8] c"%s\00", align 1
@intformatstr = private unnamed_addr constant [3 x i8] c"%d\00", align 1

%class.Object.Base = type { }
%class.Object = type { i32, i8*, %class.Object.Base }

%class.IO.Base = type {%class.Object.Base* }
%class.IO = type { i32, i8*, %class.IO.Base }

%class.Main.Base = type {%class.Object.Base* }
%class.Main = type { i32, i8*, %class.Main.Base }

@_ZTV6Object = constant [3 x i8*] [i8* bitcast (%class.Object* ( %class.Object* )* @_ZN6Object5abort to i8*), i8* bitcast ([1024 x i8]* ( %class.Object* )* @_ZN6Object9type_name to i8*), i8* bitcast (i32 ( %class.Object* )* @_ZN6Object4copy to i8*)] 
@_ZTV2IO = constant [7 x i8*] [i8* bitcast (%class.Object* ( %class.Object* )* @_ZN6Object5abort to i8*), i8* bitcast ([1024 x i8]* ( %class.Object* )* @_ZN6Object9type_name to i8*), i8* bitcast (%class.IO* ( %class.IO* )* @_ZN2IO4copy to i8*), i8* bitcast (%class.IO* ( %class.IO*, [1024 x i8]* )* @_ZN2IO10out_string to i8*), i8* bitcast (%class.IO* ( %class.IO*, i32 )* null to i8*), i8* bitcast ([1024 x i8]* ( %class.IO* )* @_ZN2IO9in_string to i8*), i8* bitcast (i32 ( %class.IO* )* @_ZN2IO9in_int to i8*)] 
@_ZTV4Main = constant [4 x i8*] [i8* bitcast (%class.Object* ( %class.Object* )* @_ZN6Object5abort to i8*), i8* bitcast ([1024 x i8]* ( %class.Object* )* @_ZN6Object9type_name to i8*), i8* bitcast (i32 ( %class.Object* )* @_ZN4Main4copy to i8*), i8* bitcast (%class.IO* (  )* @_ZN4Main4main to i8*)] 
define %classObject* @_ZN6Object5abort( %class.Object* %this ) noreturn {
entry:
call void @exit( i32 1 )
ret %classObject* null
}

define void @_Z6ObjectBaseC ( %class.Object.Base ) {
entry:
ret void
}

define [1024 x i8]* @_ZN6Object9type_name( %class.Object* %this ) {
entry:
%0 = getelementptr inbounds %classObject, %classObject* %this, i32 0, i32 0
%1 = load i32, i32* %0
%2 = getelementptr inbounds [8 x [1024 x i8]], [8 x [1024 x i8]]* @classnames, i32 0, i32 %1
%retval = call [1024 x i8]* @_ZN6String4copy( [1024 x i8]* %2 )
ret [1024 x i8]* %retval
}

define void @_Z2IOBaseC ( %IO.Base*%this ) { 
entry: 
%0 = getelementptr inbouds %class.Object.Base.Base, class.Object.Base.Base* %this, i32 0, i32 0
call void @_Z11Object.BaseBaseC( %class.Object.Base.Base*%0
return void
define void @_Z4MainBaseC ( %Main.Base*%this ) { 
entry: 
%0 = getelementptr inbouds %class.Object.Base.Base, class.Object.Base.Base* %this, i32 0, i32 0
call void @_Z11Object.BaseBaseC( %class.Object.Base.Base*%0
return void
@.str = private unnamed_addr constant [14 x i8] c"Hello world!
\00", align 1
