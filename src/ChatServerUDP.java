import java.net.*;
import java.util.*;

public class ChatServerUDP {
    private static final int PORT = 50001;
    private static final int BUFFER_SIZE = 1024;
    private static Map<String, InetSocketAddress> clients = new HashMap<>();

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(PORT);
        byte[] buffer = new byte[BUFFER_SIZE];
        System.out.println("Servidor UDP pronto na porta " + PORT);

        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String received = new String(packet.getData(), 0, packet.getLength());
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            
            if (received.startsWith("LISTAR_USUARIOS:")) {
                String userList = "Usuários registrados:\n";
                for (String user : clients.keySet()) {
                    userList += "- " + user + "\n";
                }
                byte[] data = userList.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(data, data.length, address, port);
                socket.send(responsePacket);
                continue;
            }

            if (received.startsWith("SAIR:")) {
                String username = received.substring(5).trim();
                clients.remove(username);

                String leaveMsg = username + " saiu do chat.";
                broadcast(socket, leaveMsg);
                continue;
            }

            // Registro de usuário
            if (received.startsWith("REGISTRO:")) {
                String username = received.substring(9).trim();
                clients.put(username, new InetSocketAddress(address, port));

                String joinMsg = username + " entrou no chat.";
                broadcast(socket, joinMsg);

                continue;
            }

            // Broadcast para todos
            if (received.startsWith("BROADCAST:")) {
                String[] partes = received.split(":", 3);
                String from = partes[1];
                String mensagem = partes[2];
                String msgFinal = "[Todos] " + from + ": " + mensagem;

                broadcast(socket, msgFinal);
                continue;
            }

            // Mensagem privada
            if (received.startsWith("PRIVADO:")) {
                try {
                    String[] partes = received.split(":", 4);
                    if (partes.length < 4) {
                        System.err.println("Formato inválido de mensagem privada: " + received);
                        continue;
                    }

                    String from = partes[1];
                    String to = partes[2];
                    String mensagem = partes[3];

                    InetSocketAddress dest = clients.get(to);
                    if (dest != null) {
                        String msgFinal = "[Privado] " + from + ": " + mensagem;
                        byte[] data = msgFinal.getBytes();
                        socket.send(new DatagramPacket(data, data.length, dest));
                        System.out.println(msgFinal);
                    } else {
                        System.out.println("Usuário " + to + " não encontrado para mensagem privada.");
                    }

                } catch (Exception e) {
                    System.err.println("Erro ao processar mensagem privada: " + e);
                    e.printStackTrace();
                }
            }
        }
    }

    private static void broadcast(DatagramSocket socket, String message) throws Exception {
        for (InetSocketAddress dest : clients.values()) {
            byte[] data = message.getBytes();
            socket.send(new DatagramPacket(data, data.length, dest));
        }
        System.out.println(message);
    }
}
