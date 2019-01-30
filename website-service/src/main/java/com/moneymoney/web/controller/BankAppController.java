package com.moneymoney.web.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.moneymoney.web.entity.CurrentDataSet;
import com.moneymoney.web.entity.Transaction;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@EnableDiscoveryClient
@EnableCircuitBreaker
@Controller
public class BankAppController {

	private CurrentDataSet currentDataSet;
	
	@Autowired
	private RestTemplate restTemplate;

	
	@RequestMapping("/")
	public String depositForm() {
		return "DepositForm";
	}
	
	@HystrixCommand(fallbackMethod="serverFallback")
	@RequestMapping(value = "/deposit", method = RequestMethod.POST)
	public String deposit(@ModelAttribute Transaction transaction, Model model) {
		restTemplate.postForEntity("http://zuul-service/transactions-service/transactions", transaction, null);
		model.addAttribute("message", "Transaction completed Successfully!");
		return "DepositForm";
	}

	@RequestMapping("/withdraw")
	public String withdrawForm() {
		return "WithdrawForm";
	}
	
	@HystrixCommand(fallbackMethod="serverFallback")
	@RequestMapping(value = "/withdraw", method = RequestMethod.POST)
	public String withdraw(@ModelAttribute Transaction transaction, Model model) {
		restTemplate.postForEntity("http://zuul-service/transactions-service/transactions/withdraw", transaction, null);
		model.addAttribute("message", "Transaction completed Successfully!");
		return "WithdrawForm";
	}
	
	public String serverFallback(@ModelAttribute Transaction transaction,Model model) {
		String message = "Transaction failed... Server under maintenance please try after some time !!!";
		model.addAttribute("message", message);
		return "ServerStatus";
	}
	
	@RequestMapping("/transfer")
	public String fundTransferForm() {
		return "FundTransfer";
	}

	public String transferFallback(Model model) {
		String message = "Transaction failed !!! Server issue please try after some time !!!";
		model.addAttribute("message", message);
		return "ServerStatus";
		
	}
	
	@HystrixCommand(fallbackMethod="fundtransferFallback")
	@RequestMapping(value = "/transfer", method = RequestMethod.POST)
	public String fundtransfer(@RequestParam("senderAccountNumber") int senderAccountNumber,
			@RequestParam("amount") double amount,
			@RequestParam("receiverAccountNumber") int receiverAccountnumber,Model model) {
		Transaction transaction = new Transaction();
		transaction.setAccountNumber(senderAccountNumber);
		transaction.setAmount(amount);
		transaction.setTransactionDetails("Transferred online");
		restTemplate.postForEntity("http://zuul-service/transactions-service/transactions/transfer?receiverAccountnumber=" + receiverAccountnumber,transaction, null);
		model.addAttribute("message", "Success!!!");
		return "FundTransfer";
	}

	public String fundtransferFallback(@RequestParam("senderAccountNumber") int senderAccountNumber,
			@RequestParam("amount") double amount, @RequestParam("receiverAccountNumber") int receiverAccountnumber,Model model) {
		String message = "Transaction failed !!! Server issue please try after some time !!!";
		model.addAttribute("message", message);
		return "ServerStatus";
	}
	
	@HystrixCommand(fallbackMethod="statementsFallback")
	@RequestMapping("/statements")
	public ModelAndView getStatements(@RequestParam("offset") int offset, @RequestParam("size") int size) {
		CurrentDataSet storeDataset = restTemplate.getForObject("http://zuul-service/transactions-service/transactions/statement",
				CurrentDataSet.class);
		currentDataSet = storeDataset;
		int currentSize = size == 0 ? 5 : size;
		int currentOffset = offset == 0 ? 1 : offset;
		Link next = linkTo(methodOn(BankAppController.class).getStatements(currentOffset + currentSize, currentSize)).withRel("next");
		Link previous = linkTo(methodOn(BankAppController.class).getStatements(currentOffset - currentOffset, currentSize)).withRel("previous");
		List<Transaction> currentDataSetList = new ArrayList<Transaction>();
		List<Transaction> transactions = currentDataSet.getTransactions();
		System.out.println(transactions);
		
		for (int i = currentOffset - 1; i < currentSize + currentOffset - 1; i++) {
			if((transactions.size()<=i && i>0) || currentOffset<1) break;
			Transaction transaction = transactions.get(i);
			currentDataSetList.add(transaction);
		}

		CurrentDataSet dataSet = new CurrentDataSet(currentDataSetList, next, previous);
		
		return new ModelAndView("Statements", "currentDataSet", dataSet);
	}
	
	public ModelAndView statementsFallback( @RequestParam("offset") int offset, @RequestParam("size") int size ) {
		CurrentDataSet storeDataset = currentDataSet;
		
		int currentSize = size == 0 ? 5 : size;
		int currentOffset = offset == 0 ? 1 : offset;
		Link next = linkTo(methodOn(BankAppController.class).getStatements(currentOffset + currentSize, currentSize)).withRel("next");
		Link previous = linkTo(methodOn(BankAppController.class).getStatements(currentOffset - currentOffset, currentSize)).withRel("previous");
		List<Transaction> currentDataSetList = new ArrayList<Transaction>();
		List<Transaction> transactions = currentDataSet.getTransactions();
		System.out.println(transactions);
		
		for (int i = currentOffset - 1; i < currentSize + currentOffset - 1; i++) {
			if((transactions.size()<=i && i>0) || currentOffset<1) break;
			Transaction transaction = transactions.get(i);
			currentDataSetList.add(transaction);
		}

		CurrentDataSet dataSet = new CurrentDataSet(currentDataSetList, next, previous);
		 
		return new ModelAndView("StatementsFallback", "currentDataSet",dataSet );
	}

}
