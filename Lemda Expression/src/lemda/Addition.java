package lemda;

interface Add{
    int Additions(int a ,int b);
}

public class Addition {
    public static  void main(String [] args){
     Add add = (a,b)-> a+b;
     int result = add.Additions(10,20);
     System.out.println(result);
    }
}
