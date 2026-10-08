package lemda;

import java.util.Arrays;

public class SecondHighest {
    public static void main(String[]args){
        int [] n = {10,30,21,45,78};
        int result = Arrays.stream(n)
                .boxed()
                .sorted()
                .skip(n.length-2)
                .findFirst()
                .get();
        System.out.println(result);

    }


}
