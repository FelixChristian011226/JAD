package Homework.W9;

//5-1 计算整数序列和

//import java.util.*;
////线程类：计算整数序列和
//class SumList
//    implements Runnable{
//    private ArrayList lt=null;//存放整数序列的列表
//    SumList(ArrayList lt){
//        this.lt=lt;
//    }
//    //线程体
//    public void run() {
//        int sum=0;
//        for (int i = 0; i < lt.size(); i++)
//            sum+=(int)lt.get(i); //取出序列元素累加
//        System.out.println(sum);
//    }
//}
////测试类
//public class Main {
//    public static void main(String[] args) {
//        Scanner sc = new Scanner(System.in);
//        int n=sc.nextInt();//输入序列个数
//        int m=sc.nextInt();//输入每个序列中的整数个数
//        for (int i = 0; i < n; i++) {
//            ArrayList score=new ArrayList(); //创建求和列表对象
//            for (int j = 0; j < m; j++) {
//                score.add(sc.nextInt());
//            }
//            SumList sl=new SumList(score);
//            Thread th=new Thread(sl);//创建线程对象
//            th.start(); //启动线程
//        }
//        sc.close();
//    }
//}

//5-2 利用线程同步机制执行正确的计数

//import java.util.*;
//class BackCounter implements Runnable{
//    private int count=100;  //线程共享变量，对它的处理必须用同步机制进行保护
//    public int getCount() { return count; }//返回变量值
//    //线程体
//    public void  run() {
//        for(int i=10;i>0;i--) { //变量值递减 10
//
//            synchronized(this)
//            { //以下代码在处理共享变量，需要同步机制保护
//                if( count<=0 ) break;
//                count--;
//            }
//            try { Thread.sleep(10); } catch ( InterruptedException e ) { }//模拟延时 10 毫秒
//        }
//    }//线程体结束
//}
//public class Main {
//    public static void main(String[] args) throws InterruptedException {//某些线程方法会抛出检查型异常
//        ArrayList<Thread> lt=new ArrayList<Thread>();
//        BackCounter bc=new BackCounter();//创建实现类对象
//        lt.add(new Thread(bc));//创建线程对象
//        lt.add(new Thread(bc));
//        for (Thread th:lt)
//            th.start()
//                    ; //启动线程
//        for (Thread th:lt)
//            th.join()
//                    ;  //等待线程结束
//        System.out.println(bc.getCount());
//    }
//}

//6-1 jmu-Java-07多线程-交替执行

import java.util.*;

class Repo {
    String s;
    List<String> list;

    /*将传递进来的字符串以空格分隔分解为多个不同的任务，并存储起来。如"1 2 3 4 5 6"被分解成6个任务1,2,3,4,5,6*/
    public Repo(String s) {
        this.s = s;
        list = new ArrayList<>();
        String[] arr = s.split(" ");
        for (String str : arr) {
            list.add(str);
        }
    }
    public int getSize() {
        return list.size();
    }

    public synchronized boolean work() {
        this.notify();
        if (list.size() == 0) {
            return false;
        }
        else {
            System.out.println(Thread.currentThread().getName() + " finish " + list.remove(0));
            return true;
        }
    }

}

class Worker1 implements Runnable {
    Repo repo;

    public Worker1(Repo repo) {
        this.repo = repo;
    }

    @Override
    public void run() {
        while (repo.work()) {
            try {
                synchronized (repo) {
                    repo.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Worker2 implements Runnable {
    Repo repo;

    public Worker2(Repo repo) {
        this.repo = repo;
    }

    @Override
    public void run() {
        while (repo.work()) {
            try {
                synchronized (repo) {
                    repo.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        Repo repo = new Repo(sc.nextLine());
        Thread t1 = new Thread(new Worker1(repo));
        Thread t2 = new Thread(new Worker2(repo));
        t1.start();
        Thread.yield();
        t2.start();
        sc.close();
    }
}