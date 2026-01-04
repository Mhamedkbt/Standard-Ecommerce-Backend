package com.ecommerce.backend.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // 1. Add this import
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 2. Inject the email from your Render Env Vars
    @Value("${spring.mail.username}")
    private String adminEmail;

    @Async
    public void sendOrderNotification(String customerName, double totalAmount, Long orderId) {
        // 3. Add this log so we can see it start in Render logs
        System.out.println("DEBUG: Starting Async email process for Order #" + orderId);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 4. Use the variable here!
            helper.setFrom(adminEmail);

            // You can also use the same variable for the "To" address
            // if you want to receive the notification at that same email
            helper.setTo("mhamedkbt@gmail.com");

            helper.setSubject("📦 New Order Received! #" + orderId);

            String htmlContent =
                    "<div style='font-family: Arial, sans-serif; padding: 20px;'>" +
                            "<h2>New Order Alert!</h2>" +
                            "<p>Order ID: #" + orderId + "</p>" +
                            "<p>Customer: " + customerName + "</p>" +
                            "<p>Total: " + totalAmount + " DH</p>" +
                            "</div>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✅ SUCCESS: Email sent using " + adminEmail);

        } catch (Exception e) {
            // 5. This will now definitely show up in Render logs if it fails
            System.err.println("❌ ERROR: Email failed for order " + orderId);
            System.err.println("Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }
}