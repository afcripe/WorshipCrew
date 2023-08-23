package net.dahliasolutions.services.store;

import net.dahliasolutions.models.store.RequestNotifyTarget;
import net.dahliasolutions.models.store.StoreSetting;
import net.dahliasolutions.models.user.User;

public interface StoreSettingServiceInterface {
    StoreSetting getStoreSetting();
    void setStoreNotifyTarget(RequestNotifyTarget notifyTarget);
    void setUser(User user);

}
