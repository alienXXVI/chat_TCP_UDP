import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServerTCP {
    private static final int PORT = 50000;
    private static Map<String, Socket> clients = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor TCP escutando na porta " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new ClientHandler(socket)).start();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Digite seu nome de usuario: ");
                username = in.readLine();
                synchronized (clients) {
                    clients.put(username, socket);
                }

                broadcast(username + " entrou no chat.");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("!list")) {
                        sendUserList(out);
                    }
                    else if (message.equalsIgnoreCase("!exit")) {
                        break;
                    }
                    else if (message.startsWith("@")) {
                        String[] parts = message.split(" ", 2);
                        String targetUser = parts[0].substring(1);
                        String privateMsg = parts[1];

                        sendToUser(targetUser, "[Privado] " + username + ": " + privateMsg);
                    } else {
                        broadcast("[Todos] " + username + ": " + message);
                    }
                }

                socket.close();
                synchronized (clients) {
                    clients.remove(username);
                }
                broadcast(username + " saiu do chat.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcast(String message) throws IOException {
            synchronized (clients) {
                for (Socket s : clients.values()) {
                    new PrintWriter(s.getOutputStream(), true).println(message);
                }
            }
            System.out.println(message);
        }

        private void sendToUser(String user, String message) throws IOException {
            synchronized (clients) {
                Socket s = clients.get(user);
                if (s != null) {
                    new PrintWriter(s.getOutputStream(), true).println(message);
                }
            }
            System.out.println(message);
        }

        private void sendUserList(PrintWriter out) {
            synchronized (clients) {
                StringBuilder userList = new StringBuilder("Usuários conectados:\n");
                for (String user : clients.keySet()) {
                    userList.append("- ").append(user).append("\n");
                }
                out.println(userList.toString());
            }
        }
    }
}
