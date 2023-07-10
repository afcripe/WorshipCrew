package net.dahliasolutions.services;

import net.dahliasolutions.models.AdminSettings;

public interface AdminSettingsServiceInterface {
    AdminSettings getAdminSettings();
    void setCompanyName(String name);
    void setMonthlyStatements(boolean bool);
    void setAllowVolunteerRequests(boolean bool);
}
