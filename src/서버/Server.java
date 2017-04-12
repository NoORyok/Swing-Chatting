package ����;

import java.awt.event.*;
import javax.swing.*;
import java.io.*; 
import java.net.*; 
import java.util.*; 

// ���� ������
public class Server extends JFrame {
	
	// ���� ������ ������Ʈ
	private JPanel contentPane;
	private JTextField server_tf;
	private JTextArea server_ta = new JTextArea();
	private JButton start_btn = new JButton("�� �� �� ��");
	private JLabel pLabel = new JLabel("�� Ʈ �� ȣ");;
	
	// ���� ��ü
	private ServerSocket serverSocket; 										// ���� ����
	private Socket socket; 													// Ŭ���̾�Ʈ ��� ����
	
	private int port; 														// ��Ʈ��ȣ

	// hashMap (K : Ŭ���̾�Ʈ �̸�, V : ��½�Ʈ��)
	private Map<String, DataOutputStream> cList = Collections.synchronizedMap(new HashMap<String, DataOutputStream>());

	Server() {
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 315, 440);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
	
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 280, 277);
		contentPane.add(scrollPane);
		scrollPane.setViewportView(server_ta);
		server_ta.setEditable(false);


		pLabel.setBounds(12, 308, 69, 15);
		contentPane.add(pLabel);

	
		server_tf = new JTextField();
		server_tf.setBounds(88, 305, 204, 21);
		contentPane.add(server_tf);
		server_tf.setColumns(10);

	
		start_btn.addActionListener(new MyActionListener());
		start_btn.setBounds(12, 346, 280, 46);
		contentPane.add(start_btn);
		
		setVisible(true);
	}

	// ���� ��û �޼ҵ�
	private void connect(int port) {
		
		try {
			serverSocket = new ServerSocket(port); 							// ���� ����(��Ʈ��ȣ)
		} catch (IOException e) 											// ���� ���� ����
		{
			server_ta.append("���� ���� ���� - ��Ʈ��ȣ�� ������Դϴ�...\n");
			server_tf.setText(null); 								
			server_tf.requestFocus(); 								
			start_btn.setEnabled(true);
			server_tf.setEnabled(true); 							
		}

		// Ŭ���̾�Ʈ ���������� ���� ��Ƽ������
		MultiConnect thread = new MultiConnect(); // �������� ������ Ŭ����
		thread.start();
	}

	// ���� ���� ������ Ŭ����
	class MultiConnect extends Thread {
		
		public void run() {
			while (true) {
				try {
					server_ta.append(" Wating Connect ... \n"); 	
					socket = serverSocket.accept(); 						// Ŭ���̾�Ʈ ����
					server_ta.append(" Connect Success ... \n"); 		

					ServerReceiver receiver = new ServerReceiver(socket);	// ��� ����
					receiver.start();
				}	catch (IOException e) {
					server_ta.append("���� ���� - accept ������ �߻��Ǿ����ϴ�.\n");
					break;
				}
			}
		}
	}

	// ��� ������ Ŭ����
	class ServerReceiver extends Thread {

		private Socket cSocket; 											// Ŭ���̾�Ʈ ��� ����
		private String name = null; 										// Ŭ���̾�Ʈ �̸�

		// ����½�Ʈ��
		private DataInputStream in;
		private DataOutputStream out;

		// Ŭ���̾�Ʈ ��� ������ �÷��ǿ� ����
		ServerReceiver(Socket socket) {

			this.cSocket = socket; 									

			// ���� ����� ��Ʈ��
			try {
				in = new DataInputStream(cSocket.getInputStream());
				out = new DataOutputStream(cSocket.getOutputStream());
			} catch (IOException e) {
				server_ta.append("��Ʈ�� ������ �����Ͽ����ϴ�.\n");
			}

			// Ŭ���̾�Ʈ �̸� ����
			try {
				name = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// ��ü ����
			cList.put(name, out);
		}

		// �޽��� ���� ������ �ڵ�
		public void run() {
			while (true) {
				try {
					String data = name.concat(":" + in.readUTF()); 			// �޽��� ����
					server_ta.append(data + "\n"); 							// TextArea ���
					send_All(data); 										// ��� Ŭ���̾�Ʈ�� �޽��� ȸ��
				} catch (IOException e) 									// Ŭ���̾�Ʈ ��������
				{
					server_ta.append(name + "�� ������ ������ϴ�.\n"); 		
					try {
						out.close();
						in.close();
						socket.close();
						cList.remove(name);
					}
					catch (IOException ex) {
					}
					break;
				}
			}
		}

		// ��� Ŭ���̾�Ʈ���� �޽����� ȸ���ϴ� �޼���
		void send_All(String msg) {
			
			Iterator<String> it = cList.keySet().iterator();
			
			while (it.hasNext()) {
				try {
					String user_name = it.next();
					cList.get(user_name).writeUTF(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// ���� ��ư �̺�Ʈ
	private class MyActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == start_btn) 								// ���� ��ư Ŭ��
			{
				if (server_tf.getText().length() == 0) 						
				{
					server_tf.setText("Please insert Port Number ...\n"); 	
					server_tf.requestFocus(); 
				} else 														// ���� ���� ���
				{
					try {
						port = Integer.parseInt(server_tf.getText()); 		// ��Ʈ��ȣ �Ҵ�
						server_tf.setEnabled(false);
						start_btn.setEnabled(false);

						connect(port); 										// ���� ����
					} catch (Exception er) {
						server_tf.setText("���ڷ� �Է��ϼ���! \n");
						server_tf.requestFocus();
					}
				}
			}
		}
	}
	
	// ���� ������ ����
	public static void main(String[] args) 	
	{
		new Server(); 
	}

}