package NXP2P_FileTran;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ServerFrame {

    JFrame frame;
    private JLabel lblNewLabel_1 = new JLabel("numberOfThreadPool");
    private JLabel lblNewLabel = new JLabel("Port");
    JTextField textField_port = new JTextField();
    JTextField textField_numberOfThreadPool = new JTextField();
    JTextArea textArea = new JTextArea();
    JButton btnNewButton;
    /**
     * Launch the application.
     */
    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ServerFrame window = new ServerFrame();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public ServerFrame() throws IOException{
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setTitle("\u670D\u52A1\u5668");
        frame.setResizable(false);
        frame.setBounds(100, 100, 600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        lblNewLabel.setBackground(Color.RED);
        lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel.setBounds(0, 13, 68, 23);
        frame.getContentPane().add(lblNewLabel);

        textField_port.setHorizontalAlignment(SwingConstants.CENTER);
        textField_port.setText("8000");
        textField_port.setBounds(88, 16, 66, 21);
        frame.getContentPane().add(textField_port);
        textField_port.setColumns(10);

        lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel_1.setBounds(217, 10, 125, 29);
        frame.getContentPane().add(lblNewLabel_1);

        //textField_numberOfThreadPool = new JTextField();
        textField_numberOfThreadPool.setHorizontalAlignment(SwingConstants.CENTER);
        textField_numberOfThreadPool.setText("");
        textField_numberOfThreadPool.setBounds(373, 14, 66, 21);
        frame.getContentPane().add(textField_numberOfThreadPool);
        textField_numberOfThreadPool.setColumns(10);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(1, 44, 590, 427);
        frame.getContentPane().add(scrollPane);

        scrollPane.setViewportView(textArea);

        btnNewButton = new JButton("start server");
        btnNewButton.addActionListener(new ButtonAction());
        btnNewButton.setToolTipText("server start run");
        btnNewButton.setHorizontalAlignment(SwingConstants.LEFT);
        btnNewButton.setBounds(468, 5, 105, 39);
        frame.getContentPane().add(btnNewButton);
    }

    class ButtonAction implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            btnNewButton.setEnabled(false);
        }
    }

}
