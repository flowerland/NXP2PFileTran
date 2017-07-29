import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Server {

	private JFrame frame;
	private JLabel lblNewLabel_1 = new JLabel("numberOfThreadPool");
	private JLabel lblNewLabel = new JLabel("Port");
	JTextField textField_port = new JTextField();
	JTextField textField_numberOfThreadPool = new JTextField();
	JTextArea textArea = new JTextArea();
	JButton btnNewButton;
	Napd np;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Server window = new Server();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}		
		});
//		try {
//			Server window = new Server();
//			window.frame.setVisible(true);
//			window.np = new Napd(window);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * Create the application.
	 */
	public Server() {
		initialize();
		this.np = new Napd(this);
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
		//frame.setVisible(true);
		
		
		lblNewLabel.setBackground(Color.RED);
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(0, 13, 68, 23);
		frame.getContentPane().add(lblNewLabel);
		
		//textField_port = new JTextField();
		textField_port.setHorizontalAlignment(SwingConstants.CENTER);
		textField_port.setText("7777");
		textField_port.setBounds(88, 16, 66, 21);
		frame.getContentPane().add(textField_port);
		textField_port.setColumns(10);
		
		//lblNewLabel_1 = new JLabel("numberOfThreadPool");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(217, 10, 125, 29);
		frame.getContentPane().add(lblNewLabel_1);
		
		//textField_numberOfThreadPool = new JTextField();
		textField_numberOfThreadPool.setHorizontalAlignment(SwingConstants.CENTER);
		textField_numberOfThreadPool.setText("64");
		textField_numberOfThreadPool.setBounds(373, 14, 66, 21);
		frame.getContentPane().add(textField_numberOfThreadPool);
		textField_numberOfThreadPool.setColumns(10);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(1, 44, 590, 427);
		frame.getContentPane().add(scrollPane);
		
		//textArea = new JTextArea();
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
			new Thread(np).start();
			btnNewButton.setEnabled(false);
		}
	}
	
	
	
	
}
