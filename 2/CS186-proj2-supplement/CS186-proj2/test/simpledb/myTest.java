package simpledb;

@SuppressWarnings("all")
public class myTest {
    public static void main(String[] args) {
        String[] newargs = new String[1];
        newargs[0] = "dblp_simpledb.schema";
        try {
            //dynamically load Parser -- if it doesn't exist, print error message
            Class<?> c = Class.forName("simpledb.Parser");
            Class<?> s = String[].class;

            java.lang.reflect.Method m = c.getMethod("main",s);
            m.invoke(null, (Object) newargs);
        } catch (ClassNotFoundException cne) {
            System.out.println("Class Parser not found -- perhaps you are trying to run the parser as a part of lab1?");
        } catch (Exception e) {
            System.out.println("Error in parser.");
            e.printStackTrace();
        }
    }
}
