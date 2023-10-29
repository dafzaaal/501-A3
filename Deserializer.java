import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.input.SAXBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Deserializer {

    private final static int PORT = 8000;

    public void listeningForConnections() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                    
                    StringBuilder xmlBuilder = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        xmlBuilder.append(line);
                    }

                    String receivedXml = xmlBuilder.toString();
                    System.out.println("Received XML:\n" + receivedXml);

                    // If needed, you can deserialize it here
                      deserialize(receivedXml);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object deserialize(org.jdom.Document document) {

    }

    public static void main(String[] args) {
        Deserializer deserializer = new Deserializer();
        deserializer.listeningForConnections();
    }
}

