import java.net.*;
import java.util.*;

public class ChatServerUDP {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;
    private static Map<String, InetSocketAddress> clients = new HashMap<>();

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(PORT);
        byte[] buffer = new byte[BUFFER_SIZE];
        System.out.println("Servidor UDP escutando na porta " + PORT);

        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String received = new String(packet.getData(), 0, packet.getLength());
            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            // Registro de usuário
            if (received.startsWith("REGISTRO:")) {
                String username = received.substring(9).trim();
                clients.put(username, new InetSocketAddress(address, port));
                System.out.println("Usuário registrado: " + username + " [" + address + ":" + port + "]");
                System.out.println("Clientes conectados: " + clients.keySet());
                continue;
            }


            // Broadcast para todos
            if (received.startsWith("BROADCAST:")) {
                System.err.println(received);
                String[] partes = received.split(":", 3);
                String from = partes[1];
                String mensagem = partes[2];
                String msgFinal = "[Todos] " + from + ": " + mensagem;

                for (InetSocketAddress dest : clients.values()) {
                    byte[] data = msgFinal.getBytes();
                    socket.send(new DatagramPacket(data, data.length, dest));
                }
                continue;
            }
            
            System.out.println("Clientes conectados: " + clients.keySet());

            // Mensagem privada
            if (received.startsWith("PRIVADO:")) {
                try {
                    System.err.println(received);

                    String[] partes = received.split(":", 4);

                    if (partes.length < 4) {
                        System.err.println("Formato inválido de mensagem privada: " + received);
                        return;
                    }

                    String from = partes[1];
                    String to = partes[2];
                    String mensagem = partes[3];

                    if (!clients.containsKey(to)) {
                        System.err.println("Usuario " + to + " nao encontrado para mensagem privada.");
                        return;
                    }

                    InetSocketAddress dest = clients.get(to);
                    if (dest == null) {
                        System.err.println("Usuario " + to + " nao encontrado para mensagem privada.");
                        return;
                    }

                    String msgFinal = "[Privado] " + from + ": " + mensagem;
                    byte[] data = msgFinal.getBytes();
                    socket.send(new DatagramPacket(data, data.length, dest));

                } catch (Exception e) {
                    System.err.println("Erro ao processar mensagem privada: " + e);
                    e.printStackTrace();
                }
            }

        }
    }
}
