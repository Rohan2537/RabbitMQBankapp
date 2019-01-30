package com.moneymoney.app.transactionsservice.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.moneymoney.app.transactionsservice.entity.Transaction;
import com.moneymoney.app.transactionsservice.sender.TransactionSender;
import com.moneymoney.app.transactionsservice.service.TransactionService;

@EnableDiscoveryClient
@EnableCircuitBreaker
@RestController
@RequestMapping("/transactions")
public class TransactionResource {
	@Autowired
	private TransactionSender sender;
	
	@Autowired
	private TransactionService service;
	@Autowired
	private RestTemplate restTemplate;

	@GetMapping
	public List<Transaction> getAllTransactions() {

		return service.getAlltransactions();

	}

	@PostMapping
	public ResponseEntity<Transaction> deposit(@RequestBody Transaction transaction) {

		ResponseEntity<Double> entity = restTemplate.getForEntity("http://accounts-service/accounts/" + transaction.getAccountNumber() + "/balance", Double.class);

		Double currentBalance = entity.getBody();
		Double updateBalance = service.deposit(transaction.getAccountNumber(),transaction.getTransactionDetails(), currentBalance, transaction.getAmount());

		transaction.setCurrentBalance(updateBalance);
		System.out.println("Deposit in process " + transaction.toString());
		sender.sendCurrentBalance(transaction);
		return new ResponseEntity<>(HttpStatus.CREATED);

	}

	@PostMapping("/withdraw")
	public ResponseEntity<Transaction> withdraw(@RequestBody Transaction transaction) {
		ResponseEntity<Double> entity = restTemplate.getForEntity("http://accounts-service/accounts/" + transaction.getAccountNumber() + "/balance", Double.class);
		Double currentBalance = entity.getBody();
		Double updateBalance = service.withdraw(transaction.getAccountNumber(), transaction.getTransactionDetails(),currentBalance, transaction.getAmount());
		transaction.setCurrentBalance(updateBalance);
		System.out.println("Withdraw in process " + transaction.toString());
		sender.sendCurrentBalance(transaction);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("/transfer")
	public ResponseEntity<Transaction> fundTransfer(@RequestBody Transaction senderTransaction,
			@RequestParam Integer receiverAccountnumber) {
		Double[] updatedBalance = new Double[2];
		System.out.println(receiverAccountnumber);
		Transaction receiverTransaction = new Transaction();
		ResponseEntity<Double> senderEntity = restTemplate.getForEntity(
				"http://accounts-service/accounts/" + senderTransaction.getAccountNumber() + "/balance", Double.class);
		Double senderCurrentbalance = senderEntity.getBody();
		ResponseEntity<Double> receiverEntity = restTemplate
				.getForEntity("http://accounts-service/accounts/" + +receiverAccountnumber + "/balance", Double.class);
		Double receivercurrentbalance = receiverEntity.getBody();
		senderTransaction.setCurrentBalance(senderCurrentbalance);
		receiverTransaction.setAccountNumber(receiverAccountnumber);
		receiverTransaction.setCurrentBalance(receivercurrentbalance);
		updatedBalance = service.fundTransfer(senderTransaction, receiverTransaction);

		System.out.println(updatedBalance);
		senderTransaction.setCurrentBalance(updatedBalance[0]);
		receiverTransaction.setCurrentBalance(updatedBalance[1]);
		sender.sendCurrentBalance(senderTransaction);
		sender.sendCurrentBalance(receiverTransaction);
		return new ResponseEntity<>(HttpStatus.OK);

	}

	@GetMapping("/statement")
	public ResponseEntity<CurrentDataSet> getStatement() {
		CurrentDataSet currentDataSet = new CurrentDataSet();
		List<Transaction> transactions = service.getStatement();
		currentDataSet.setTransactions(transactions);

		return new ResponseEntity<>(currentDataSet,HttpStatus.OK);

	}
}
