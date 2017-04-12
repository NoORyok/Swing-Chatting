package 서버;

import java.awt.event.*;
import javax.swing.*;
import java.io.*; 
import java.net.*; 
import java.util.*; 

// 서버 프레임
public class Server extends JFrame {
	
	// 서버 프레임 컴포넌트
	private JPanel contentPane;
	private JTextField server_tf;
	private JTextArea server_ta = new JTextArea();
	private JButton start_btn = new JButton("서 버 실 행");
	private JLabel pLabel = new JLabel("포 트 번 호");;
	
	// 소켓 객체
	private ServerSocket serverSocket; 										// 서버 소켓
	private Socket socket; 													// 클라이언트 통신 소켓
	
	private int port; 														// 포트번호

	// hashMap (K : 클라이언트 이름, V : 출력스트림)
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

	// 연결 요청 메소드
	private void connect(int port) {
		
		try {
			serverSocket = new ServerSocket(port); 							// 서버 소켓(포트번호)
		} catch (IOException e) 											// 소켓 생성 실패
		{
			server_ta.append("서버 실행 실패 - 포트번호가 사용중입니다...\n");
			server_tf.setText(null); 								
			server_tf.requestFocus(); 								
			start_btn.setEnabled(true);
			server_tf.setEnabled(true); 							
		}

		// 클라이언트 다중접속을 위한 멀티스레딩
		MultiConnect thread = new MultiConnect(); // 다중접속 스레드 클래스
		thread.start();
	}

	// 다중 접속 스레드 클래스
	class MultiConnect extends Thread {
		
		public void run() {
			while (true) {
				try {
					server_ta.append(" Wating Connect ... \n"); 	
					socket = serverSocket.accept(); 						// 클라이언트 연결
					server_ta.append(" Connect Success ... \n"); 		

					ServerReceiver receiver = new ServerReceiver(socket);	// 통신 소켓
					receiver.start();
				}	catch (IOException e) {
					server_ta.append("연결 실패 - accept 에러가 발생되었습니다.\n");
					break;
				}
			}
		}
	}

	// 통신 스레드 클래스
	class ServerReceiver extends Thread {

		private Socket cSocket; 											// 클라이언트 통신 소켓
		private String name = null; 										// 클라이언트 이름

		// 입출력스트림
		private DataInputStream in;
		private DataOutputStream out;

		// 클라이언트 통신 소켓을 컬렉션에 저장
		ServerReceiver(Socket socket) {

			this.cSocket = socket; 									

			// 소켓 입출력 스트림
			try {
				in = new DataInputStream(cSocket.getInputStream());
				out = new DataOutputStream(cSocket.getOutputStream());
			} catch (IOException e) {
				server_ta.append("스트림 설정이 실패하였습니다.\n");
			}

			// 클라이언트 이름 수신
			try {
				name = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// 객체 저장
			cList.put(name, out);
		}

		// 메시지 전송 쓰레드 코드
		public void run() {
			while (true) {
				try {
					String data = name.concat(":" + in.readUTF()); 			// 메시지 수신
					server_ta.append(data + "\n"); 							// TextArea 출력
					send_All(data); 										// 모든 클라이언트로 메시지 회신
				} catch (IOException e) 									// 클라이언트 강제종료
				{
					server_ta.append(name + "의 접속이 끊겼습니다.\n"); 		
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

		// 모든 클라이언트에게 메시지를 회신하는 메서드
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

	// 실행 버튼 이벤트
	private class MyActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == start_btn) 								// 실행 버튼 클릭
			{
				if (server_tf.getText().length() == 0) 						
				{
					server_tf.setText("Please insert Port Number ...\n"); 	
					server_tf.requestFocus(); 
				} else 														// 정상 실행 경우
				{
					try {
						port = Integer.parseInt(server_tf.getText()); 		// 포트번호 할당
						server_tf.setEnabled(false);
						start_btn.setEnabled(false);

						connect(port); 										// 서버 실행
					} catch (Exception er) {
						server_tf.setText("숫자로 입력하세요! \n");
						server_tf.requestFocus();
					}
				}
			}
		}
	}
	
	// 서버 프레임 생성
	public static void main(String[] args) 	
	{
		new Server(); 
	}

}