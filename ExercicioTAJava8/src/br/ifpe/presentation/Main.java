package br.ifpe.presentation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.stream.StreamFilter;

import org.omg.Messaging.SyncScopeHelper;

import br.ifpe.entities.Account;
import br.ifpe.entities.AccountEnum;
import br.ifpe.entities.Client;
import br.ifpe.service.BankService;
import br.ifpe.service.ServiceFactory;

/**
 * OBSERVAÇÕES: 
 * NÃO é permitido o uso de nenhuma estrutura de repetição (for, while, do-while).
 * Tente, ao máximo, evitar o uso das estruturas if, else if, else e switch-case
 * 
 * @author Victor Lira
 * @author Vanessa Fran�a
 *
 */
public class Main {

	private static BankService service = ServiceFactory.getService();

	public static void main(String[] args) {


	}

	/**
	 * 1. Imprima na tela o nome e e-mail de todos os clientes (sem repetição), usando o seguinte formato:
	 * Victor Lira - vl@cin.ufpe.br
	 */
	public static void imprimirNomesClientes() {
		service
		.listClients()
		.stream()
		.map( c1 -> c1.getName() + " - " + c1.getEmail())
		.distinct()
		.forEach(System.out::println);

	}

	/**
	 * 2. Imprima na tela o nome do cliente e a média do saldo de suas contas, ex:
	 * Victor Lira - 352
	 */
	public static void imprimirMediaSaldos() {
		service
		.listClients()
		.stream()
		.forEach(client -> {
			double avaregeBalances = service.listAccounts()
					.stream()
					.filter(account -> account.getClient().getName().equals(client.getName()))
					.mapToDouble(account -> account.getBalance())
					.average().getAsDouble();
			System.out.println(client.getName() + " - " + avaregeBalances);
		});


		service
		.listClients()
		.stream()
		.map(client ->  client.getName() + " - " + service.listAccounts()
		.stream()
		.filter(account -> account.getClient().getName().equals(client.getName()))
		.mapToDouble(account -> account.getBalance())
		.average().getAsDouble()
				)
		.forEach(System.out::println);





	}

	/**
	 * 3. Considerando que só existem os países "Brazil" e "United States", 
	 * imprima na tela qual deles possui o cliente mais rico, ou seja,
	 * com o maior saldo somando todas as suas contas.
	 */	
	public static void imprimirPaisClienteMaisRico() {


		service
		.listClients()
		.stream()
		.forEach(client -> {
			double clientBrazil	= service.listAccounts()
					.stream()
					.filter(c1 -> c1.getClient().getAddress().getCountry().equals(client.getAddress().getCountry().equals("Brazil")))
					.mapToDouble(a1 -> a1.getBalance()).sum();

			double clientUSA = service.listAccounts()
					.stream()
					.filter(c1 -> c1.getClient().getAddress().getCountry().equals(client.getAddress().getCountry().equals("United States")))
					.mapToDouble(a1 -> a1.getBalance()).sum();
			System.out.println(Double.compare(clientBrazil, clientUSA));

		});
	}

	/**
	 * 4. Imprime na tela o saldo médio das contas da agência
	 * @param agency
	 */
	public static void imprimirSaldoMedio(int agency) {
		double averageBalances = service.listAccounts()
				.stream().filter(a1 -> a1.getAgency() == agency)
				.collect(Collectors.averagingDouble(Account:: getBalance));
		System.out.println(averageBalances);
	}

	/**
	 * 5. Imprime na tela o nome de todos os clientes que possuem conta poupança (tipo SAVING)
	 */
	public static void imprimirClientesComPoupanca() {
		service.listClients().removeIf(c1->!c1.getAccounts().stream().anyMatch(a1->a1.getType().equals(AccountEnum.SAVING)));
		service.listClients().stream().forEach(System.out::println);
	}

	/**
	 * 6.
	 * @param agency
	 * @return Retorna uma lista de Strings com o "estado" de todos os clientes da agência
	 */
	public static List<String> getEstadoClientes(int agency) {
		List<String> stateClients = (List<String>) service.listAccounts().stream()
				.filter(a1 -> a1.getAgency() == agency )
				.map(a1 -> a1.getClient().getAddress().getState())
				.collect(Collectors.toList());
		return stateClients;
	}

	/**
	 * 7.
	 * @param country
	 * @return Retorna uma lista de inteiros com os números das contas daquele país
	 */
	public static int[] getNumerosContas(String country) {
		int[]  numberAccounts = service.listAccounts().stream()
				.filter(a1 -> a1.getClient().getAddress().getCountry().equals(country))
				.mapToInt(a1 -> a1.getNumber()).toArray();

		return numberAccounts;
	}

	/**
	 * 8.
	 * Retorna o somatório dos saldos das contas do cliente em questão 
	 * @param clientEmail
	 * @return
	 */
	public static double getMaiorSaldo(String clientEmail) {

		DoubleSummaryStatistics maxBalance = service.listAccounts()
				.stream()
				.filter(a1->a1.getClient().getEmail().equals(clientEmail))
				.collect(Collectors.summarizingDouble(Account::getBalance));	

		return maxBalance.getSum();
	}

	/**
	 * 9.
	 * Realiza uma operação de saque na conta de acordo com os parâmetros recebidos
	 * @param agency
	 * @param number
	 * @param value
	 */
	public static void sacar(int agency, int number, double value) {
		service.listAccounts().stream().filter(a1 -> a1.getAgency() == agency && a1.getNumber() == number)
		.map(a1 -> a1.getBalance() - value);
	}

	/**
	 * 10. Realiza um deposito para todos os clientes do país em questão	
	 * @param country
	 * @param value
	 */
	public static void depositar(String country, double value) {
		service.listAccounts().stream().filter(a1 -> a1.getClient().getAddress().getCountry().equals(country))
		.map(a1 -> a1.getBalance() + value);
	}

	/**
	 * 11. Realiza uma transferência entre duas contas de uma agência.
	 * @param agency - agência das duas contas
	 * @param numberSource - conta a ser debitado o dinheiro
	 * @param numberTarget - conta a ser creditado o dinheiro
	 * @param value - valor da transferência
	 */
	public static void transferir(int agency, int numberSource, int numberTarget, double value) {
		service.listAccounts().stream().filter(a1 -> a1.getAgency() == agency && a1.getNumber() == numberSource)
		.map(a1 -> a1.getBalance() - value);
		service.listAccounts().stream().filter(a1 -> a1.getAgency() == agency && a1.getNumber() == numberTarget)
		.map(a1 -> a1.getBalance() + value);

	}

	/**
	 * 12.
	 * @param clients
	 * @return Retorna uma lista com todas as contas conjuntas (JOINT) dos clientes
	 */
	public static List<Account> getContasConjuntas(List<Client> clients) {

		List<Account> jointAccounts;
		service.listAccounts()
		.stream().filter(c1 -> c1.getClient().equals(clients))
		.map(c1 -> c1.getType().equals(AccountEnum.JOINT))
		.collect(Collectors.toList());
		jointAccounts = (List<Account>) Collectors.toList();
		return jointAccounts;



	}

	/**
	 * 13.
	 * @param state
	 * @return Retorna uma lista com o somatório dos saldos de todas as contas do estado 
	 */
	public static double getSomaContasEstado(String state) {
		double sumAccountState = service.listAccounts().stream()
				.filter(a1 -> a1.getClient().getAddress().getState().equals(state))
				.mapToDouble(a1 -> a1.getBalance()).sum();
		return sumAccountState;
	}

	/**
	 * 14.
	 * @return Retorna um array com os e-mails de todos os clientes que possuem contas conjuntas
	 */
	public static String[] getEmailsClientesContasConjuntas() {
		List<String> emailJointAccounts = service.listClients()
				.stream().filter(c1 -> c1.getAccounts().equals(AccountEnum.JOINT))
				.map(c1 -> c1.getEmail())
				.collect(Collectors.toList());
		return (String[]) emailJointAccounts.toArray();	
	}

	/**
	 * 15.
	 * @param number
	 * @return Retorna se o número é primo ou não
	 */
	public static boolean isPrimo(int number) {		

		boolean isPrimo = number > 1 && IntStream.range(2, number - 1)
				.noneMatch(i -> number % i == 0);
		System.out.println(isPrimo); 

		return isPrimo;
	}

	/**
	 * 16.
	 * @param number
	 * @return Retorna o fatorial do número
	 */
	public static int getFatorial(int number) {
		int factorial = IntStream.rangeClosed(1, number).reduce(1, (x,y) -> x * y);
		System.out.println(factorial);

		return factorial;
	}
} 