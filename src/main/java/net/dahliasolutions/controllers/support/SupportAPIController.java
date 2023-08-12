package net.dahliasolutions.controllers.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.support.Ticket;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportAPIController {

    @GetMapping("")
    public List<Ticket> getSupportTickets() {
        return null;
    }
}
