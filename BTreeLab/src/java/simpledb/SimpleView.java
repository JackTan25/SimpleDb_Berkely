package simpledb;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
@SuppressWarnings("ALL")
public class SimpleView {
    static String errorMessage = "";
    static String selectOption;
    static JTable table;
    static String[] newargs = {"E:\\专业课教材\\读研准备\\资料\\数据库实现原版\\BTreeLab\\data\\dblp_simpledb.schema"};
    public static void main(String[] args) {
        init();
        //1.创建框架
        JFrame jf = new JFrame("SimpleDBMS可视化界面");
        jf.setSize(800, 600);
        jf.setLocationRelativeTo(null);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setResizable(true);
        jf.getContentPane().setBackground(Color.pink);
        //2.设置面板
        JPanel panel = new BgPanel((new ImageIcon("C:\\Users\\Administrator\\Desktop\\bgp.jpg")).getImage());
        panel.setBackground(Color.pink);
        panel.setBounds(0,0,800,600);
//      panel.setLayout(new GridLayout(2,1));
        // 创建文本框，指定可见列数为40列
        JPanel pane = new JPanel();
        final JTextField textField = new JTextField(40);
        textField.setFont(new Font(null, Font.PLAIN, 20));
        pane.add(textField);

        // 创建一个按钮，点击后获取文本框中的文本
        JButton btn = new JButton("查询");
        btn.setFont(new Font(null, Font.PLAIN, 20));
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(textField.getText());createTable(textField.getText(),newargs);
                if(!errorMessage.equals("")){
                    JOptionPane.showMessageDialog(panel, errorMessage,"Error信息提示", JOptionPane.PLAIN_MESSAGE);
                    errorMessage = "";
                }else{
                    // 设置滚动面板视口大小（超过该大小的行数据，需要拖动滚动条才能看到）
                    table.setPreferredScrollableViewportSize(new Dimension(700, 400));
                    JPanel p2 = new JPanel();
                    //把表格放到滚动面板中（表头将自动添加到滚动面板顶部）
                    JScrollPane scrollPane = new JScrollPane(table);
                    scrollPane.setBackground(Color.pink);
                    //添加滚动面板到内容面板
                    p2.add(scrollPane);
                    JPanel p = new JPanel();
                    p.add(pane);
                    p.add(p2);
                    jf.setContentPane(p);
                    jf.setVisible(true);
                }
            }
        });
        pane.add(btn);
        panel.add(pane);

        jf.setContentPane(panel);
        jf.setVisible(true);
    }

   public static JTable createTable(String cmd,String newargs[]){
        selectOption = cmd;
       try {
           Class<?> c = Class.forName("simpledb.Parser");
           Class<?> s = String[].class;
           java.lang.reflect.Method m = c.getMethod("main", s);
           m.invoke(null, (java.lang.Object) newargs);
       } catch (ClassNotFoundException cne) {
           System.out.println("无法找到Parser类");
       } catch (Exception e) {
           System.out.println("解析错误");
           e.printStackTrace();
       }
       return table;
    }

    public static void init(){
        //初始化加载目录文件
        Database.getCatalog().loadSchema(newargs[0]);
        TableStats.computeStatistics();
    }
}

class BgPanel extends JPanel
{
    Image im;
    public BgPanel(Image im)
    {
        this.im=im;
        this.setOpaque(true);
    }
    //Draw the back ground.
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponents(g);
        g.drawImage(im,0,0,this.getWidth(),this.getHeight(),this);
    }
}
