import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LSJL extends JFrame implements Runnable {
    private JTextArea wby;
    private JPanel LSJL;
    LSJL(){
        this.setContentPane(this.LSJL);
        this.setTitle("JDM下载器 历史记录");
        ImageIcon btx = new ImageIcon("img/ad.png");
        Image image = btx.getImage();
        this.setIconImage(image);
        this.setSize(600,300);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.setVisible(true);
        wby.setEditable(false);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        BufferedReader peiz = null;
        try {
            peiz = new BufferedReader(new FileReader("peiz/peiz.txt"));
            String sj;
            while ((sj=peiz.readLine())!=null){
                wby.append(sj+"\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {

    }
}
