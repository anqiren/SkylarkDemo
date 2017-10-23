package com.skylark;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {

    public static void main(String[] args) {
        try {
            SocketServer socketServer = new SocketServer(7878);
            socketServer.start();
            System.out.println("server is running");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ExecutorService executorService;//�̳߳�
    private int port;//�����˿�
    private boolean quit = false;//�˳�
    private ServerSocket server;

    public SocketServer(int port) {
        this.port = port;
        //�����̳߳أ����о���(cpu����*50)���߳�
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 50);
    }

    /**
     * �˳�
     */
    public void quit() {
        this.quit = true;
        try {
            server.close();
        } catch (IOException e) {
        }
    }

    /**
     * ��������
     *
     * @throws Exception
     */
    public void start() throws Exception {
        server = new ServerSocket(port);
        while (!quit) {
            try {
                Socket socket = server.accept();
                //Ϊ֧�ֶ��û��������ʣ������̳߳ع���ÿһ���û�����������
                executorService.execute(new SocketTask(socket));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final class SocketTask implements Runnable {
        private Socket socket = null;

        public SocketTask(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                final int orderLen = 1024;
                byte[] orderArr = new byte[orderLen];
                while (true) {
                    /**
                     * ���ղ�����ָ��
                     */
                    System.out.println("���ղ�����ָ��");
                    // �ȴ�client����ָ��
                    int realOrderLen = in.read(orderArr, 0, orderLen);
                    if (realOrderLen < 0) {
                        break;
                    }
                    String orderStr = new String(orderArr, 0, realOrderLen);
                    System.out.println("���յ�ָ��Ϊ��" + orderStr);
                    // ����ָ��
                    String[] order = orderStr.trim().split("\\|"); // ������������ʽ����б�ߺ�һ������֮��û�пո�
                    final String filePath = order[0];
                    final int piece = Integer.parseInt(order[1]);
                    final int pieceSize = Integer.parseInt(order[2]);

                    /**
                     * ͨ��ָ���ȡָ���ļ��е�ָ����
                     */
                    System.out.println("ͨ��ָ���ȡָ���ļ��е�ָ����");
                    byte[] buf = new byte[pieceSize]; // ������
                    FileInputStream f = new FileInputStream(filePath);
                    f.skip(piece * (long) pieceSize); // ��ת
                    int realBufLen = f.read(buf);  // ��ȡ
                    f.close();

                    /**
                     * ���ݸ�����Ч���ݳ��Ⱥ�MD5ֵ
                     */
                    System.out.println("���ݸ�����Ч���ݳ��Ⱥ�MD5ֵ");
                    // ��ȡmd5ֵ����lenת����byte[]��ͨ��socket����
                    byte[] md5 = ByteMD5(buf);
                    byte[] lenInB = ByteBuffer.allocate(4).putInt(realBufLen).array();
                    // �ϲ�����Ч���ݳ��ȡ����ݵ�md5ֵ������
                    ByteBuffer byteBuffer = ByteBuffer.allocate(lenInB.length + md5.length + buf.length);
                    byte[] result = byteBuffer.put(lenInB).put(md5).put(buf).array();

                    /**
                     * �����ݷ��͸�Client
                     */
                    System.out.println("�����ݷ��͸�Client");
                    out.write(result);
                }
                /**
                 * �ر�����
                 */
                System.out.println("���ݷ������");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static byte[] ByteMD5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            return new byte[0];
        }
    }
}
