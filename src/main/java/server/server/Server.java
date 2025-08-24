package server.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    static GameServer game;
    public static void main(String[] args) throws Exception{
        game = new GameServer();
        System.out.println(1);
        game.init();

        new Thread(() -> {
            while(true) {
                System.out.println(2);
                game.tick();
                try { Thread.sleep(16); } catch (Exception e ) {}
            }
        }).start();

        ServerSocket serverSocket = new ServerSocket(25565);
        System.out.println("Server running on port 25565");

        while(true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected!");

            new Thread(() -> handleClient(clientSocket)).start();
        }
    }
    private static void handleClient(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("Hello from server!");

            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("Client: " + msg);
                out.println("Echo: " + msg);
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
