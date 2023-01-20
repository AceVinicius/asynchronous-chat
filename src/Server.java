import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private static ArrayList<String> messages;
    private static ArrayList<BufferedWriter>clients;
    private final Socket con;
    private String name;
    private BufferedReader bfr;
    private final FileOutputStream fos;

    public Server(Socket con, FileOutputStream file) {
        this.con = con;
        this.fos = file;

        try {
            InputStream in = con.getInputStream();
            InputStreamReader inr = new InputStreamReader(in);
            bfr = new BufferedReader(inr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String message;
            OutputStream ou = this.con.getOutputStream();
            Writer ouw = new OutputStreamWriter(ou);
            BufferedWriter bfw = new BufferedWriter(ouw);
            clients.add(bfw);
            name = message = bfr.readLine();

            for (String s : messages) {
                bfw.write(s + "\n");
            }
            bfw.write("End\r\n");

            message = name + " : [connected] : " + Instant.now().toString();
            fos.write((message + "\r\n").getBytes());
            messages.add(message);
            sendToAll(message);

            boolean running = true;
            while (running) {
                message = bfr.readLine();

                if (message == null) {
                    continue;
                } else if (message.equals("Disconnected")) {
                    message = name + " : [disconnected] : " + Instant.now().toString();
                    running = false;
                } else {
                    message = name + " : " + message + " : " + Instant.now().toString();
                }

                fos.write((message + "\r\n").getBytes());
                messages.add(message);
                sendToAll(message);
            }

            System.out.println("closed " + name + " connection [loop]");

            clients.remove(bfw);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToAll(String message) throws IOException {

        BufferedWriter bw_current = null;
        try {
            for (BufferedWriter bw : clients) {
                bw_current = bw;

                bw.write(message + "\r\n");
                bw.flush();
            }
        } catch (Exception e) {
            System.out.println("closed " + name + " connection [broadcast]");
            clients.remove(bw_current);
            bw_current.close();
        }
    }

    public static void main(String []args) {
        try {
            //
            // Message Dialog get Server Port
            //
            JLabel labelPort = new JLabel("Server port:");
            JTextField textFieldPort = new JTextField("12345");

            Object[] content = {
                    labelPort,
                    textFieldPort
            };

            JOptionPane.showMessageDialog(null, content, "Create chat", JOptionPane.INFORMATION_MESSAGE);

            //
            // Create server data
            //
            ServerSocket server = new ServerSocket(Integer.parseInt(textFieldPort.getText()));
            clients = new ArrayList<BufferedWriter>();
            messages = new ArrayList<String>();
            FileOutputStream file = new FileOutputStream(textFieldPort.getText() + "-server.txt");

            JOptionPane.showMessageDialog(null, "Server is active on port: " + textFieldPort.getText(), "Server", JOptionPane.INFORMATION_MESSAGE);

            while (true) {
                System.out.println("Waiting connection...");

                Socket con = server.accept();

                System.out.println("Client connected...");

                Thread t = new Server(con, file);
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
