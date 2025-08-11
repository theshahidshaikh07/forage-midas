package com.jpmc.midascore.service;

import com.jpmc.midascore.dto.Incentive;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;
    
    @Autowired
    public TransactionService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.restTemplate = new RestTemplate();
    }
    
    @Transactional
    public void processTransaction(Transaction transaction) {
        logger.info("Processing transaction: {}", transaction);
        
        // Find sender and recipient
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());
        
        // Validate transaction
        boolean isValid = validateTransaction(transaction, sender, recipient);
        
        // Get incentive from API (regardless of validation result)
        float incentiveAmount = getIncentiveFromAPI(transaction);
        logger.info("Incentive received for transaction: {}", incentiveAmount);
        
        if (isValid) {
            // Execute the transaction
            executeTransaction(transaction, sender, recipient, incentiveAmount);
            logger.info("Transaction executed successfully: {}", transaction);
        } else {
            logger.warn("Transaction validation failed: {}", transaction);
        }
        
        // Record the transaction (valid or invalid) with incentive
        TransactionRecord record = TransactionRecord.fromTransaction(transaction, sender, recipient, isValid, incentiveAmount);
        transactionRepository.save(record);
        logger.info("Transaction recorded: {}", record);
    }
    
    private boolean validateTransaction(Transaction transaction, UserRecord sender, UserRecord recipient) {
        // Check if sender exists
        if (sender == null) {
            logger.warn("Sender with ID {} not found", transaction.getSenderId());
            return false;
        }
        
        // Check if recipient exists
        if (recipient == null) {
            logger.warn("Recipient with ID {} not found", transaction.getRecipientId());
            return false;
        }
        
        // Check if sender has sufficient balance
        if (sender.getBalance() < transaction.getAmount()) {
            logger.warn("Insufficient balance for sender {}: required {}, available {}", 
                sender.getName(), transaction.getAmount(), sender.getBalance());
            return false;
        }
        
        // Check if amount is positive
        if (transaction.getAmount() <= 0) {
            logger.warn("Invalid transaction amount: {}", transaction.getAmount());
            return false;
        }
        
        return true;
    }
    
    private void executeTransaction(Transaction transaction, UserRecord sender, UserRecord recipient, float incentiveAmount) {
        // Deduct amount from sender
        float newSenderBalance = sender.getBalance() - transaction.getAmount();
        sender.setBalance(newSenderBalance);
        userRepository.save(sender);
        
        // Add amount to recipient (transaction amount + incentive)
        float newRecipientBalance = recipient.getBalance() + transaction.getAmount() + incentiveAmount;
        recipient.setBalance(newRecipientBalance);
        userRepository.save(recipient);
        
        logger.info("Balances updated - Sender {}: {} -> {}, Recipient {}: {} -> {} (including incentive: {})", 
            sender.getName(), sender.getBalance() + transaction.getAmount(), newSenderBalance,
            recipient.getName(), recipient.getBalance() - transaction.getAmount() - incentiveAmount, newRecipientBalance, incentiveAmount);
    }
    
    private float getIncentiveFromAPI(Transaction transaction) {
        try {
            String url = "http://localhost:8080/incentive";
            Incentive response = restTemplate.postForObject(url, transaction, Incentive.class);
            
            if (response != null) {
                logger.info("Incentive API response: {}", response);
                return response.getAmount();
            } else {
                logger.warn("Incentive API returned null response");
                return 0.0f;
            }
        } catch (Exception e) {
            logger.error("Error calling Incentive API: {}", e.getMessage());
            return 0.0f;
        }
    }
}
