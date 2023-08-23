package net.dahliasolutions.data;

import net.dahliasolutions.models.support.SupportSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface SupportSettingRepository extends JpaRepository<SupportSetting, BigInteger> {
}
