; ModuleID = 'helloworld.cl'
source_filename = "helloworld.cl"
target datalayout = "e-m:e-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-pc-linux-gnu"

; Global Constants

@dStr = private constant [2 x i8] c"%d"

@sStr = private constant [2 x i8] c"%s"

@nullStr = private unnamed_addr constant [1 x i8] zeroinitializer

; C function

declare void @exit(i32)

; C function

declare i8* @malloc(i64)

; C function

declare i32 @printf(i8* , ...)

; C function

declare i32 @scanf(i8* , ..).)

; C function

declare i32 @strlen(i8*)

; C function

declare i8* @strcat(i8*, i8*)

; C function

declare i8* @strcpy(i8*, i8*)


; Cool Function

define void @abort(i32 %a1) {

call void @exit(i32 %a1)

ret void
}


; Cool Function

define void @out_string(i8* %a1) {

call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @sStr, i32 0, i32 0),i8* %a1)

ret void
}


; Cool Function

define void @in_string(i8* %a1) {

call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @sStr, i32 0, i32 0),i8* %a1)

ret void
}


; Cool Function

define void @out_int(i32 %a1) {

call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @dStr, i32 0, i32 0),i32 %a1)

ret void
}


; Cool Function

define i32 @in_int() {

%v1 = alloca i32

call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @sStr, i32 0, i32 0),i32* %v1)

%v2 = load i32, i32* %v1

ret i32 %v2
}



; Cool Function

define i32 @length(i8* %a1) {

%v1 = call i32 @strlen(i8* %a1)

ret i32 %v1
}


; Cool Function

define i8* @concat(i8* %a1, i8* %a2){

call i8* @strcat(i8* %a1, i8* %a2)

ret i8* %a1
}


; Cool Function

define i8* @substr(i8* %s, i32 %i, i32 %l){

%z = zext i32 %l to i64

%str = call noalias i8* @malloc(i64 %z)

%ptr = getelementptr inbounds i8, i8* %s, i32 %i

%foo = call i8* @strncpy(i8* %str, i8* %ptr, i64 %z)

ret i8* %str
}






%class.Main = type { i8,
 }

%class.Main = type { i32, i8*, %class.Main.Base }
; Class Initializtion Methods
define void @init_Main(%struct.Main* %a1) {
%v0 = getelementptr %struct.Main, %struct.Main* %a1, i32 0, i32 0
store i8 1, i8* %v0
ret void
}



; Class Methods Definitions

define i8* @_CN4_Main_FN4main_AL0_NP_() {
@g1 = private unnamed_addr constant [4 x i8] c"Main"
%v1 = getelementptr inbounds [4 x i8], [4 x i8]* @g1, i32 0, i32 0
ret i8* %v1
}

; Main Function


