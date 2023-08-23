package net.dahliasolutions.services.support;

import net.dahliasolutions.models.NotifyTarget;
import net.dahliasolutions.models.support.SupportSetting;
import net.dahliasolutions.models.user.User;

public interface SupportSettingServiceInterface {
    SupportSetting getSupportSetting();
    void setSupportNotifyTarget(NotifyTarget notifyTarget);
    void setUser(User user);

}
