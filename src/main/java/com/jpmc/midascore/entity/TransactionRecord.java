package com.jpmc.midascore.entity;

import com.jpmc.midascore.foundation.Transaction;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_records")
public class TransactionRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserRecord sender;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserRecord recipient;
    
    @Column(nullable = false)
    private float amount;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private boolean valid;
    
    @Column(nullable = false)
    private float incentive;
    
    protected TransactionRecord() {}
    
    public TransactionRecord(UserRecord sender, UserRecord recipient, float amount, boolean valid, float incentive) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.valid = valid;
        this.incentive = incentive;
        this.timestamp = LocalDateTime.now();
    }
    
    public static TransactionRecord fromTransaction(Transaction transaction, UserRecord sender, UserRecord recipient, boolean valid, float incentive) {
        return new TransactionRecord(sender, recipient, transaction.getAmount(), valid, incentive);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UserRecord getSender() { return sender; }
    public void setSender(UserRecord sender) { this.sender = sender; }
    
    public UserRecord getRecipient() { return recipient; }
    public void setRecipient(UserRecord recipient) { this.recipient = recipient; }
    
    public float getAmount() { return amount; }
    public void setAmount(float amount) { this.amount = amount; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    
    public float getIncentive() { return incentive; }
    public void setIncentive(float incentive) { this.incentive = incentive; }
    
    @Override
    public String toString() {
        return String.format("TransactionRecord[id=%d, sender='%s', recipient='%s', amount=%.2f, valid=%s, incentive=%.2f, timestamp=%s]",
                id, sender != null ? sender.getName() : "null", 
                recipient != null ? recipient.getName() : "null", 
                amount, valid, incentive, timestamp);
    }
}
