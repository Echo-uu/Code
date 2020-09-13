import java.util.Scanner;

public class main {
    public static void main(String[] args){
        //System.out.println(args+"\n");

        //System.out.println("a"+'\u0000'+'a');
        //showInteger();
        
        System.out.println(1.0/0);

    }

    public static void showStringMethod(){
        String stringA="string A";
        String stringB=new String(" string B ");
        String stringC=new String();
        int lengthA=stringA.length();
        char A=stringA.charAt(7);
        stringC=stringA.concat(stringB);
        String stringD=stringB.trim();
        System.out.println(stringA+'\n'+stringB+'\n'+stringC+'\n'+stringD);
        System.out.println("stringA length: "+lengthA+'\n'+A
        +'\n'+"String Object 比较方法 : \n");
        Scanner input=new Scanner(System.in);
        System.out.println("Enter two strings");
        String tmp1=input.nextLine();
        String tmp2=input.nextLine();
        if(tmp1.contains(tmp2)||tmp2.contains(tmp1))
            System.out.println("true.\n");
        
    }

    public static void showInteger(){
        System.out.println(Integer.MIN_VALUE+"------"+Integer.MAX_VALUE);
        Integer Int = 100;
        System.out.println(Int.byteValue()+"    "+Int.shortValue()+"      "+Int.toString()+
            "    "+Integer.parseInt("999"));
    }
}