package com.skylark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * �����file·���ĸ�Ŀ¼������Ŀ�ĵ�Ŀ¼��SkylarkDemo\
 * css.mp3Ҳ����SkylarkDemo\cssserver.mp3
 * �����ڲ��Ե�ʱ����Ҫ��Ҫ�ϴ����ļ�������SkylarkDemo\Ŀ¼�£�
 * Ȼ���޸������filename
 */
public class SocketClient {

    static String srcFile = "./socket/file/server/cssserver.mp3";//Դ�ļ�
    static String dstFile = "./socket/file/client/cssclient.mp3";//Ŀ���ļ�

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {

            String[] pathSplit = dstFile.trim().split("\\\\");
            String bpFile = pathSplit[pathSplit.length - 1] + ".index";
            int begin = GetBreakPoint(bpFile, 0);

            Socket socket = new Socket("127.0.0.1", 7878);
            System.out.println("client is running");
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            final int bufLen = 8 * 1000 * 1000; // 8M
            byte[] buf = new byte[4 + 16 + bufLen]; // 4λ��Ч���ݳ���(int)��16λmd5ֵ

            for (int piece = begin; ; ++piece) {

                SetBreakPoint(bpFile, piece);

                /**
                 * ��Server����ָ��
                 */
                System.out.println("��Server����ָ��");
                String order = String.format("%s|%d|%d", srcFile, piece, bufLen);
                out.write(order.getBytes());

                /**
                 * ��������
                 */
                System.out.println("��������");
                int sum = 0;
                int complete;
                while ((complete = in.read(buf, sum, 20 + bufLen - sum)) > 0 && sum < 20 + bufLen) {
                    sum += complete;
                }

                /**
                 * �������ݣ��ж��ļ�������У��md5
                 */
                System.out.println("�������ݣ��ж��ļ�������У��md5");
                // ��ȡ��Ч���ݳ���
                byte[] realBufLenByte = new byte[4];
                System.arraycopy(buf, 0, realBufLenByte, 0, 4);
                int realBufLen = ByteBuffer.wrap(realBufLenByte).getInt();
                // �ļ���ͷ��
                if (realBufLen <= 0) {
                    break;
                }
                // ���md5ֵ����ȷ��
                byte[] md5 = new byte[16];
                System.arraycopy(buf, 4, md5, 0, 16);
                if (!CheckMD5(buf, 20, md5)) {
                    // ����������
                    --piece;
                    continue;
                }

                /**
                 * ������д���ļ�
                 */
                System.out.println("������д���ļ�");
                FileOutputStream fw = new FileOutputStream(dstFile, true); // append=true
                fw.write(buf, 20, realBufLen);
                fw.close();
            }
            DelBreakPoint(bpFile);
            /**
             * �ر�����
             */
            System.out.println("�ر�����");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    // �ϵ㲻���ڻ���ִ����򷵻�backup
    private static int GetBreakPoint(String bpFile, int backup) {
        FileReader fr = null;
        try {
            fr = new FileReader(bpFile);
            BufferedReader br = new BufferedReader(fr);
            String numStr = br.readLine();
            return Integer.parseInt(numStr);
        } catch (NumberFormatException | IOException e) {
            return backup;
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void SetBreakPoint(String bpFile, int num) throws FileNotFoundException {
        PrintStream f = new PrintStream(bpFile);
        f.println(num);
        f.close();
    }

    private static void DelBreakPoint(String bpFile) {
        File f = new File(bpFile);
        if (f.exists()) {
            f.delete();
        }
    }

    private static Boolean CheckMD5(byte[] data, int start, byte[] md5) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            if (start == 0) {
                md.update(data);
            } else {
                byte[] part = new byte[data.length - start];
                System.arraycopy(data, start, part, 0, part.length);
                md.update(part);
            }

        } catch (NoSuchAlgorithmException e) {
            return false;
        }
        byte[] result = md.digest();
        for (int i = 0; i < result.length; ++i) {
            if (md5[i] != result[i]) {
                return false;
            }
        }
        return true;
    }
}
