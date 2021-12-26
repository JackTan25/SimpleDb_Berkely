import org.junit.Test;
import java.io.*;
import java.util.HashMap;

public class myTest {
    @Test
    public void test() throws IOException {
        File file = new File("E:\\专业课教材\\读研准备\\资" +
                "料\\数据库实现原版\\1\\CS186-proj1\\test\\out.txt");
        System.out.println(file.length());
    }
}
