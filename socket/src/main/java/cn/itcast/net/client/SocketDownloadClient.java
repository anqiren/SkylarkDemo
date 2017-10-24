package cn.itcast.net.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import cn.itcast.utils.StreamTool;

public class SocketDownloadClient {

    public static void main(String[] args) {
        try {
            SocketDownloadClient socketDownloadClient = new SocketDownloadClient("127.0.0.1",7879);
            socketDownloadClient.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<Long, FileLog> datas = new HashMap<Long, FileLog>();//��Ŷϵ�����
    private String ipAddress;
    private int port;
    private Socket socket;

    public SocketDownloadClient(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;

    }

    public void start() throws IOException {
        socket = new Socket(ipAddress,port);
        new Thread(new SocketTask(socket)).start();

    }

    private final class SocketTask implements Runnable{

        private Socket socket = null;

        public SocketTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                PushbackInputStream inStream = new PushbackInputStream(socket.getInputStream());
                //�õ�����˷����ĵ�һ��Э�����ݣ�Content-Length=143253434;filename=xxx.3gp;sourceid=
                //����û����������ļ���sourceid��ֵΪ�ա�
                String head = StreamTool.readLine(inStream);
                System.out.println("server say "+head);
                if (head != null) {
                    //�����Э����������ȡ�������ֵ
                    String[] items = head.split(";");
                    String filelength = items[0].substring(items[0].indexOf("=") + 1);
                    String filename = items[1].substring(items[1].indexOf("=") + 1);
                    String sourceid = items[2].substring(items[2].indexOf("=") + 1);
                    long id = System.currentTimeMillis();//������Դid�������ҪΨһ�ԣ����Բ���UUID
                    System.out.println("client����������Դid��" + id);
                    File file = null;
                    int position = 0;

                    File logFile = new File("./socket/file/client",filename + ".log");
                    if (logFile.exists()){
                        file = new File("./socket/file/client",filename);
                        Properties properties = new Properties();
                        properties.load(new FileInputStream(logFile));
                        position = Integer.valueOf(properties.getProperty("length"));//��ȡ�Ѿ����ص����ݳ���
                    }else {
                        position = 0;
                        File dir = new File("./socket/file/client");
                        if (!dir.exists()) dir.mkdirs();
                        file = new File(dir, filename);// ���������ļ�
                        if (file.exists()) {//������ص��ļ�����������Ȼ����и���
                            filename = filename.substring(0, filename.indexOf(".") - 1) + dir.listFiles().length + filename.substring(filename.indexOf("."));
                            file = new File(dir, filename);
                        }
                        save(id, file);
                    }

                    OutputStream outStream = socket.getOutputStream();
                    String response = "sourceid=" + id + ";position=" + position + "\r\n";
//                    String response = "sourceid=" + "1508811547833" + ";position=" + 517120 + "\r\n";
                    //�ͻ����յ���������������Ϣ�󣬸�������������Ӧ��Ϣ��sourceid=1274773833264;position=0
                    //sourceid�ɿͻ������ɣ�Ψһ��ʶ���ص��ļ���positionָʾ����˴��ļ���ʲôλ�ÿ�ʼ�ϴ�
                    outStream.write(response.getBytes());

                    RandomAccessFile fileOutStream = new RandomAccessFile(file, "rwd");
                    if (position == 0) fileOutStream.setLength(Integer.valueOf(filelength));//�����ļ�����
                    fileOutStream.seek(position);//ָ�����ļ����ض�λ�ÿ�ʼд������
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    int length = position;
                    int count = 0;
                    while ((len = inStream.read(buffer)) != -1) {//���������ж�ȡ����д�뵽�ļ���
                        fileOutStream.write(buffer, 0, len);
                        length += len;
                        count += 1;
                        Properties properties = new Properties();
                        properties.put("length", String.valueOf(length));
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(file.getParentFile(), file.getName() + ".log"));
                        properties.store(fileOutputStream, null);//ʵʱ��¼�Ѿ����յ��ļ�����
                        fileOutputStream.close();

                        System.out.println("len : " + len + "--->count : " + count);
                    }
                    System.out.println("client receive finish");
                    if (length == fileOutStream.length()) delete(id);
                    fileOutStream.close();
                    inStream.close();
                    outStream.close();
                    file = null;

                }
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


    public FileLog find(Long sourceid) {
        return datas.get(sourceid);
    }

    //�������ؼ�¼
    public void save(Long id, File saveFile) {
        //�պ���Ըĳ�ͨ�����ݿ���
        datas.put(id, new FileLog(id, saveFile.getAbsolutePath()));
    }

    //���ļ�������ϣ�ɾ����¼
    public void delete(long sourceid) {
        if (datas.containsKey(sourceid)) datas.remove(sourceid);
    }

    private class FileLog {
        private Long id;
        private String path;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public FileLog(Long id, String path) {
            this.id = id;
            this.path = path;
        }
    }
}
