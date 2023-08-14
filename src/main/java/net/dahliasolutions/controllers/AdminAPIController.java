package net.dahliasolutions.controllers;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.records.SingleStringModel;
import net.dahliasolutions.services.AdminSettingsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminAPIController {

    private final AdminSettingsService adminSettingsService;

    @PostMapping("/update/{prop}")
    public void updateAdminSettings(@PathVariable String prop, @RequestBody SingleStringModel singleStringModel) {
        if (prop.equals("companyName")) {
            adminSettingsService.setCompanyName(singleStringModel.name());
        }
    }

}
