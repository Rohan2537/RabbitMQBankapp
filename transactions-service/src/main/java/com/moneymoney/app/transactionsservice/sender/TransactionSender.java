package com.moneymoney.app.transactionsservice.sender;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.moneymoney.app.transactionsservice.entity.Transaction;

@Component
public class TransactionSender {

	@Autowired
	private RabbitMessagingTemplate template;

	@Bean
	public Queue accountQueue() {
		return new Queue("AccountQueue", false);
	}

	public void sendCurrentBalance(Transaction transaction) { 
		System.out.println(transaction);
		template.convertAndSend("AccountQueue", transaction);

	}

	
}
