package com.ecommerce.backend.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendOrderNotification(String customerName, double totalAmount, Long orderId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // IMPORTANT: This must be the email you verified in Brevo
            helper.setFrom("mhameddev1@gmail.com");
            helper.setTo("mhamedkbt@gmail.com");
            helper.setSubject("📦 New Order Received! #" + orderId);

            String htmlContent =
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; padding: 20px; border-radius: 10px;'>" +
                            "   <h2 style='color: #4F46E5; text-align: center;'>New Order Alert!</h2>" +
                            "   <p style='font-size: 16px;'>Hello Admin,</p>" +
                            "   <p style='font-size: 14px;'>A new order has been placed on your store.</p>" +
                            "   <div style='background-color: #f9fafb; padding: 15px; border-radius: 8px; margin: 20px 0;'>" +
                            "       <p><strong>Order ID:</strong> #" + orderId + "</p>" +
                            "       <p><strong>Customer:</strong> " + customerName + "</p>" +
                            "       <p style='font-size: 18px; color: #059669;'><strong>Total Amount: " + totalAmount + " DH</strong></p>" +
                            "   </div>" +
                            "   <p style='text-align: center; margin-top: 30px;'>" +
                            "       <a href='https://eccomstandard.vercel.app/dashboard' style='background-color: #4F46E5; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Open Admin Panel</a>" +
                            "   </p>" +
                            "   <p style='font-size: 12px; color: #9ca3af; text-align: center; margin-top: 20px;'>Standard Ecommerce System</p>" +
                            "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("✅ SUCCESS: Order Email sent to Admin!");

        } catch (Exception e) {
            System.err.println("❌ ERROR: Email failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}