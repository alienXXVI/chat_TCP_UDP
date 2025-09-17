import java.net.*;
import java.util.Scanner;

public class ChatClientUDP {
    private static final int SERVER_PORT = 50001;
    private static final String SERVER_IP = "localhost";
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        DatagramSocket socket = new DatagramSocket();

        System.out.print("Digite seu nome de usuario: ");
        String username = scanner.nextLine();

        // Registrar no servidor
        sendMessage(socket, "REGISTRO:" + username);
        Thread.sleep(200);  // Espera o servidor processar

        // Thread para receber mensagens
        new Thread(() -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (!socket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    System.out.println(msg);
                } catch (SocketException e) {
                    // Ignora a exceção de "Socket closed" que acontece
                    // quando o socket é fechado pela thread principal.
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Loop de envio de mensagens
        while (true) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("!list")) {
                sendMessage(socket, "LISTAR_USUARIOS:");
            }
            else if (input.equalsIgnoreCase("!exit")) {
                sendMessage(socket, "SAIR:" + username);
                Thread.sleep(200);
                socket.close();
                break;
            }
            else if (input.startsWith("@")) {
                // Mensagem privada
                int space = input.indexOf(" ");
                if (space == -1) {
                    System.out.println("Formato inválido. Use: @usuario mensagem");
                    continue;
                }
                String to = input.substring(1, space);
                String msg = input.substring(space + 1);
                sendMessage(socket, "PRIVADO:" + username + ":" + to + ":" + msg);
            } else {
                // Broadcast
                sendMessage(socket, "BROADCAST:" + username + ":" + input);
            }
        }
    }

    private static void sendMessage(DatagramSocket socket, String msg) throws Exception {
        byte[] data = msg.getBytes();
        InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, SERVER_PORT);
        socket.send(packet);
    }
}
