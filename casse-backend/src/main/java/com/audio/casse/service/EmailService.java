package com.audio.casse.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${casse.approvers}")
    private String approverEmailsString;

    @Value("${casse.base-url}") // Inject the base URL from application.yaml
    private String baseUrl;

    public void sendApprovalEmail(String uploaderEmail, String songTitle, String storageAccessKey) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);

            // Split the comma-separated string into an array of emails
            String[] approverEmails = approverEmailsString.split(",");
            helper.setTo(approverEmails); // Set recipients

            helper.setSubject("New Song Approval Request: " + songTitle);

            String playDownloadLink = createPlayDownloadLink(uploaderEmail, songTitle, storageAccessKey);
            String approvalLink = createApprovalLink(uploaderEmail, songTitle);
            String denialLink = createDenialLink(uploaderEmail, songTitle); // New denial link

            String htmlContent = "<html>"
                    + "<body>"
                    + "<h2>New Song Approval Request</h2>"
                    + "<p>A new song titled <strong>" + songTitle + "</strong> has been uploaded by <strong>" + uploaderEmail + "</strong> and is awaiting your approval.</p>"
                    + "<p>You can try to download it <a href=\"" + playDownloadLink + "\">here</a>.</p>"
                    + "<p>To approve this song, please click here: <a href=\"" + approvalLink + "\">Approve Song</a></p>"
                    + "<p>To deny this song, please click here: <a href=\"" + denialLink + "\">Deny Song</a></p>" // Added denial link
                    + "<p>Please review and approve/reject as necessary.</p>"
                    + "<p>Thank you,</p>"
                    + "<p>Casse Team</p>"
                    + "</body>"
                    + "</html>";

            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Log the exception or handle it appropriately
            e.printStackTrace();
        }
    }

    public void sendSongApprovedEmail(String uploaderEmail, String songTitle) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(uploaderEmail);
            helper.setSubject("Your Song '" + songTitle + "' Has Been Approved!");

            String htmlContent = "<html>"
                    + "<body>"
                    + "<h2>Congratulations!</h2>"
                    + "<p>Your song <strong>" + songTitle + "</strong> has been approved and is now live on Casse.</p>"
                    + "<p>Congratulations and keep making good music!</p>"
                    + "<p>Thank you,</p>"
                    + "<p>Casse Team</p>"
                    + "</body>"
                    + "</html>";

            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendSongDeniedEmail(String uploaderEmail, String songTitle) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(uploaderEmail);
            helper.setSubject("Your Song '" + songTitle + "' Has Been Denied");

            String htmlContent = "<html>"
                    + "<body>"
                    + "<h2>Regarding Your Song Submission</h2>"
                    + "<p>We regret to inform you that your song <strong>" + songTitle + "</strong> has been denied.</p>"
                    + "<p>Please review our submission guidelines and try again with a different song.</p>"
                    + "<p>Thank you,</p>"
                    + "<p>Casse Team</p>"
                    + "</body>"
                    + "</html>";

            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String createPlayDownloadLink(String uploaderEmail, String songTitle, String storageAccessKey) {
        return baseUrl + "/audio/stream?email=" + uploaderEmail + "&title=" + songTitle + "&key=" + storageAccessKey;
    }

    private String createApprovalLink(String uploaderEmail, String songTitle) {
        return baseUrl + "/audio/approve?email=" + uploaderEmail + "&title=" + songTitle;
    }

    private String createDenialLink(String uploaderEmail, String songTitle) {
        return baseUrl + "/audio/deny?email=" + uploaderEmail + "&title=" + songTitle;
    }
}
