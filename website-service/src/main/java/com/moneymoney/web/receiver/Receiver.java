package com.moneymoney.web.receiver;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import com.moneymoney.web.controller.BankAppController;
import com.moneymoney.web.entity.Transaction;

public class Receiver {
	
	BankAppController controller;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@RabbitListener(queues = "TransactionQueue")
	public void processDeposit(Transaction transaction,Model model) {
		controller.deposit(transaction, model);
		System.out.println("Deposit in process");
	}
	
	@RabbitListener(queues = "TransactionQueue")
	public void processWithdraw(Transaction transaction,Model model) {
		controller.withdraw(transaction, model);
		System.out.println("Withdrawl in process");
	}
	
	@RabbitListener(queues = "TransactionQueue")
	public void processFundTransfer(int senderAccountNumber,double amount,int receiverAccountNumber,Model model) {
		controller.fundtransfer(senderAccountNumber, amount, receiverAccountNumber, model);
		System.out.println("Deposit in process");
	}
}
