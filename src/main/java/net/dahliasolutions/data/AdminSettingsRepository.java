package net.dahliasolutions.data;

import net.dahliasolutions.models.AdminSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface AdminSettingsRepository extends JpaRepository<AdminSettings, BigInteger> {
}
