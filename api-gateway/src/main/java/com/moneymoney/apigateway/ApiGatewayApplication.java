package com.moneymoney.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

import com.moneymoney.filters.ErrorFilter;
import com.moneymoney.filters.PostFilter;
import com.moneymoney.filters.PreFilter;
import com.moneymoney.filters.RouteFilter;


@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
	/*
	 * @Bean public PreFilter preFilter() { return new PreFilter(); }
	 * 
	 * @Bean public PostFilter postFilter() { return new PostFilter(); }
	 * 
	 * @Bean public ErrorFilter errorFilter() { return new ErrorFilter(); }
	 * 
	 * @Bean public RouteFilter routeFilter() { return new RouteFilter(); }
	 */
}

