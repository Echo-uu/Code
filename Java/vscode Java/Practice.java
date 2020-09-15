import java.util.*;
import java.util.concurrent.ExecutorService;
import java.io.*;
import java.net.*;

public class Practice {
    // DAY ONE
    public static void main(String[] args) {
        testExecutor();
    }

    public static void dayOne() {
        final Scanner input = new Scanner(System.in);
        boolean continueInput = true;
        do {
            try {
                System.out.println("Enter an integer: ");
                final int number = input.nextInt();

                System.out.println("The number entered is " + number);

                continueInput = false;
            } catch (final InputMismatchException ex) {
                System.out.println("Incorrect input: an integer is required");
                input.nextLine();
            }
        } while (continueInput);
        input.close();

    }

    // DAY TWO
    public static void dayTwo() {
        try {
            System.out.println(sum(new int[] { 1, 2, 3, 4, 5 }));
        } catch (final Exception ex) {
            ex.printStackTrace();
            System.out.println("\n" + ex.getMessage());
            System.out.println("\n" + ex.toString());
            System.out.println("\n Trace Info Obtained from getStackTrace");
            final StackTraceElement[] traceElements = ex.getStackTrace();
            for (int i = 0; i < traceElements.length; i++) {
                System.out.print("method : " + traceElements[i].getMethodName());
                System.out.print("(" + traceElements[i].getClassName() + ":");
                System.out.print(traceElements[i].getLineNumber() + ")");
            }
        }
    }

    private static int sum(final int[] list) {
        int result = 0;
        for (int i = 0; i <= list.length; i++)
            result += list[i];
        return result;
    }

    // DAY THREE 2020.9.5
    public static void dayThree() {
        final int a = 10;
        final int b = 20;
        assert a == 20 : "a!=20";
        System.out.println(a + b);
    }

    // File类操作（文件）
    public static void testFile() {
        final java.io.File file = new java.io.File("miao.jpg");
        System.out.println("Does it exists? " + file.exists());
        System.out.println("The file has " + file.length() + " bytes");
        System.out.println("Can it be read?" + file.canRead());
        System.out.println("Can it be written? " + file.canWrite());
        System.out.println("Is it a directory? " + file.isDirectory());
        System.out.println("Is it a file? " + file.isFile());
        System.out.println("Is it absolute? " + file.isAbsolute());
        System.out.println("Is it hidden? " + file.isHidden());
        System.out.println("Absolute path is " + file.getAbsolutePath());
        System.out.println("Last modified on " + file.lastModified());
        System.out.println("Last modified on " + new java.util.Date(file.lastModified()));
    }

    public static void writeData() throws IOException {
        final File file = new File("test.txt");
        if (file.createNewFile())
            System.out.println("succeed!");
        else
            System.out.println("existed");

        System.out.println("1: using close()    0:using try-with-resources");
        final Scanner cin = new Scanner(System.in);
        final int t = cin.nextInt();
        cin.close();

        if (t == 1) {
            final java.io.PrintWriter output = new java.io.PrintWriter(file);
            output.print("test 3 ");
            output.println("test 2");
            output.print("test 1");
            output.close();
        } else if (t == 0) {
            try (java.io.PrintWriter output = new java.io.PrintWriter(file);) {
                output.print("test 3 ");
                output.println("test 2");
                output.print("test 1");
            }
        } else
            System.out.println("Wrong number!");
    }

    public static void readData() throws FileNotFoundException {
        final File file = new File("test.txt");
        final Scanner input = new Scanner(file);

        while (input.hasNext()) {
            final String test1 = input.next();
            final int num1 = input.nextInt();
            System.out.println(test1 + ' ' + num1);
        }

        input.close();
    }

    // 判断分隔符
    public static void showLineSeparator() {
        final String lineSeparator = System.getProperty("line.separerator");
        System.out.println(lineSeparator);
    }

    // DAY FOUR 2020.9.6
    // URL & Web & 爬取数据
    public static void webCrawler() {
        final Scanner input = new Scanner(System.in);
        System.out.println("Enter a URL");
        final String url = input.nextLine();
        crawler(url);
        input.close();
    }

    public static void crawler(final String startingURL) {
        final ArrayList<String> listOfPendingURLs = new ArrayList<>();
        final ArrayList<String> listOfTraversedURLs = new ArrayList<>();

        listOfPendingURLs.add(startingURL);
        while (!listOfPendingURLs.isEmpty() && listOfTraversedURLs.size() < 100) {
            final String urlString = listOfPendingURLs.remove(0);
            if (!listOfTraversedURLs.contains(urlString)) {
                listOfTraversedURLs.add(urlString);
                System.out.println("Crawl " + urlString);

                for (final String s : getSubURLs(urlString)) {
                    if (!listOfTraversedURLs.contains(s))
                        listOfPendingURLs.add(s);
                }
            }
        }
    }

    public static List<String> getSubURLs(final String urlString) {
        final ArrayList<String> list = new ArrayList<>();

        try (Scanner input = new Scanner(new URL(urlString).openStream())) {
            int current = 0;
            while (input.hasNext()) {
                final String line = input.nextLine();
                current = line.indexOf("https:", current);
                while (current > 0) {
                    // URL结束标志符
                    final int endIndex = line.indexOf("\"", current);
                    if (endIndex > 0) {
                        list.add(line.substring(current, endIndex));
                        current = line.indexOf("http:", endIndex);
                    } else
                        current = -1;
                }
            }
        } catch (final Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }

        return list;
    }

    public static void testGeometricObject() {
        final GeometricObject obj1 = new Circle(5);
        final GeometricObject obj2 = new Rectangle(2, 3);
        System.out.println("The two objects have the same area? " + equalArea(obj1, obj2));
        displayGeometricObject(obj1);
        displayGeometricObject(obj2);
    }

    public static boolean equalArea(final GeometricObject obj1, final GeometricObject obj2) {
        return obj1.getArea() == obj2.getArea();
    }

    public static void displayGeometricObject(final GeometricObject obj) {
        System.out.println("\nThe area is " + obj.getArea() + "\nThe premeter is " + obj.getPerimeter());
    }

    public static void testThread() {
        Runnable printa = new printChar('a', 10000);
        Runnable printb = new printChar('b', 10000);
        Runnable print100 = new printNum(10000);

        Thread thread1 = new Thread(printa);
        Thread thread2 = new Thread(printb);
        Thread thread3 = new Thread(print100);

        //Q : serPriority()无效
        thread3.setPriority(Thread.MAX_PRIORITY);

        thread1.start();
        thread2.start();
        thread3.start();
    }

    public static void testExecutor(){
        ExecutorService executor = Executors.newFixedThreadPool();

        executor.execute(new printChar('a', 100));
        executor.execute(new printChar('b', 100));
        executor.execute(new printNum(100));

        executor.shutdown();
    }
}

// Day FIVE 2020.9.7
// Abstract class and API 抽象类和接口
// 抽象类不可以创建对象，可以包含抽象方法（实现于子类
abstract class GeometricObject {
    private String color = "white";
    private boolean filled;
    private final Date dateCreated;

    protected GeometricObject() {
        dateCreated = new Date();
    }

    protected GeometricObject(final String color, final boolean filled) {
        dateCreated = new Date();
        this.color = color;
        this.filled = filled;
    }

    public String getColor() {
        return color;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    public boolean isFilled() {
        return filled;
    }

    public void setFilled(final boolean filled) {
        this.filled = filled;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    @Override
    public String toString() {
        return "created on" + dateCreated + "\ncolor: " + color + " and filled: " + filled;
    }

    public abstract double getArea();

    public abstract double getPerimeter();
}

class Circle extends GeometricObject {
    private double radius;

    public Circle() {
        super();
    }

    public Circle(final double radius) {
        super();
        this.radius = radius;
    }

    public Circle(final double radius, final String color, final boolean filled) {
        super(color, filled);
        this.radius = radius;
    }

    public void setRadius(final double radius) {
        this.radius = radius;
    }

    public double gerDiameeter() {
        return radius * 2;
    }

    @Override
    public double getArea() {
        return radius * radius * 3.14;
    }

    @Override
    public double getPerimeter() {
        return 2 * radius * 3.14;
    }
}

class Rectangle extends GeometricObject {
    private double width;
    private double height;

    public Rectangle() {
        super();
    }

    public Rectangle(final double width, final double height) {
        super();
        this.width = width;
        this.height = height;
    }

    public Rectangle(final double width, final double height, final String color, final boolean filled) {
        super(color, filled);
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(final double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(final double height) {
        this.height = height;
    }

    @Override
    public double getArea() {
        return width * height;
    }

    @Override
    public double getPerimeter() {
        return 2 * (width + height);
    }
}

// 2020.9.8
// 递归学习

// 2020.9.14
// 多线程和并行程序设计

// public class TaskClass implements Runnable {
// public TaskClass(...) {

// }
// }

class printChar implements Runnable {
    private final char charToPrint;
    private final int times;

    public printChar(final char c, final int t) {
        charToPrint = c;
        times = t;
    }

    // @Override
    // public void run() {
    //     try{
    //         for (int i = 1; i <= times; i++) {
    //             System.out.print(charToPrint);
    //             Thread.sleep(100);
    //         }
    //     }
    //     catch (InterruptedException ex){
    //         ex.printStackTrace();
    //     }
    // }

    @Override
    public void run() {
            for (int i = 1; i <= times; i++) {
                System.out.print(charToPrint);
                //Thread.yield();
            }
    }
}

class printNum implements Runnable {
    private final int num;

    public printNum(final int num) {
        this.num = num;
    }

    @Override
    public void run() {
        // Thread thread4 = new Thread(new printChar('c', 40000));
        // thread4.start();
        for(int i = 1; i <= num; i++){
            System.out.print(" " + i);
        }
    }

    // @Override
    // public void run() {
    //     Thread thread4 = new Thread(new printChar('c', 40000));
    //     thread4.start();
    //     try {
    //         for(int i = 1; i <= num; i++){
    //             System.out.print(" " + i);
    //             if(i == 50) thread4.join();
    //         }
    //     }
    //     catch(InterruptedException ex){
    //         ex.printStackTrace();
    //     }
    // }
}


