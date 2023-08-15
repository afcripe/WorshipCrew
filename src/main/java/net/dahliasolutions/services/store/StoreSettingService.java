package net.dahliasolutions.services.store;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.AdminSettingsRepository;
import net.dahliasolutions.data.StoreSettingRepository;
import net.dahliasolutions.models.AdminSettings;
import net.dahliasolutions.models.store.StoreNotifyTarget;
import net.dahliasolutions.models.store.StoreSetting;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.AdminSettingsServiceInterface;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class StoreSettingService implements StoreSettingServiceInterface {

    private final StoreSettingRepository storeSettingRepository;

    @Override
    public StoreSetting getStoreSetting() {
        StoreSetting storeSetting = storeSettingRepository.findById(BigInteger.valueOf(1)).orElse(null);
        if (storeSetting == null) {
            return storeSettingRepository.save(
                    new StoreSetting(
                            BigInteger.valueOf(1),
                            StoreNotifyTarget.RegionalDepartmentDirector,
                            null)
            );
        }
        return storeSetting;
    }

    @Override
    public void setStoreNotifyTarget(StoreNotifyTarget notifyTarget) {
        StoreSetting storeSetting = getStoreSetting();
                    storeSetting.setNotifyTarget(notifyTarget);
        storeSettingRepository.save(storeSetting);
    }

    @Override
    public void setUser(User user) {
        StoreSetting storeSetting = getStoreSetting();
                    storeSetting.setUser(user);
        storeSettingRepository.save(storeSetting);
    }

}
