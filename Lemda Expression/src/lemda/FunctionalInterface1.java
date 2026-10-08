package lemda;

import java.lang.FunctionalInterface;

@FunctionalInterface
interface Display{
    void display();
}
public class FunctionalInterface1 {
    public static void main(String[] args){
        Display d =()->System.out.println("Hello my name is kaushik");
        d.display();
    }
}
