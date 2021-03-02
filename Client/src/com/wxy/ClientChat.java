package com.wxy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author Administrator
 * @Auther: wuxy
 * @Date: 2021/3/2 - 03 - 02 - 20:16
 * @Description: com.wxy
 * @version: 1.0
 */
public class ClientChat {
    //这是一个main方法，是程序的入口：
    public static void main(String[] args) {
        ClientJframe clientJframe = new ClientJframe();
        clientJframe.init();
    }
}

class ClientJframe extends JFrame {
    //GUI布局
    //聊天记录显示区
    private JTextArea ta = new JTextArea(10, 20);
    //聊天记录输入区
    private JTextField tf = new JTextField(20);

    //端口
    // 静态常量主机端口号
    private static final String CONNSTR = "127.0.0.1";
    // 静态常量服务器端口号
    private static final int CONNPORT = 8888;
    private Socket socket = null;

    //Client发送数据
    private DataOutputStream dataOutputStream = null;

    //客户端连接上服务器判断符号
    private boolean isConn = false;

    /**
     * 无参的构造方法 throws HeadlessException
     */
    public ClientJframe() throws HeadlessException {
        super();
    }

    public void init() {
        this.setTitle("客户端窗口");
        this.add(ta, BorderLayout.CENTER);
        this.add(tf, BorderLayout.SOUTH);

        this.setBounds(300, 300, 400, 400);

        // 添加监听，使回车键可以输入数据(判断数据合法性)，
        // 并輸入到聊天框，换行
        tf.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String strSend = tf.getText();
                // 去掉空格判断长度是否为空
                if (strSend.trim().length() == 0) {
                    return;
                }
                //客户端信息strSend发送到服务器上
                send(strSend);
                tf.setText("");
                //ta.append(strSend + "\n");

            }
        });

        //关闭事件
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ta.setEditable(false);//聊天区域不可以输入
        tf.requestFocus();//光标聚焦

        try {
            socket = new Socket(CONNSTR, CONNPORT);
            isConn = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 启动多线程
        new Thread(new Receive()).start();

        this.setVisible(true);
    }

    /**
     * 客户端发送信息到服务器上的方法
     */
    public void send(String str) {
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(str);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @author 武新宇
     * @deprecated 多线程的类，实现了Runnable接口的类
     */
    class Receive implements Runnable {
        @Override
        public void run() {
            try {
                while (isConn) {
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    String str = dataInputStream.readUTF();
                    //通讯消息
                    ta.append(str);
                }
            } catch (SocketException e) {
                System.out.println("服务器意外终止了！");
                ta.append("服务器意外终止了！");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}