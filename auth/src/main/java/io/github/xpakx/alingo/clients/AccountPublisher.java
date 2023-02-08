package io.github.xpakx.alingo.clients;

import io.github.xpakx.alingo.clients.event.AccountEvent;
import io.github.xpakx.alingo.user.Account;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AccountPublisher {
    private final AmqpTemplate template;
    private final String accountsTopic;

    public AccountPublisher(AmqpTemplate template, @Value("${amqp.exchange.accounts}") String accountsTopic) {
        this.template = template;
        this.accountsTopic = accountsTopic;
    }

    public void sendNewAccount(Account account) {
        AccountEvent event = new AccountEvent();
        event.setId(account.getId());
        event.setUsername(account.getUsername());
        template.convertAndSend(accountsTopic, "account", event);
    }
}