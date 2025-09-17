import java.io.*;
import java.net.*;

public class ChatClientTCP {
    public static void main(String[] args) throws IOException {
        // Conecta-se ao servidor usando o endereço IP e a porta especificados
        // Isso inicia o "handshake" TCP, estabelecendo uma conexão persistente
        Socket socket = new Socket("localhost", 50000);

        // Configura os fluxos de entrada e saída para comunicação com o servidor
        // 'in' lê as mensagens que vêm do servidor
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // 'keyboard' lê a entrada do teclado do usuário
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        // 'out' envia mensagens para o servidor 'true' no construtor do PrintWriter ativa o 'auto-flush',
        // garantindo que as mensagens sejam enviadas imediatamente
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Cria e inicia uma nova thread para lidar com as mensagens recebidas do servidor
        // Isso permite que o cliente envie e receba mensagens simultaneamente
        new Thread(() -> {
            String serverMsg;
            try {
                // Loop que lê continuamente as mensagens do servidor
                // Ele bloqueia a execução até que uma nova mensagem chegue
                while ((serverMsg = in.readLine()) != null) {
                    System.out.println(serverMsg);
                }
            } catch (IOException e) {
                // Captura exceções, como quando o servidor fecha a conexão
                // A exceção 'SocketException' de "Connection reset" é comum aqui
                e.printStackTrace();
            }
        }).start();

        // Loop principal para ler a entrada do usuário e enviar mensagens
        String userInput;
        while ((userInput = keyboard.readLine()) != null) {
            // Verifica se a entrada é um comando
            if (userInput.equalsIgnoreCase("!list")) {
                // Envia o comando para o servidor
                out.println("!list");
            }
            else if (userInput.equalsIgnoreCase("!exit")) {
                // Envia o comando de saída para o servidor
                out.println("!exit");
                // Sai do loop, permitindo que o programa continue para fechar o socket
                break;
            }
            else {
                // Se não for um comando, envia a mensagem de chat para o servidor
                out.println(userInput);
            }
        }

        // Fecha o socket para encerrar a conexão com o servidor
        socket.close();
    }
}
