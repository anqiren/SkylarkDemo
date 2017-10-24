package cn.itcast.net.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.itcast.utils.StreamTool;

public class SocketDownloadServer {

    public static void main(String[] args) {
        try {
            SocketDownloadServer socketUploadServer = new SocketDownloadServer(7879);
            socketUploadServer.start();
            System.out.println("server is running");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ExecutorService executorService;//�̳߳�
    private int port;//�����˿�
    private boolean quit = false;//�˳�
    private ServerSocket server;

    public SocketDownloadServer(int port) {
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
                System.out.println("accepted connection " + socket.getInetAddress() + ":" + socket.getPort());
                OutputStream outStream = socket.getOutputStream();
                String filename = "css.mp3";
                File file = new File(filename);
                String head = "Content-Length=" + file.length() + ";filename=" + filename + ";sourceid=\r\n";
//            String head = "Content-Length=" + file.length() + ";filename=" + filename + ";sourceid=1508806329691\r\n";
                outStream.write(head.getBytes());

                PushbackInputStream inStream = new PushbackInputStream(socket.getInputStream());
                String response = StreamTool.readLine(inStream);
                System.out.println("client say : " + response);
                String[] items = response.split(";");
                String position = items[1].substring(items[1].indexOf("=") + 1);

                RandomAccessFile fileInputStream = new RandomAccessFile(file, "r");
                fileInputStream.seek(Integer.valueOf(position));
                byte[] buffer = new byte[1024*8];
                int len = -1;
                int count = 0;
                while ((len = fileInputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                    count += 1;
                    System.out.println("len : " + len + "--->count : " + count);

                /*if (count == 30) {
                    System.out.println("client pause start");
                    Thread.sleep(10000);
                    System.out.println("client pause end");
                }*/

                }
                System.out.println("server send finish");
                fileInputStream.close();
                outStream.close();
                inStream.close();
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

}
