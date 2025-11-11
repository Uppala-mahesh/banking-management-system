package bankingmanagementsystem;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private int messageId;
    private String customerAccountNumber;
    private String customerName;
    private String content;
    private String reply;
    private LocalDateTime dateCreated;
    private boolean resolved;

    public Message(int messageId, String customerAccountNumber, String customerName, String content) {
        this.messageId = messageId;
        this.customerAccountNumber = customerAccountNumber;
        this.customerName = customerName;
        this.content = content;
        this.dateCreated = LocalDateTime.now();
        this.reply = "No reply yet.";
        this.resolved = false;
    }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
    public void setReply(String reply) { this.reply = reply; }

    // --- NEW METHODS ---
    public int getMessageId() {
        return messageId;
    }

    public String getCustomerAccountNumber() {
        return customerAccountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getContent() {
        return content;
    }

    public String getReply() {
        return reply;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }
    // --- END NEW METHODS ---

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format(
                "\n--- Message ID: %d ---\n" +
                "From: %s (Acc: %s) on %s\n" +
                "Issue: %s\n" +
                "Reply: %s\n" +
                "Status: %s\n" +
                "--------------------",
                messageId, customerName, customerAccountNumber, dateCreated.format(formatter), content, reply,
                (resolved ? "Resolved" : "Pending")
        );
    }
}