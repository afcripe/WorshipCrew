package net.dahliasolutions.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/app")
public class AppAPIController {

    @GetMapping("/removeerrormsg")
    public void removeErrorMSG(HttpSession session) {
        try {
            session.removeAttribute("msgError");
            session.removeAttribute("msgSuccess");
        } catch (Exception e) {
            System.out.println("Attributes not removed.");
        }
    }

    @GetMapping("/removesuccessmsg")
    public void removeSuccessMSG(HttpSession session) {
        try {
            session.removeAttribute("msgError");
            session.removeAttribute("msgSuccess");
        } catch (Exception e) {
            System.out.println("Attributes not removed.");
        }
    }
}
