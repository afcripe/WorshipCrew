package net.dahliasolutions.services.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.SupportSettingRepository;
import net.dahliasolutions.models.NotifyTarget;
import net.dahliasolutions.models.support.SupportSetting;
import net.dahliasolutions.models.user.User;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class SupportSettingService implements SupportSettingServiceInterface {

    private final SupportSettingRepository supportSettingRepository;

    @Override
    public SupportSetting getSupportSetting() {
        SupportSetting supportSetting = supportSettingRepository.findById(BigInteger.valueOf(1)).orElse(null);
        if (supportSetting == null) {
            return supportSettingRepository.save(
                    new SupportSetting(
                            BigInteger.valueOf(1),
                            NotifyTarget.RegionalDepartmentDirector,
                            null)
            );
        }
        return supportSetting;
    }

    @Override
    public void setSupportNotifyTarget(NotifyTarget notifyTarget) {
        SupportSetting supportSetting = getSupportSetting();
                supportSetting.setNotifyTarget(notifyTarget);
        supportSettingRepository.save(supportSetting);
    }

    @Override
    public void setUser(User user) {
        SupportSetting supportSetting = getSupportSetting();
                supportSetting.setUser(user);
        supportSettingRepository.save(supportSetting);
    }

}
