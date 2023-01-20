import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Client extends JFrame implements ActionListener, KeyListener  {
    @Serial
    private static final long serialVersionUID = -6856148701572311469L;
    private final JTextArea textAreaHistory;
    private final JTextField textFieldMessage;
    private final JTextField textFieldIp;
    private final JTextField textFieldPort;
    private final JTextField textFieldName;
    private final JButton buttonSendMessage;
    private final JButton buttonExitChat;
    private Socket socket;
    private OutputStream socketOutputStream;
    private Writer ouw;
    private BufferedWriter bfw;
    private final FileOutputStream fos;


    public Client() throws IOException {
        //
        // Message Dialog to Collect Server Information
        //
        JLabel labelQuestion = new JLabel("Which chat do you want to connect?");

        JLabel labelIp = new JLabel("Chat IP:");
        textFieldIp = new JTextField("127.0.0.1");

        JLabel labelPort = new JLabel("Chat port:");
        textFieldPort = new JTextField("12345");

        JLabel labelName = new JLabel("Your name:");
        textFieldName = new JTextField("");


        Object[] content = {
                labelQuestion,
                labelIp,
                textFieldIp,
                labelPort,
                textFieldPort,
                labelName,
                textFieldName
        };

        JOptionPane.showMessageDialog(null, content, "Connect to chat", JOptionPane.INFORMATION_MESSAGE);

        //
        // Build chat UI
        //
        JLabel labelHistory = new JLabel("Conversation");
        textAreaHistory = new JTextArea(20,50);
        textAreaHistory.setBackground(new Color(230, 230, 230));
        textAreaHistory.setEditable(false);
        textAreaHistory.setLineWrap(true);

        JScrollPane scrollPaneHistory = new JScrollPane(textAreaHistory);

        JLabel labelMessage = new JLabel("Message");
        textFieldMessage = new JTextField(50);
        textFieldMessage.addKeyListener(this);

        buttonSendMessage = new JButton("Send");
        buttonSendMessage.setToolTipText("Send Message");
        buttonSendMessage.addActionListener(this);

        buttonExitChat = new JButton("Close Chat");
        buttonExitChat.setToolTipText("Exit From Chat");
        buttonExitChat.addActionListener(this);

        JPanel panelContent = new JPanel();
        panelContent.add(labelHistory);
        panelContent.add(scrollPaneHistory);
        panelContent.add(labelMessage);
        panelContent.add(textFieldMessage);
        panelContent.add(buttonExitChat);
        panelContent.add(buttonSendMessage);

        setTitle(textFieldName.getText());
        setContentPane(panelContent);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(640,480);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //
        // Create client file
        //
        fos = new FileOutputStream(textFieldPort.getText() + "-" + textFieldName.getText() + ".txt");
    }

    public void connect() throws IOException {
        socket = new Socket(textFieldIp.getText(), Integer.parseInt(textFieldPort.getText()));

        socketOutputStream = socket.getOutputStream();
        ouw = new OutputStreamWriter(socketOutputStream);
        bfw = new BufferedWriter(ouw);

        bfw.write(textFieldName.getText() + "\r\n");
        bfw.flush();
    }

    public void sendMessage(String msg) throws IOException {
        if (msg != null) {
            bfw.write(msg + "\r\n");
            bfw.flush();
        }
        textFieldMessage.setText("");
    }

    public void listen() throws IOException {
        InputStream in = socket.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader bfr = new BufferedReader(inr);
        String message = bfr.readLine();

        while (! "End".equalsIgnoreCase(message)) {
            message = message + "\r\n";
            fos.write(message.getBytes());
            textAreaHistory.append(message);
            message = bfr.readLine();
        }

        boolean running = true;
        while (running) {
            if (bfr.ready()) {
                message = bfr.readLine();

                if (message.contains(textFieldName.getText()) && message.contains("[disconnected]")) {
                    running = false;
                }
                message = message + "\r\n";
                fos.write((message).getBytes());
                textAreaHistory.append(message);
            }
        }
    }

    public void exit() throws IOException {
        sendMessage("Disconnected");

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        bfw.close();
        ouw.close();
        socketOutputStream.close();
        socket.close();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getActionCommand().equals(buttonSendMessage.getActionCommand())) {
                sendMessage(textFieldMessage.getText());
            } else if (e.getActionCommand().equals(buttonExitChat.getActionCommand())) {
                exit();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER){
            try {
                sendMessage(textFieldMessage.getText());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub
    }

    public static void main(String []args) throws IOException {
        Client app = new Client();

        app.connect();
        app.listen();
    }
}
