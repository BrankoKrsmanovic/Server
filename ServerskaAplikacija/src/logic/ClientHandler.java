package logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {

	private BufferedReader clientInput = null;
	private PrintStream clientOutput = null;
	private Socket soketZaKomunikaciju = null;

	public ClientHandler(Socket soketZaKomunikaciju) {
		this.soketZaKomunikaciju = soketZaKomunikaciju;
	}

	private String racunaj(String unos) {
		if (unos == null || unos.equalsIgnoreCase("***quit") || unos.equalsIgnoreCase("n"))
			return null;
		String izraz[] = unos.split(" ");

		if (izraz.length != 3)
			return "GREŠKA: Neispravan unos!";
		double x, y;
		String znak = izraz[1];
		try {
			x = Double.parseDouble(izraz[0]);
			y = Double.parseDouble(izraz[2]);
		} catch (NumberFormatException e) {
			return "GREŠKA: Niste uneli brojeve!";
		}
		switch (znak) {
		case "+":
			return Double.toString(x + y);
		case "-":
			return Double.toString(x - y);
		case "*":
			return Double.toString(x * y);
		case "/":{
			if(y != 0)
				return Double.toString(x / y);
			else 
				return "GREŠKA: Neispravna računska operacija";
		}
		default:
			return "GREŠKA: Neispravna računska operacija";
		}
	}

	private String obradiUsername(String username) throws IOException {

		if (username == null || username.equalsIgnoreCase("***quit") || username.equalsIgnoreCase("n")) {
			return null;
		}

		if (username.isEmpty() || username.contains(" ") || username.contains("\t"))
			return "GREŠKA: Nedozvoljeni unos!";

		BufferedReader in = new BufferedReader(new FileReader("Data/korisnici.txt"));
		boolean kraj = false;

		while (!kraj) {
			String pom = in.readLine();
			if (pom == null)
				kraj = true;
			else {
				if (pom.equalsIgnoreCase("[USERNAME]" + username)) {
					return "GREŠKA: Korisničko ime je zauzeto!";
				}
			}
		}
		in.close();

		return "OK";
	}

	private String obradiPassword(String password) throws IOException {
		if (password == null || password.equalsIgnoreCase("***quit") || password.equalsIgnoreCase("n")) {
			return null;
		}
		if (password.length() < 8) {
			return "GREŠKA: Šifra ne sme biti manja od 8 karaktera";
		}

		if (!password.matches(".*\\d+.*"))
			return "GREŠKA: Šifra mora da sadrži bar jednu cifru!";
		String pom = password.toLowerCase();
		if (pom.equals(password))
			return "GREŠKA: Šifra mora da sadrži bar jedno veliko slovo!";

		return "OK";
	}

	private void signUp(PrintStream izlaz, BufferedReader ulaz, Socket soketZaKomunikaciju) {
		boolean validanUnos = false;

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Data/korisnici.txt", true)));

			do {
				izlaz.println("Unesite korisnicko ime (ne sme da sadrži razmak): ");
				String username = ulaz.readLine();
				String rezultat = obradiUsername(username);

				if (rezultat == null) {
					izlaz.println("Dovidjenja");
					soketZaKomunikaciju.close();
					return;
				}
				if (rezultat.startsWith("GREŠKA:")) {
					izlaz.println(rezultat);
				} else {
					validanUnos = true;
					out.println("[USERNAME]" + username);
					izlaz.println("Korisnicko ime je ispravno.");
				}
			} while (!validanUnos);

			validanUnos = false;

			do {
				izlaz.println("Unesite šifru (mora da sadrži bar jednu cifru i bar jedno veliko slovo): ");
				String password = ulaz.readLine();
				String rezultat = obradiPassword(password);

				if (rezultat == null) {
					izlaz.println("Dovidjenja");
					soketZaKomunikaciju.close();
					return;
				}
				if (rezultat.startsWith("GREŠKA:")) {
					izlaz.println(rezultat);
				} else {
					validanUnos = true;
					out.println("[PASSWORD]" + password);
					izlaz.println("Šifra je ispravna.");
					out.close();
				}
			} while (!validanUnos);

			izlaz.println("Uspešno ste kreirali korisnicki nalog.");
			out.close();

			while (true) {
				izlaz.println("Unesite [***quit] ili [N] za izlazak iz programa.");
				String zaIzlazak = ulaz.readLine();
				if (zaIzlazak.equalsIgnoreCase("N") || zaIzlazak.equalsIgnoreCase("***quit")) {
					izlaz.println("Dovidjenja");
					soketZaKomunikaciju.close();
					return;
				}
			}
		} catch (IOException e) {
			izlaz.println("GREŠKA: Fatal error!");
		}
	}

	private String nadjiUsername(String username, String password) {
		try {
			BufferedReader in = new BufferedReader(new FileReader("Data/korisnici.txt"));
			boolean kraj = false;

			while (!kraj) {
				String pom = in.readLine();
				if (pom == null) {
					in.close();
					return "GREŠKA: Korisnicko ime ne postoji u bazi.";
				}
				if (pom.equals("[USERNAME]" + username)) {
					String pom2 = in.readLine();
					if (pom2.equals("[PASSWORD]" + password)) {
						in.close();
						return "OK";
					}
					in.close();
					return "GREŠKA: Šifra se ne poklapa sa korisnickim imenom.";
				}
			}
			in.close();
		} catch (IOException e) {
			return "GREŠKA: Fatal error!";
		}
		return "GREŠKA: Korisnicko ime ne postoji u bazi.";
	}

	private void logIn(PrintStream izlaz, BufferedReader ulaz, Socket soketZaKomunikaciju) {
		boolean kraj = false;
		String username;
		try {
			do {
				izlaz.println("Unesite korisnicko ime: ");
				username = ulaz.readLine();

				if (username == null || username.equalsIgnoreCase("***quit") || username.equalsIgnoreCase("n")) {
					izlaz.println("Dovidjenja");
					soketZaKomunikaciju.close();
					return;
				}

				izlaz.println("Unseite šifru: ");
				String password = ulaz.readLine();

				if (password == null || password.equalsIgnoreCase("***quit") || password.equalsIgnoreCase("n")) {
					izlaz.println("Dovidjenja");
					soketZaKomunikaciju.close();
					return;
				}

				String rezultat = nadjiUsername(username, password);

				if (rezultat.startsWith("GREŠKA:")) {
					izlaz.println(rezultat);
					izlaz.println("Da izadjete iz programa unesite [***quit] ili [N]\n"
							+ "Da kreirate novi nalog unesite [Y]\n" + "Da pokušate ponovo unesite bilo sta\n");
					String pom = ulaz.readLine();
					if (pom == null || pom.equalsIgnoreCase("**quit") || pom.equalsIgnoreCase("N")) {
						izlaz.println("Dovidjenja");
						soketZaKomunikaciju.close();
						return;
					} else if (pom.equalsIgnoreCase("Y")) {
						kraj = true;
						izlaz.println("REGISTRACIJA");
						signUp(izlaz, ulaz, soketZaKomunikaciju);
					}
				}

				if (rezultat.equals("OK")) {
					kraj = true;
					izlaz.println("Uspešno ste se prijavili. Dobrodošli " + username);
				}

			} while (!kraj);

			String nazivFajla = "Data/" + username + ".txt";
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(nazivFajla, true)));
			while (true) {
				
				izlaz.println("Za kalkulator unesite 1\n" + "Za spisak svih kalkulacija unesite 2\n"
						+ "Za izlazak unesite [***quit] ili [N]");

				String izbor = ulaz.readLine();

				if (izbor == null || izbor.equalsIgnoreCase("***quit") || izbor.equalsIgnoreCase("N")) {
					izlaz.println("Dovidjenja");
					soketZaKomunikaciju.close();
					return;
				}
				
				if (izbor.equalsIgnoreCase("1")) {
					kraj = false;

					while (!kraj) {
						out = new PrintWriter(new BufferedWriter(new FileWriter(nazivFajla, true)));
						izlaz.println("Unesi izraz u formatu: PRVI_BROJ ZNAK DRUGI_BROJ [5 + 6]");
						String izraz = ulaz.readLine();
						String rezultat = racunaj(izraz);

						if (rezultat == null) {
							out.close();
							izlaz.println("Dovidjenja");
							soketZaKomunikaciju.close();
							return;
						}

						izlaz.println(rezultat);
						if (!rezultat.startsWith("GREŠKA:"))
							out.println(izraz + " = " + rezultat);

						out.close();

						izlaz.println("Da li zelite da nastavite?\n0-NE\t1-DA");
						String nastavak = ulaz.readLine();

						if (nastavak == null || nastavak.equalsIgnoreCase("***quit")
								|| nastavak.equalsIgnoreCase("N")) {
							izlaz.println("Dovidjenja");
							soketZaKomunikaciju.close();
							return;
						}
						if (nastavak.equalsIgnoreCase("0"))
							kraj = true;
					}
				} else if (izbor.equalsIgnoreCase("2")) {
					BufferedReader in = new BufferedReader(new FileReader(nazivFajla));
					izlaz.println(nazivFajla);
					kraj = false;
					String s = "[KALKULACIJE]";
					while (!kraj) {
						String pom = in.readLine();
						if (pom == null)
							kraj = true;
						else
							s += (pom + "\t");
					}
					in.close();
					izlaz.println(s);
				} else {
					izlaz.println("Uneli ste nepostojecu komandu!Pokusajte ponovo");
				}

			}
		} catch (IOException e) {
			izlaz.println("GREŠKA: Fatal error!");
		}
	}

	public void mainMenu(PrintStream izlaz, BufferedReader ulaz, Socket soketZaKomunikaciju) throws IOException {

		boolean isValid = false;

		do {
			izlaz.println("IZABERITE OPCIJU:" + "\n1)REGISTRACIJA" + "\n2)PRIJAVA" + "\n3)NASTAVI KAO GOST");

			String izbor = ulaz.readLine();

			switch (izbor) {
			case "1": {
				isValid = true;
				signUp(izlaz, ulaz, soketZaKomunikaciju);
				break;
			}
			case "2": {
				isValid = true;
				logIn(this.clientOutput, this.clientInput, this.soketZaKomunikaciju);
				break;
			}
			case "3": {
				isValid = true;
				for (int i = 0; i < 3; i++) {

					izlaz.println("Unesi izraz u formatu: PRVI_BROJ ZNAK DRUGI_BROJ [5 + 6]");
					String izraz = ulaz.readLine();
					String rezultat = racunaj(izraz);

					if (rezultat == null) {
						izlaz.println("Dovidjenja");
						soketZaKomunikaciju.close();
						return;
					}
					if (rezultat.startsWith("GREŠKA"))
						i--;
					izlaz.println(rezultat);

					izlaz.println(
							"Broj preostalih kalkulacija: " + (2 - i) + "\nDa li zelite da nastavite?\nN-NE\tY-DA");
					if (ulaz.readLine().equalsIgnoreCase("N")) {
						izlaz.println("Dovidjenja");
						soketZaKomunikaciju.close();
						return;
					}
				}

				izlaz.println(
						"Samo registrovani korisnici mogu da vrše više od 3 kalkulacije po sesiji. Da li želite da se registrujete?\nN-NE\tY-DA");
				if (ulaz.readLine().equalsIgnoreCase("N")) {
					izlaz.println("Dovidjenja");
					soketZaKomunikaciju.close();
					return;
				} else {
					signUp(izlaz, ulaz, soketZaKomunikaciju);
				}
				break;
			}
			case "***quit": {
				isValid = true;
				izlaz.println("Dovidjenja");
				soketZaKomunikaciju.close();
				return;
			}
			case "N": {
				isValid = true;
				izlaz.println("Dovidjenja");
				soketZaKomunikaciju.close();
				return;
			}
			case "n": {
				isValid = true;
				izlaz.println("Dovidjenja");
				soketZaKomunikaciju.close();
				return;
			}
			default: {
				izlaz.println("Uneli ste nepostojecu komandu!Pokusajte ponovo");
			}
			}
		} while (!isValid);
	}

	@Override
	public void run() {
		try {
			clientInput = new BufferedReader(new InputStreamReader(soketZaKomunikaciju.getInputStream()));
			clientOutput = new PrintStream(soketZaKomunikaciju.getOutputStream());

			clientOutput.println("***********************ONLINE CALCULATOR***********************");
			clientOutput.println("Za prekid rada aplikacije u bilo kom trenutku unesite: ***quit\n\n");

			mainMenu(this.clientOutput, this.clientInput, this.soketZaKomunikaciju);

			soketZaKomunikaciju.close();
		} catch (IOException e) {
			System.out.println("Nasilno zatvaranje!");
		}
	}

}
