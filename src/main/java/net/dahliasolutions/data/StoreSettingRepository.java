package net.dahliasolutions.data;

import net.dahliasolutions.models.store.StoreSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface StoreSettingRepository extends JpaRepository<StoreSetting, BigInteger> {
}
