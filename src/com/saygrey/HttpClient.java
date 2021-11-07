package Main;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;

/**
 * class for http client
 */
public class HttpClient {
    /**
     * main procedure
     * @param args
     */
    public static void main(String[] args) {

        try {
            String header = null;
            if (args.length == 0) {
                //Создание потока и чтение данных
                header = readHeader(System.in);
            } else {
                //Если в метод main переданы аргументы
                FileInputStream fis = new FileInputStream(args[0]);
                header = readHeader(fis);
                fis.close();
            }
            //Вывод заголовка в консоль
            System.out.println("Заголовок: \n" + header);
            //
            String answer = sendRequest(header);
            /* Ответ выводится на консоль */
            System.out.println("Ответ от сервера: \n");
            System.out.write(answer.getBytes());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.getCause().printStackTrace();
        }
    }

    /**
     *
     * @param strm
     * @return
     * @throws IOException
     */
    public static String readHeader(InputStream strm) throws IOException {

        byte[] buff = new byte[64 * 1024];
        int length = strm.read(buff);
        String res = new String(buff, 0, length);
        return res;
    }

    /**
     *
     * @param httpHeader
     * @return
     * @throws Exception
     */
    public static String sendRequest(String httpHeader) throws Exception {


        String host = null;
        int port = 0;
        try {
            //Получение хоста и порта
            host = getHost(httpHeader);
            port = getPort(host);
            host = getHostWithoutPort(host);
        } catch (Exception e) {
            throw new Exception("Не удалось получить адрес сервера.", e);
        }
        //Создание сокета и ответ из него
        Socket socket = null;
        try {
            socket = new Socket(host,port);
            System.out.println("Создан сокет: " + host + " port:" + port);
            OutputStream out = socket.getOutputStream();
            out.write(httpHeader.getBytes());
            out.flush();
            System.out.println("Заголовок отправлен. \n");
            //Возращение и вызов метода getAnswer
            return getAnswer(socket);
        } catch (Exception e) {
            throw new Exception("Ошибка при отправке запроса: "
                    + e.getMessage(), e);
        } finally {

            socket.close();
        }
    }


    /**
     *
     * @param socket
     * @return
     * @throws Exception
     */
    private static String getAnswer(Socket socket) throws Exception {
        String res = null;
        try {

            InputStreamReader isr = new InputStreamReader(socket
                    .getInputStream());
            BufferedReader bfr = new BufferedReader(isr);
            StringBuffer sbf = new StringBuffer();

            int ch = bfr.read();
            while (ch != -1) {
                sbf.append((char) ch);
                ch = bfr.read();
            }

            res = sbf.toString();
        } catch (Exception e) {
            throw new Exception("Ошибка при чтении ответа от сервера.", e);
        }
        return res;
    }

    /**
     *
     * @param header
     * @return
     * @throws ParseException
     */
    private static String getHost(String header) throws ParseException {
        final String host = "Host: ";
        final String normalEnd = "\n";
        final String msEnd = "\r\n";

        int s = header.indexOf(host, 0);

        if (s < 0) {
            return "localhost";
        }

        s += host.length();
        int e = header.indexOf(normalEnd, s);
        e = (e > 0) ? e : header.indexOf(msEnd, s);

        if (e < 0) {
            throw new ParseException(
                    "В заголовке запроса не найдено закрывающих символов после пункта Host.",
                    0);
        }

        String res = header.substring(s, e).trim();

        return res;
    }

    /**
     *
     * @param hostWithPort
     * @return
     */
    private static int getPort(String hostWithPort) {

        int port = hostWithPort.indexOf(":", 0);

        port = (port < 0) ? 80 : Integer.parseInt(hostWithPort
                .substring(port + 1));

        return port;
    }

    /**
     *
     * @param hostWithPort
     * @return
     */
    private static String getHostWithoutPort(String hostWithPort) {

        int portPosition = hostWithPort.indexOf(":", 0);
        if (portPosition < 0) {
            return hostWithPort;
        } else {
            return hostWithPort.substring(0, portPosition);
        }
    }
}