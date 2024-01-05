package Project.ChatServer;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_IP, PORT);

            // 获取用户输入的用户名
            System.out.print("Enter your username: ");
            Scanner scanner = new Scanner(System.in);
            String username = scanner.nextLine();

            // 发送用户名到服务器
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(username);

            // 启动读取服务器消息的线程
            Thread readerThread = new Thread(new ReaderThread(socket));
            readerThread.start();

            // 发送消息给服务器
            String message;
            while (true) {
                message = scanner.nextLine();
                writer.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ReaderThread implements Runnable {
        private Socket socket;

        ReaderThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("Received: " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
