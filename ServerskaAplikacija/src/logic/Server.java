package logic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	public static void main(String[] args) {
		int port = 7001;
		ServerSocket serverSoket = null;
		Socket soketZaKomunikaciju = null;

		try {
			serverSoket = new ServerSocket(port);
			while (true) {
				System.out.println("Cekam na konekciju....");
				soketZaKomunikaciju = serverSoket.accept();
				System.out.println("Doslo je do konekcije....");

				ClientHandler klijent = new ClientHandler(soketZaKomunikaciju);
				klijent.start();
			}
		} catch (IOException e) {
			System.out.println("Greska prilikom pokretanja servera....");
		}

	}
}
