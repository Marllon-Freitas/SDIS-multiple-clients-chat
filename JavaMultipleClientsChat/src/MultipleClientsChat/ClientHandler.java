package MultipleClientsChat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
	
	public static ArrayList<ClientHandler> clientHandlerList = new ArrayList<>();
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String clientUserName;
	
	public ClientHandler (Socket socket) {
		try {
			this.socket = socket;

			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			
			//pegando o nome do usuario
			this.clientUserName = bufferedReader.readLine();
			
			//adicionando ele na lista de usuarios
			clientHandlerList.add(this); 
			
			//envia a mensagem para os outros usuarios
			broadcastMessage("SERVIDOR: " + clientUserName + " entrou no grupo!");
			
		} catch(IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter );
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		String messageFromClient;
		
		while (socket.isConnected()) {
			try {
				messageFromClient = bufferedReader.readLine();
				broadcastMessage(messageFromClient);
			} catch (IOException e) {
				closeEverything(socket, bufferedReader, bufferedWriter );
				break;
			}
		}
	}
	
	//envia a mensagem para todos no grupo
	public void broadcastMessage(String messageToSend) {
		//loopa por todos os usuarios no array de usuarios
		for (ClientHandler clientHandler : clientHandlerList) {
			try {
				//envia a mensagem para todos os usuarios menos pra quem enviou
				if (!clientHandler.clientUserName.equals(clientUserName)) {
					clientHandler.bufferedWriter.write(messageToSend);	
					clientHandler.bufferedWriter.newLine();	
					clientHandler.bufferedWriter.flush();	
				}
			}  catch (IOException e) {
				closeEverything(socket, bufferedReader, bufferedWriter );
				break;
			}
		}
	}
	
	//remove o usuario do grupo
	public void removeClientHandler() {
		clientHandlerList.remove(this);
		broadcastMessage(clientUserName + " deixou o grupo!");
	}
	
	public void closeEverything(Socket socket, BufferedReader bufferedReader,BufferedWriter bufferedWriter ) {
		removeClientHandler();
		try {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

}
