package com.moneymoney.app.accountsservice.sender;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.moneymoney.app.accountsservice.resource.AccountResource;
import com.moneymoney.app.transactionsservice.entity.Transaction;

@Component
public class AccountReceiver {

	@Autowired
	private AccountResource resource;

	@Bean
	public Queue queue() {
		return new Queue("AccountQueue", false);
	}
	 
	@RabbitListener(queues = "AccountQueue")
	public void processMessage(Transaction transaction) {
		System.out.println("Inside Receiver");
		resource.updateAccountBalance(transaction);
	}

	

}
