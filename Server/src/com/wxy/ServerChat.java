package com.wxy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Administrator
 * @Auther: wuxy
 * @Date: 2021/3/2 - 03 - 02 - 20:54
 * @Description: com.wxy
 * @version: 1.0
 */
public class ServerChat {
    //这是一个main方法，是程序的入口：
    public static void main(String[] args) throws Exception {
        ServerJframe serverJframe = new ServerJframe();
        serverJframe.init();
    }
}

class ServerJframe extends JFrame {
    //GUI相关属性
    JTextArea serverTa = new JTextArea();
    JPanel btnTool = new JPanel();
    JButton startBtn = new JButton("启动");
    JButton stopBtn = new JButton("停止");
    //端口
    private static final int PORT = 8888;
    //ServerSocket
    private ServerSocket serverSocket = null;
    private Socket socket = null;

    //Server接受数据
    private DataInputStream dataInputStream = null;

    // 多个客户端访问时，客户端对象存放入List中
    private ArrayList<ClientCoon> ccList = new ArrayList<ClientCoon>();

    // 服务器启动的标志 (其实ServerSocket ss 初始化出来时以为者服务器的启动)
    private boolean isStart = false;

    public void init() throws Exception {
        this.setTitle("服务器端窗口");
        this.add(serverTa, BorderLayout.CENTER);//流式布局
        btnTool.add(startBtn);
        btnTool.add(stopBtn);
        this.add(btnTool, BorderLayout.SOUTH);

        this.setBounds(0, 0, 500, 500);

        //判断服务器是否已经开启
        if (isStart) {
            System.out.println("服务器已经启动了\n");
        } else {
            System.out.println("服务器还没有启动，请点击启动服务器！\n");
        }
        //按钮监听监听服务器开启，置开始位false
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (serverSocket == null) {
                        serverSocket = new ServerSocket(PORT);
                    }
                    isStart = true;
                    //startServer();
                    serverTa.append("服务器已经启动啦！ \n");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        // 终止按钮监听停止服务器，置开始位true
        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (serverSocket != null) {
                        serverSocket.close();
                        isStart = false;
                    }
                    System.exit(0);
                    serverTa.append("服务器断啦！！\n");
                    System.out.println("服务器断啦！！\n");

                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        /**
         * 服务器窗口关闭应该停止服务器，需改进的代码
         */
        // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // serverTa.setEditable(false);
        this.setVisible(true);
        startServer();
    }

    /**
     * 服务器启动代码
     */
    public void startServer() throws Exception {
        try {
            try {
                serverSocket = new ServerSocket(PORT);
                isStart = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 可以接受多个客户端的连接
            // 接每一個信息时，服务器不可以终断，所以将其写入while（）中，判断符为服务器开关的判断符
            while (isStart) {
                socket = serverSocket.accept();
                ccList.add(new ClientCoon(socket));
                System.out.println("\n" + "一个客户端连接服务器" + socket.getInetAddress() + "/" + socket.getPort());
                serverTa.append("\n" + "一个客户端连接服务器" + socket.getInetAddress() + "/" + socket.getPort());
            }
            //服务器接受客户端一句话
            /*receiveStream();*/
        } catch (SocketException e) {
            System.out.println("服务器终断了！！！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 服务器停止代码
     * */

    /**
     * 服务器接受数据的方法（客户端传送一句话）,不适用多个客户端进行通话
     * */
    /*public void receiveStream(){
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            String str = dataInputStream.readUTF();
            System.out.println(str);
            serverTa.append(str);
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/

    /**
     * @author 武新宇
     * @deprecated 内部类声明 对象 这个对象是属于服务器端的一个连接对象
     */
    class ClientCoon implements Runnable {
        Socket socket = null;

        public ClientCoon(Socket socket) {
            this.socket = socket;
            /**
             * 线程启动在这里：
             * 初始化方法里 初始化一个线程 ，线程中封装的是自己，做整个线程的调用
             */
            (new Thread(this)).start();
        }

        //接受客户端信息（多线程run（）方法）
        @Override
        public void run() {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                // 为了让服务器能够接受到每个客户端的多句话
                while (isStart) {
                    //readUTF()是一种阻塞方法，接一句就执行完了，所以循环中
                    String str = dataInputStream.readUTF();
                    System.out.println("\n" + socket.getInetAddress() + "|" + socket.getPort() + "说" + str + "\n");
                    serverTa.append("\n" + socket.getInetAddress() + "|" + socket.getPort() + "说" + str + "\n");
                    //服务器向每个客户端发送别的客户端发来的信息
                    // 遍历ccList，调用send方法,在客户端里接受应该是多线程的接受
                    String strSend = "\n" + socket.getInetAddress() + "|" + socket.getPort() + "说" + str + "\n";
                    Iterator<ClientCoon> iterator = ccList.iterator();
                    while (iterator.hasNext()) {
                        ClientCoon clientCoon = iterator.next();
                        clientCoon.send(strSend);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 服务器向每個连接对象发送数据的方法
        public void send(String str) {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
                dataOutputStream.writeUTF(str);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}



