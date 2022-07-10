package si.matjazcerkvenik.eventlogger.util;

public class Test {

    public static void main(String[] args) {

        String s = "{fkljlkj}{asdfasd}";

        String[] sArray = s.split("\\}\\{");

        System.out.println(sArray.length);
    }

}
