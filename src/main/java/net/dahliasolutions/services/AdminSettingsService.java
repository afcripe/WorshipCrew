package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.AdminSettingsRepository;
import net.dahliasolutions.models.AdminSettings;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class AdminSettingsService implements AdminSettingsServiceInterface{

    private final AdminSettingsRepository adminSettingsRepository;

    @Override
    public AdminSettings getAdminSettings() {
        AdminSettings adminSettings = adminSettingsRepository.findById(BigInteger.valueOf(1)).orElse(null);
        if (adminSettings == null) {
            return adminSettingsRepository.save(
                    new AdminSettings(
                            BigInteger.valueOf(1),
                            "Destiny Worship Exchange",
                            false,
                            true)
            );
        }
        return adminSettings;
    }

    @Override
    public void setCompanyName(String name) {
        AdminSettings adminSettings = getAdminSettings();
        adminSettings.setCompanyName(name);
        adminSettingsRepository.save(adminSettings);
    }

    @Override
    public void setMonthlyStatements(boolean bool) {
        AdminSettings adminSettings = getAdminSettings();
        adminSettings.setMonthlyStatements(bool);
        adminSettingsRepository.save(adminSettings);
    }

    @Override
    public void setAllowVolunteerRequests(boolean bool) {
        AdminSettings adminSettings = getAdminSettings();
        adminSettings.setAllowVolunteerRequests(bool);
        adminSettingsRepository.save(adminSettings);
    }

}
