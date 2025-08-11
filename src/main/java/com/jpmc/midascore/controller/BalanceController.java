package com.jpmc.midascore.controller;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/balance")
public class BalanceController {
    
    private static final Logger logger = LoggerFactory.getLogger(BalanceController.class);
    
    private final UserRepository userRepository;
    
    @Autowired
    public BalanceController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @GetMapping
    public ResponseEntity<Balance> getUserBalance(@RequestParam("userId") long userId) {
        logger.info("Balance query requested for user ID: {}", userId);
        
        UserRecord user = userRepository.findById(userId);
        
        if (user == null) {
            logger.info("User with ID {} not found, returning balance 0", userId);
            return ResponseEntity.ok(new Balance(0.0f));
        }
        
        float balance = user.getBalance();
        logger.info("User {} (ID: {}) has balance: {}", user.getName(), userId, balance);
        
        return ResponseEntity.ok(new Balance(balance));
    }
}
