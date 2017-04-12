package Ŭ���̾�Ʈ;

import java.awt.*;
import java.awt.event.*;
import java.io.*; 
import java.net.*;

import javax.swing.*;

public class Client extends JFrame {
	
	// Ŭ���̾�Ʈ �α��� ������Ʈ
	private JFrame Login = new JFrame();
	private JPanel loginPane;
	private JTextField name_tf;
	private JTextField ip_tf;
	private JTextField port_tf;
	private JButton login_btn = new JButton("Login");
	
	// Ŭ���̾�Ʈ ä�ù� ������Ʈ
	private JPanel chattingPane;
	private JTextField input_tf; 							
	private JButton send_btn = new JButton("����"); 				
	private JTextArea chat_ta = new JTextArea(); 			
	private JLabel nLabel;
	private Socket clientSocket; 										// Ŭ���̾�Ʈ ����
	
	private DataInputStream in;
	private DataOutputStream out;

	private String ip; 													// IP�� �����ϴ� ���ڿ�
	private String name; 												// �̸��� �����ϴ� ���ڿ�
	private int port; 													// ��Ʈ��ȣ
	
	
	Client() {
		
		// �α��� ������
		Login.setTitle("�α��� â");
		Login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Login.setBounds(100, 100, 310, 450);
		loginPane = new JPanel();
		Login.setContentPane(loginPane);
		loginPane.setLayout(null);

		ImageIcon pic = new ImageIcon("images/picpic.jpg");
		JLabel imageLabel = new JLabel(pic);
		imageLabel.setBounds(35, 21, 227, 144);
		loginPane.add(imageLabel);

		JLabel IdLabel = new JLabel("��  ��");
		IdLabel.setBounds(35, 185, 57, 15);
		loginPane.add(IdLabel);

		JLabel IpLabel = new JLabel("�� �� IP");
		IpLabel.setBounds(35, 235, 57, 15);
		loginPane.add(IpLabel);

		JLabel PortLabel = new JLabel("�� Ʈ �� ȣ");
		PortLabel.setBounds(35, 285, 71, 15);
		loginPane.add(PortLabel);

		name_tf = new JTextField();
		name_tf.setBounds(129, 185, 133, 21);
		loginPane.add(name_tf);
		name_tf.setColumns(10);

		ip_tf = new JTextField();
		ip_tf.setBounds(129, 235, 133, 21);
		loginPane.add(ip_tf);
		ip_tf.setColumns(10);

		port_tf = new JTextField();
		port_tf.setBounds(129, 285, 133, 21);
		loginPane.add(port_tf);
		port_tf.setColumns(10);

		login_btn.setBounds(35, 345, 227, 44);
		loginPane.add(login_btn);
		Login.setVisible(true); // Ȱ��ȭ
		login_btn.addActionListener(new MyActionListener()); 		

		// ä�ù� ������
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 520, 400);
		chattingPane = new JPanel();
		setContentPane(chattingPane);
		chattingPane.setLayout(null);

		chat_ta.setBounds(13, 10, 478, 294);
		chattingPane.add(chat_ta);
		chat_ta.setEditable(false);
		input_tf = new JTextField();
		input_tf.setBounds(62, 322, 360, 30);
		input_tf.setFont(new Font("���ü", Font.ITALIC, 20)); 		
		chattingPane.add(input_tf);
		input_tf.setColumns(10);

		send_btn.setBounds(433, 322, 60, 30);
		chattingPane.add(send_btn);

		setVisible(false);

		send_btn.addActionListener(new MyActionListener()); 				// ���۹�ư �̺�Ʈ
		input_tf.addKeyListener(new MyKeyAdapter()); 						// �޽����Է� â �̺�Ʈ
	}

	// ���� ����
	private void connect() {
		
		try {

			clientSocket = new Socket(ip, port); 							// ���� IP, ��Ʈ��ȣ�� �����ϴ� ���� ����

			try {
				in = new DataInputStream(clientSocket.getInputStream());
				out = new DataOutputStream(clientSocket.getOutputStream());
			}

			catch (IOException e) {
				e.printStackTrace();
			}

			setVisible(true); 												// ä�ù� ������ Ȱ��ȭ
			Login.setVisible(false); 										// �α���â ������ ��Ȱ��ȭ

			try {
				out.writeUTF(name); 										// �̸��� ������ ����
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			nLabel = new JLabel(name);						 	
			nLabel.setBounds(12, 325, 94, 21);	
			chattingPane.add(nLabel);

			// ���� ���� ������
			ClientReceiver th = new ClientReceiver();
			th.start(); 													// ������ ����

		} catch (UnknownHostException e) {
			System.out.println("Unknown");
		} catch (IOException e) {
			System.out.println("�Է� �����ϼ̽��ϴ�.");
		}
	}

	// ���� ���� ������
	class ClientReceiver extends Thread {

		public void run() {

			while (true) {
				try {
					String msg = in.readUTF(); 								// �޽��� ����
					chat_ta.append(msg + "\n"); 							// ä��â�� ǥ��
				} catch (IOException e) {
					try {
						out.close();
						in.close();
						clientSocket.close();
						chat_ta.append("������ ������ ������ϴ�.");
						break;
					} catch (IOException ex) {
					}
				}
			}
		}
	}

	// ��ư �̺�Ʈ
	private class MyActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {

			// �α��� ��ư�� ���� ���
			if (e.getSource() == login_btn) {
				ip = ip_tf.getText(); 										// IP ����
				port = Integer.parseInt(port_tf.getText()); 				// ��Ʈ��ȣ ����
				name = name_tf.getText(); 									// �̸� ����
				connect(); 													// ���� ���� ����
			}

			// ���� ��ư�� ���� ���
			else if (e.getSource() == send_btn) {
				try {
					out.writeUTF(input_tf.getText()); 						// ������ �޽��� ����
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				input_tf.setText(null); 								
				input_tf.requestFocus();
			}

		}
	}

	// ä�� �Է� �̺�Ʈ
	private class MyKeyAdapter extends KeyAdapter {
		public void keyPressed(KeyEvent e) {

			// ����Ű�� �Է��� ���, Enter Ű�� �ڵ尪�� 10
			if (e.getKeyCode() == 10) {
				try {
					out.writeUTF(input_tf.getText()); 						// ������ �޽��� ����
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		
				input_tf.setText(null); 
				input_tf.requestFocus();
			}
		}
	}
	
	// Ŭ���̾�Ʈ ������ ����
	public static void main(String[] args) {
		new Client(); 
	}
}
