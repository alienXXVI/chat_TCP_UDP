import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServerTCP {
    // A porta que o servidor irá escutar
    private static final int PORT = 50000;
    // Um mapa que armazena os clientes conectados, associando o nome de usuário ao seu Socket
    private static Map<String, Socket> clients = new HashMap<>();

    public static void main(String[] args) throws IOException {
        // Cria o ServerSocket, que aguarda conexões de clientes na porta especificada
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor TCP escutando na porta " + PORT);

        // Loop infinito para aceitar novas conexões de clientes
        while (true) {
            // 'accept()' bloqueia a execução até que um cliente se conecte
            // Quando um cliente se conecta, um novo Socket é retornado
            Socket socket = serverSocket.accept();

            // Para cada nova conexão, uma nova thread é iniciada para lidar com o cliente
            new Thread(new ClientHandler(socket)).start();
        }
    }

    // Lida com a comunicação de um único cliente
    static class ClientHandler implements Runnable {
        private Socket socket;
        private String username;

        // Construtor que recebe o Socket do cliente
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                // Configura os fluxos de entrada (para ler) e saída (para escrever)
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Solicita o nome de usuário e o lê do cliente
                out.println("Digite seu nome de usuario: ");
                username = in.readLine();

                // Adiciona o novo cliente ao mapa de clientes de forma segura
                synchronized (clients) {
                    clients.put(username, socket);
                }

                // Notifica todos os outros clientes que um novo usuário entrou
                broadcast(username + " entrou no chat.");

                String message;
                // Loop para ler mensagens do cliente até que a conexão seja fechada
                while ((message = in.readLine()) != null) {
                    // Verifica se a mensagem é um comando ou uma mensagem de chat
                    if (message.equalsIgnoreCase("!list")) {
                        sendUserList(out);
                    }
                    else if (message.equalsIgnoreCase("!exit")) {
                        // Encerra o loop do cliente. O código abaixo do loop será executado para fechar a conexão
                        break;
                    }
                    else if (message.startsWith("@")) {
                        // Lida com mensagens privadas
                        String[] parts = message.split(" ", 2);
                        String targetUser = parts[0].substring(1);
                        String privateMsg = parts[1];

                        sendToUser(targetUser, "[Privado] " + username + ": " + privateMsg);
                    } else {
                        // Lida com mensagens de broadcast para todos
                        broadcast("[Todos] " + username + ": " + message);
                    }
                }
                
                // Fecha o socket e limpa os recursos
                socket.close();
                // Remove o cliente do mapa após a desconexão
                synchronized (clients) {
                    clients.remove(username);
                }
                // Notifica a saída do usuário
                broadcast(username + " saiu do chat.");

            } catch (IOException e) {
                // Trata exceções, como um cliente se desconectando inesperadamente
                e.printStackTrace();
            }
        }

        // Envia uma mensagem para todos os clientes conectados
        private void broadcast(String message) throws IOException {
            synchronized (clients) {
                for (Socket s : clients.values()) {
                    // Cria um novo PrintWriter para cada socket para garantir que a mensagem seja enviada
                    new PrintWriter(s.getOutputStream(), true).println(message);
                }
            }
            System.out.println(message);
        }

        // Envia uma mensagem para um usuário específico
        private void sendToUser(String user, String message) throws IOException {
            synchronized (clients) {
                Socket s = clients.get(user);
                if (s != null) {
                    new PrintWriter(s.getOutputStream(), true).println(message);
                }
            }
            System.out.println(message);
        }

        // Constrói e envia a lista de usuários conectados para o cliente que a solicitou
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
