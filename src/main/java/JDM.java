import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class JDM extends JFrame implements Runnable {
    private final Image image;
    private String dz;
    private JFileChooser bcdz;//保存地址弹窗
    private ExecutorService xzxc;//线程池
    private JButton xz;//下载按钮
    private JTextField url;//url
    private JPanel JDM;//主界面
    private JLabel w1;//提示词1
    private JLabel w2;//提示词2
    private JLabel w3;//提示线程文本
    private JTextField xc;//修改线程数
    private JButton xzdz;
    private JLabel xians;
    private JButton Button1;


    JDM(){
        //页面初始化
        this.setContentPane(this.JDM);
        this.setTitle("JDM下载器");
        ImageIcon btx = new ImageIcon("img/ad.png");
        image = btx.getImage();
        this.setIconImage(image);
        this.setSize(600,300);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setVisible(true);

        //线程池
        xzxc = Executors.newFixedThreadPool(16);

        //窗口关闭监听器
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                xzxc.shutdown();
                System.exit(0);
            }
        });
    }

    @Override
    public void run() {
        xzdz.addActionListener(e -> {
            //点击选择文件夹
            bcdz = new JFileChooser();
            bcdz.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = bcdz.showDialog(null, "选择文件夹");
            bcdz.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            if (result==JFileChooser.APPROVE_OPTION){
                dz = bcdz.getSelectedFile().getAbsolutePath();
                xians.setText(dz);
            }
        });
        Button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new LSJL());
            }
        });
        xz.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //修改线程池
                int xcs = Integer.parseInt(xc.getText());
                if (xcs!=16){
                    xzxc.shutdown();
                    xzxc = Executors.newFixedThreadPool(xcs);
                }
                //判断输入
                String wjurl = url.getText();
                if (Objects.equals(wjurl, "")){
                    tanc("请输入下载地址");
                    return;
                }
                //http://www.ldszyn.xyz/down.php/9d8067d189dc4be4ebe2e0d3c764b121.apk
                //获取后缀
                String[] groups = wjurl.split("\\.");
                String hzm;
                if (groups.length > 0) {
                    hzm = groups[groups.length - 1];
                }else {
                    tanc("请确认下载地址正确");
                    return;
                }
                if (Objects.equals(dz, "")){
                    tanc("请选择地址");
                    xzdz.doClick();
                }

                //获取文件大小
                int fileSize;
                try {
                    URL url = new URL(wjurl);
                    URLConnection uc = url.openConnection();
                    fileSize = uc.getContentLength();// 文件大小
                } catch (IOException ex) {
                    tanc("无法获取文件大小，请检查网址");
                    return;
                }
                Date time1 = new Date();
                long time11 = time1.getTime();
                int partSize = fileSize / xcs; // 每个线程要下载的数据量
                String[] paths = new String[xcs];//各个文件的地址
                CountDownLatch latch = new CountDownLatch(xcs);//多线程计数器

                for (int i = 0; i < xcs; i++) {
                    final int part = i;
                    final int lowerBound = partSize * part;
                    final int upperBound = (part == xcs - 1) ? fileSize : (lowerBound + partSize) - 1;
                    final String path = dz + "//part_" + part + "." + hzm;//各个文件名
                    paths[part] = path;
                    xzxc.execute(() -> {
                        try {
                            URLConnection conn = new URL(wjurl).openConnection();
                            String byteRange = lowerBound + "-" + upperBound;
                            conn.addRequestProperty("Range", "bytes=" + byteRange);
                                BufferedInputStream inputStream = new BufferedInputStream(conn.getInputStream());
                                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(path));
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }
                            inputStream.close();
                            outputStream.close();
                            latch.countDown();
                        } catch (IOException ex) {
                            tanc("下载出错: " + path);
                        }
                    });
                }
                //堵塞至下载结束
                try {
                    latch.await();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                Date time2 = new Date();
                long time = time2.getTime();
                int l = (int) ((time - time11)/1000);
                tanc("下载完成,用时约"+l+"秒,开始聚合");
                //聚合
                //dz+"//"+time+"."+hzm
                mergeFiles(paths, dz+"//"+time+"."+ hzm);
                /*单独线程用于 输出用时 进度
                Date date = new Date();
                long startTime = new Date().getTime();
                long xzjd = 0;
                double tmp = 0.0;
                xzjd++;
                double progress = (double) xzjd / fileSize * 100;
                if (tmp+12.5<progress){
                    //实时输出用时 进度
                    long currentTime = new Date().getTime();
                    double elapsedTime = (double)(currentTime - startTime) / 1000.0;
                    System.out.printf("已完成 %.1f%% , 用时 %.2f秒 \n", progress, elapsedTime);
                    tmp=progress;
                }
                long currentTime = new Date().getTime();
                double elapsedTime = (double)(currentTime - startTime) / 1000.0;
                tanc("下载完成，用时"+String.valueOf(elapsedTime)+"秒");*/
            }
        });
    }
    void mergeFiles(String[] fileList,String dz) {
        try {
            Date tt1 = new Date();
            // 创建输出流
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(dz));
            // 逐个合并文件
            for (String filePath : fileList) {
                BufferedInputStream input = new BufferedInputStream(new FileInputStream(filePath));
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                input.close();
                //删除临时文件
                File tempFile = new File(filePath);
                tempFile.delete();
            }
            // 关闭输出流
            output.close();
            Date tt2 = new Date();
            int l = (int) ((tt2.getTime() - tt1.getTime())/100);
            tanc("文件合并完成,用时约0."+l+"秒");
            //存入配置
            BufferedWriter peiz = new BufferedWriter(new FileWriter("peiz/peiz.txt",true));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fdate = dateFormat.format(tt2);
            String line = String.format("下载日期:%s,下载到的地址:%s",fdate,dz);
            peiz.write(line);
            peiz.newLine();
            peiz.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    void tanc(String text){
        JDialog tc = new JDialog();
        tc.setTitle("提示");
        tc.setIconImage(image);
        tc.setSize(200,200);
        tc.setLocationRelativeTo(null);
        tc.setResizable(false);
        tc.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        JLabel wb = new JLabel(text);
        tc.getContentPane().add(wb);
        tc.setVisible(true);
    }
}
