import java.io.*;
import java.net.*;

public class ChatClientTCP {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 50000);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Thread para ler mensagens do servidor
        new Thread(() -> {
            String serverMsg;
            try {
                while ((serverMsg = in.readLine()) != null) {
                    System.out.println(serverMsg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Loop para enviar mensagens
        String userInput;
        while ((userInput = keyboard.readLine()) != null) {
            if (userInput.equalsIgnoreCase("!list")) {
                out.println("!list");
            }
            else if (userInput.equalsIgnoreCase("!exit")) {
                out.println("!exit");
                break;
            }
            else {
                out.println(userInput);
            }
        }

        socket.close();
    }
}
