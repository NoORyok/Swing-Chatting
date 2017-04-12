package 클라이언트;

import java.awt.*;
import java.awt.event.*;
import java.io.*; 
import java.net.*;

import javax.swing.*;

public class Client extends JFrame {
	
	// 클라이언트 로그인 컴포넌트
	private JFrame Login = new JFrame();
	private JPanel loginPane;
	private JTextField name_tf;
	private JTextField ip_tf;
	private JTextField port_tf;
	private JButton login_btn = new JButton("Login");
	
	// 클라이언트 채팅방 컴포넌트
	private JPanel chattingPane;
	private JTextField input_tf; 							
	private JButton send_btn = new JButton("전송"); 				
	private JTextArea chat_ta = new JTextArea(); 			
	private JLabel nLabel;
	private Socket clientSocket; 										// 클라이언트 소켓
	
	private DataInputStream in;
	private DataOutputStream out;

	private String ip; 													// IP를 저장하는 문자열
	private String name; 												// 이름을 저장하는 문자열
	private int port; 													// 포트번호
	
	
	Client() {
		
		// 로그인 프레임
		Login.setTitle("로그인 창");
		Login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Login.setBounds(100, 100, 310, 450);
		loginPane = new JPanel();
		Login.setContentPane(loginPane);
		loginPane.setLayout(null);

		ImageIcon pic = new ImageIcon("images/picpic.jpg");
		JLabel imageLabel = new JLabel(pic);
		imageLabel.setBounds(35, 21, 227, 144);
		loginPane.add(imageLabel);

		JLabel IdLabel = new JLabel("이  름");
		IdLabel.setBounds(35, 185, 57, 15);
		loginPane.add(IdLabel);

		JLabel IpLabel = new JLabel("서 버 IP");
		IpLabel.setBounds(35, 235, 57, 15);
		loginPane.add(IpLabel);

		JLabel PortLabel = new JLabel("포 트 번 호");
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
		Login.setVisible(true); // 활성화
		login_btn.addActionListener(new MyActionListener()); 		

		// 채팅방 프레임
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
		input_tf.setFont(new Font("고딕체", Font.ITALIC, 20)); 		
		chattingPane.add(input_tf);
		input_tf.setColumns(10);

		send_btn.setBounds(433, 322, 60, 30);
		chattingPane.add(send_btn);

		setVisible(false);

		send_btn.addActionListener(new MyActionListener()); 				// 전송버튼 이벤트
		input_tf.addKeyListener(new MyKeyAdapter()); 						// 메시지입력 창 이벤트
	}

	// 서버 연결
	private void connect() {
		
		try {

			clientSocket = new Socket(ip, port); 							// 서버 IP, 포트번호로 연결하는 소켓 생성

			try {
				in = new DataInputStream(clientSocket.getInputStream());
				out = new DataOutputStream(clientSocket.getOutputStream());
			}

			catch (IOException e) {
				e.printStackTrace();
			}

			setVisible(true); 												// 채팅방 프레임 활성화
			Login.setVisible(false); 										// 로그인창 프레임 비활성화

			try {
				out.writeUTF(name); 										// 이름을 서버로 전송
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			nLabel = new JLabel(name);						 	
			nLabel.setBounds(12, 325, 94, 21);	
			chattingPane.add(nLabel);

			// 서버 수신 스레드
			ClientReceiver th = new ClientReceiver();
			th.start(); 													// 스레드 실행

		} catch (UnknownHostException e) {
			System.out.println("Unknown");
		} catch (IOException e) {
			System.out.println("입력 실패하셨습니다.");
		}
	}

	// 서버 수신 스레드
	class ClientReceiver extends Thread {

		public void run() {

			while (true) {
				try {
					String msg = in.readUTF(); 								// 메시지 수신
					chat_ta.append(msg + "\n"); 							// 채팅창에 표시
				} catch (IOException e) {
					try {
						out.close();
						in.close();
						clientSocket.close();
						chat_ta.append("서버와 연결이 끊겼습니다.");
						break;
					} catch (IOException ex) {
					}
				}
			}
		}
	}

	// 버튼 이벤트
	private class MyActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {

			// 로그인 버튼을 누른 경우
			if (e.getSource() == login_btn) {
				ip = ip_tf.getText(); 										// IP 저장
				port = Integer.parseInt(port_tf.getText()); 				// 포트번호 저장
				name = name_tf.getText(); 									// 이름 저장
				connect(); 													// 서버 연결 실행
			}

			// 전송 버튼을 누른 경우
			else if (e.getSource() == send_btn) {
				try {
					out.writeUTF(input_tf.getText()); 						// 서버로 메시지 전송
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				input_tf.setText(null); 								
				input_tf.requestFocus();
			}

		}
	}

	// 채팅 입력 이벤트
	private class MyKeyAdapter extends KeyAdapter {
		public void keyPressed(KeyEvent e) {

			// 엔터키를 입력한 경우, Enter 키의 코드값은 10
			if (e.getKeyCode() == 10) {
				try {
					out.writeUTF(input_tf.getText()); 						// 서버로 메시지 전송
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		
				input_tf.setText(null); 
				input_tf.requestFocus();
			}
		}
	}
	
	// 클라이언트 프레임 생성
	public static void main(String[] args) {
		new Client(); 
	}
}
