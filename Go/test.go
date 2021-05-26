package main
import "fmt"
import "strconv"

var _ int64=s()
<<<<<<< HEAD
      
=======
>>>>>>> 9e502fdc2b06f31a3351efbb5a32d74483b10789

func init(){
	fmt.Println("init")
}

func s() int64{
	fmt.Println("function")
	return 1
}

func main(){
	fmt.Println("main")
	// strFmtPrintf()
	// strConv()
	// strParse()
	// pointer()
	// basePrint()
	// flowControl()
}

func print(){
	//var a int =520
	b := "Echo"
	//var c = 99.99
	d := "handsome "
	e := d + b

	var f byte = 'a';
	fmt.Println(f)
	fmt.Printf("%c\n",f)
	fmt.Println(e)
}


//基本类型转字符串
func strFmtPrintf(){
	var num1 int = 99 
	var num2 float64 = 23.456 
	var b bool = true 
	var myChar byte = 'h' 
	var str string

	fmt.Println("Using fmt.Printf(\"%para\",expression) to transfer string")

	str = fmt.Sprintf("%d",num1) 
	fmt.Printf("str type %T str=%q\n",str,str)

	str = fmt.Sprintf("%f",num2) 
	fmt.Printf("str type %T str=%q\n",str,str)

	str = fmt.Sprintf("%t",b) 
	fmt.Printf("str type %T str=%q\n",str,str)

	str = fmt.Sprintf("%c",myChar) 
	fmt.Printf("str type %T str=%q\n\n",str,str)
}

func strConv(){
	var num3 int = 99
	var num4 float64 = 23.456
	var b2 bool = true

	fmt.Println("Using strconv to transfer string")

	str := strconv.FormatInt(int64(num3),10)	
	fmt.Printf("str type %T str=%q\n",str,str)

	//f 格式 10：表示小数位保留10位 64：表示这个小数是float64
	str = strconv.FormatFloat(num4,'f',10,64)
	fmt.Printf("str type %T str=%q\n",str,str)

	str = strconv.FormatBool(b2)
	fmt.Printf("str type %T str=%q",str,str)

	//strconv 包中有一个函数Itoa
	var num5 int64 = 4567
	str = strconv.Itoa(int(num5))
	fmt.Printf("str type %T str=%q\n\n",str,str)

}

//	字符串转基本类型
func strParse(){
	// func ParseBool(str string)(value bool,err error)
	// func ParseFloat(s string,bitSize int)(f float64,err error)
	// func ParseInt(s string,base int,bitSize int)(i int64,err error)
	// func ParseUint(s string,b int,bitSize int)(n uint64,err error)

	var str string = "true"
	var b bool
	b,_ = strconv.ParseBool(str)
	fmt.Printf("b type %T b=%v \n",b,b)

	var str2 string = "1234590"
	var n1 int64
	var n2 int
	n1,_ = strconv.ParseInt(str2,10,64)
	n2 = int(n1)
	fmt.Printf("n1 type %T n1=%v\n",n1,n1)
	fmt.Printf("n2 type %T n1=%v\n",n2,n2)

	var str3 string = "123.456"
	var f1 float64
	f1,_ = strconv.ParseFloat(str3,64)
	fmt.Printf("f1 type %T f1=%v",f1,f1)
}

//指针
func pointer(){
	num := 10
	fmt.Println("num address: ", &num)
	adr := &num
	fmt.Println("adr :", adr)
	fmt.Println("adr ponits value : ", *adr)
}

func basePrint(){
	var i int = 5
	fmt.Printf("%b \n",i)//二进制输出
	var j int = 011 //八进制
	fmt.Println("j",j)
	var k int = 0x11 //16进制
	fmt.Println("k",k)
}

func flowControl(){
	/*switch语句
	fallthrough 可以穿透一个case，继续执行下一个case
	type 可以使用type判断interface变量指向的变量类型*/

	//Golang 提供 for-range的方式，可以方便遍历字符串和数组
	//传统方式 
	var str1 string = "hello world"
	for i :=0;i < len(str1);i++{
		fmt.Printf("%c \n",str1[i])
	}
	//传统方式弊端如果str 中含有中文，会出现乱码，因为一个汉字占三个字节，需要用到切片
	var str2 string = "hello 北京"
	str3 := []rune(str2)//把str2 转成[]rune
	for i :=0;i < len(str3);i++{
		fmt.Printf("%c \n",str3[i])
	}

	//for-range 方式：是按字符方式遍历，如果有中文也是ok
	str4 := "abc~ok,北京"
	for index,val := range str4{
		fmt.Printf("index=%d, val=%c \n",index,val)
	}
	
	//goto lable1
	//...
	//label1

}