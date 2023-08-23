package net.dahliasolutions.services.support;

import net.dahliasolutions.models.store.RequestNotifyTarget;
import net.dahliasolutions.models.support.SupportSetting;
import net.dahliasolutions.models.support.TicketNotifyTarget;
import net.dahliasolutions.models.user.User;

import java.math.BigInteger;

public interface SupportSettingServiceInterface {
    SupportSetting getSupportSetting();
    void setResponseHours(int hours);
    void setDefaultSLAId(BigInteger id);

    void setSupportNotifyTarget(TicketNotifyTarget notifyTarget);
    void setUser(User user);

}
