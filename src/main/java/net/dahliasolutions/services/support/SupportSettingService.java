package net.dahliasolutions.services.support;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.SupportSettingRepository;
import net.dahliasolutions.models.store.RequestNotifyTarget;
import net.dahliasolutions.models.support.SupportSetting;
import net.dahliasolutions.models.support.TicketNotifyTarget;
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
                            24,
                            BigInteger.valueOf(0),
                            TicketNotifyTarget.RegionalDepartmentDirector,
                            null)
            );
        }
        return supportSetting;
    }

    @Override
    public void setResponseHours(int hours) {
        SupportSetting supportSetting = getSupportSetting();
            supportSetting.setResponseHours(hours);
        supportSettingRepository.save(supportSetting);
    }

    @Override
    public void setDefaultSLAId(BigInteger id) {
        SupportSetting supportSetting = getSupportSetting();
            supportSetting.setDefaultSLAId(id);
        supportSettingRepository.save(supportSetting);
    }

    @Override
    public void setSupportNotifyTarget(TicketNotifyTarget notifyTarget) {
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
