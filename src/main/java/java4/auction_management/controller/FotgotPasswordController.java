package java4.auction_management.controller;

import java4.auction_management.entity.user.User;
import java4.auction_management.repository.IUserRepository;
import java4.auction_management.service.impl.UserNotFoundException;
import java4.auction_management.service.impl.UserService;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@Controller
public class FotgotPasswordController {
    // Display the form

    @Autowired
    private IUserRepository iUserRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService;



    @GetMapping("/forgot_password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("pageTitle", "Forgot Password");
        return "forgot_password_form";
    }

    @PostMapping("/forgot_password")
    public String processForgotPassword(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        String token = RandomString.make(45);

        System.out.println(email);
        System.out.println(token);

        try {
            userService.updateResetPasswordToken(token, email);
            String resetPasswordLink = Utility.getSiteURL(request) + "/reset_password?token=" + token;
            System.out.println(resetPasswordLink);
            sendEmail(email, resetPasswordLink);
            model.addAttribute("message", "We have sent a reset password link to your email. Please check.");

        }  catch (UserNotFoundException ex) {
            model.addAttribute("error", ex.getMessage());
        } catch (UnsupportedEncodingException | MessagingException e) {
            model.addAttribute("error", "Error while sending email");

        }

        return "forgot_password_form";
    }

    private void sendEmail(String email, String resetPasswordLink) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("websiteaution2022@gmail.com", "FPT Action");
        helper.setTo(email);

        String subject = "Here's the link to reset your password";
        String content = "<p>Hello,</p>"
                + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>"
                + "<p><a href=\"" + resetPasswordLink + "\">Change my password</a></p>"
                + "<br>"
                + "<p>Ignore this email if you do remember your password, "
                + "or you have not made the request.</p>";
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }


    @GetMapping("/reset_password")
    public String showResetPasswordForm(@Param(value = "token") String token, Model model) {
        User user = userService.getByResetPasswordToken(token);
        model.addAttribute("token", token);

        if (user == null) {
            model.addAttribute("message", "Invalid Token");
            return "message";
        }

        return "reset_password_form";
    }

    @PostMapping("/reset_password")
    public String processResetPassword(HttpServletRequest request, Model model) {
        String token = request.getParameter("token");
        String password = request.getParameter("password");

        User user = userService.getByResetPasswordToken(token);
        model.addAttribute("title", "Reset your password");

        if (user == null) {
            model.addAttribute("message", "Invalid Token");
            return "message";
        } else {
            userService.updatePassword(user, password);

            model.addAttribute("message", "Reset Password Successfully !");
        }

        return "message";
    }

}
