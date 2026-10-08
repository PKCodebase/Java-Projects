package lemda;

interface FunctionalInterface{
    void abstractFun(int x);
    default  void normalFun(){
        System.out.println("Hello");
    }
}
public class Functional {
    public static void main(String [] args){
        FunctionalInterface fun =(x)->System.out.println(2*x);
        fun.abstractFun(5);

       fun.normalFun();
    }
}
