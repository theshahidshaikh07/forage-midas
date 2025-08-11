package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionKafkaListener {
    private static final Logger logger = LoggerFactory.getLogger(TransactionKafkaListener.class);
    
    @Value("${general.kafka-topic}")
    private String topic;
    
    @Autowired
    private TransactionService transactionService;
    
    private int transactionCount = 0;
    private final float[] firstFourAmounts = new float[4];
    
    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
    public void listen(Transaction transaction) {
        transactionCount++;
        logger.info("Received transaction #{}: {}", transactionCount, transaction);
        logger.info("Transaction amount: {}", transaction.getAmount());
        
        // Process the transaction through the service
        try {
            transactionService.processTransaction(transaction);
        } catch (Exception e) {
            logger.error("Error processing transaction: {}", transaction, e);
        }
        
        // Collect the first 4 transaction amounts (for Task 2 compatibility)
        if (transactionCount <= 4) {
            firstFourAmounts[transactionCount - 1] = transaction.getAmount();
            logger.info("Collected amount #{}: {}", transactionCount, transaction.getAmount());
            
            // If we have all 4, display them
            if (transactionCount == 4) {
                logger.info("==========================================");
                logger.info("FIRST 4 TRANSACTION AMOUNTS COLLECTED:");
                logger.info("==========================================");
                for (int i = 0; i < 4; i++) {
                    logger.info("Transaction {}: {}", i + 1, firstFourAmounts[i]);
                }
                logger.info("==========================================");
                logger.info("TASK 2 COMPLETE! You can now stop the test.");
                logger.info("==========================================");
            }
        }
    }
}
