import java.util.Arrays;
import java.util.Scanner;
 

public class C {
    public static void main(String[] args) {
 
       Scanner cin = new Scanner(System.in);
       int n = cin.nextInt();

       int[] nums = new int[n];

       for (int i = 0; i < n; i++) {
           nums[i] = cin.nextInt();
       }
// int[] nums = {400, 300, 500, 300, 250};
//        int[] nums2 = {100, 150, 200};
//        System.out.println(getMaxValue(nums1));
        System.out.println(getMaxValue(nums));
 
 
    }
 
    public static double getMaxValue(int[] nums) {
        int len = nums.length;
        double[][] table = new double[len][len + 1];//
        table[0][1] = 1;
        for (int i = 0; i < table.length; i++) {
 
            table[i][0] = nums[i];
        }
 
        for (int i = 2; i < table[0].length; i++) {
 
            for (int j = 1; j < table.length; j++) {
 
                double noChange = table[j - 1][i - 1];
 
                double change = noChange * nums[j - 1] * 1.0 / nums[j];
 
                table[j][i] = noChange > change ? noChange : change;
 
 
            }
 
        }
 
 
        for(double []ds :table){
            System.out.println(Arrays.toString(ds));
 
        }
 
        return Math.floor(table[table.length - 1][table[0].length - 1] * 10000) / 100;
    }
 
}