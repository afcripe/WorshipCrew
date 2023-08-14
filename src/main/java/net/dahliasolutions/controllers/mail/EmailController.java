package net.dahliasolutions.controllers.mail;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.BrowserMessage;
import net.dahliasolutions.models.mail.EmailDetails;
import net.dahliasolutions.models.mail.MailerLinks;
import net.dahliasolutions.models.order.OrderItem;
import net.dahliasolutions.models.order.OrderNote;
import net.dahliasolutions.models.order.OrderRequest;
import net.dahliasolutions.models.order.OrderStatus;
import net.dahliasolutions.models.user.ChangePasswordModel;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.mail.EmailService;
import net.dahliasolutions.services.mail.MailerLinksService;
import net.dahliasolutions.services.order.OrderItemService;
import net.dahliasolutions.services.order.OrderNoteService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mailer")
public class EmailController {

    private final MailerLinksService mailerLinksService;
    private final UserService userService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderNoteService orderNoteService;
    private final EmailService emailService;

    @GetMapping("")
    public String testMailer(){
        return "documentation";
    }
    @GetMapping("/")
    public String testMailerTwo(){
        return "documentation";
    }

    @GetMapping("/{randomString}")
    public String getSetPasswordForm(@PathVariable String randomString,
                                     @RequestParam BigInteger eventId,
                                     @RequestParam String action,
                                     Model model) {
        MailerLinks mailerLinks = mailerLinksService.findByRandomLinkString(randomString).orElse(null);
        if (mailerLinks == null) {
            model.addAttribute("errorMessage", "Link not Valid!");
            return "error";
        }
        if (mailerLinks.getExpiration().isBefore(LocalDateTime.now()) || mailerLinks.isForceExpire()) {
            model.addAttribute("errorMessage", " The link has Expired!");
            return "error";
        }

        User user = userService.findById((mailerLinks.getUserId())).orElse(null);
        model.addAttribute("userDetails", user);
        model.addAttribute("randomString", randomString);

        switch (mailerLinks.getServiceMethod()) {
            case "setPassword":
            case "resetPassword":
                return "mailer/setPassword";
            case "acknowledgeRequest":
                updateOrderRequest(mailerLinks);
                return "mailer/acknowledge";
            case "acknowledgeItem":
                updateOrderRequestItem(mailerLinks);
                return "mailer/acknowledge";
            default:
                model.addAttribute("errorMessage", "Link not Valid!");
                return "error";
        }
    }

    @PostMapping("/setpassword")
    public String setNewPassword(@ModelAttribute ChangePasswordModel changePasswordModel, HttpSession session, Model model) {
        MailerLinks mailerLinks = mailerLinksService.findByRandomLinkString(changePasswordModel.currentPassword()).orElse(null);
        if (mailerLinks == null) {
            model.addAttribute("errorMessage", "Link not Valid!");
            return "error";
        }
        if (mailerLinks.getExpiration().isBefore(LocalDateTime.now()) || mailerLinks.isForceExpire()) {
            model.addAttribute("errorMessage", "Link has Expired!");
            return "error";
        }
        if (mailerLinks.getServiceMethod().equals("setPassword") || mailerLinks.getServiceMethod().equals("resetPassword")) {
            User user = userService.findById((mailerLinks.getUserId())).orElse(null);
            if(user == null){
                model.addAttribute("errorMessage", "User Not Found");
                return "error";
            }

            if (!changePasswordModel.newPassword().equals(changePasswordModel.confirmPassword())) {
                session.setAttribute("msgError", "Passwords Do Not Match!");
                return "redirect:/mailer/"+changePasswordModel.currentPassword();
            } else {
                userService.updateActivateUserPasswordById(user.getId(), changePasswordModel.newPassword());
                mailerLinks.setForceExpire(true);
                mailerLinksService.save(mailerLinks);
            }
            return "redirect:/login";
        }

        model.addAttribute("errorMessage", "Link not valid for setting password!");
        return "error";
    }

    private void updateOrderRequest(MailerLinks mailerLinks) {
        Optional<OrderRequest> orderRequest = orderService.findById(mailerLinks.getServiceId());
        Optional<User> supervisor = userService.findById(mailerLinks.getUserId());

        if (orderRequest.isPresent()) {
            User user = orderRequest.get().getUser();
            String statusNote = "Request Marked Received by "+supervisor.get().getFirstName()+" "+supervisor.get().getLastName();
            orderRequest.get().setOrderStatus(OrderStatus.Received);
            orderService.save(orderRequest.get());
            orderNoteService.createOrderNote(new OrderNote(
                    null,
                    orderRequest.get().getId(),
                    null,
                    statusNote,
                    BigInteger.valueOf(0),
                    orderRequest.get().getOrderStatus(),
                    supervisor.get()));

            EmailDetails emailDetailsUser =
                    new EmailDetails(user.getContactEmail(),"Your Request Has Been Received", "", null );
            BrowserMessage returnMsg = emailService.sendUserRequest(emailDetailsUser, orderRequest.get());
        }

        mailerLinks.setForceExpire(true);
        mailerLinksService.save(mailerLinks);
    }

    private void updateOrderRequestItem(MailerLinks mailerLinks) {
        Optional<OrderItem> requestItem = orderItemService.findById(mailerLinks.getServiceId());
        Optional<User> supervisor = userService.findById(mailerLinks.getUserId());

        if (requestItem.isPresent()) {
            User OrderSupervisor = requestItem.get().getOrderRequest().getSupervisor();
            String statusNote = "Request Item Marked Received by "+supervisor.get().getFirstName()+" "+supervisor.get().getLastName();
            // update item status
            requestItem.get().setItemStatus(OrderStatus.Received);
            orderItemService.save(requestItem.get());
            // add new note to order
            orderNoteService.createOrderNote(new OrderNote(
                    null,
                    requestItem.get().getOrderRequest().getId(),
                    null,
                    statusNote,
                    requestItem.get().getId(),
                    requestItem.get().getItemStatus(),
                    supervisor.get()));

            EmailDetails emailDetailsSupervisor =
                    new EmailDetails(OrderSupervisor.getContactEmail(),"Request Item was Received", "", null );
            BrowserMessage returnMsg2 = emailService.sendUserRequest(emailDetailsSupervisor, requestItem.get().getOrderRequest());
        }

        mailerLinks.setForceExpire(true);
        mailerLinksService.save(mailerLinks);
    }
}
