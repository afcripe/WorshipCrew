package net.dahliasolutions.services;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.*;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.mail.AppEventService;
import net.dahliasolutions.services.mail.NotificationMessageService;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final NotificationService notificationService;
    private final AppEventService appEventService;
    private final NotificationMessageService notificationMessageService;

    public AppEvent createEvent(AppEvent dispatch) {
        List<Notification> notifyList = notificationService.findAllByModuleAndType(dispatch.getModule(), dispatch.getType());

        // find existing event or generate new id
        if (dispatch.getId() != null) {
            dispatch.setId(BigInteger.valueOf(Instant.now().toEpochMilli()));
            dispatch.setUsers(new ArrayList<>());
        } else {
            Optional<AppEvent> existingEvent = appEventService.findAppEventById(dispatch.getId());
            if (existingEvent.isEmpty()) {
                dispatch.setId(BigInteger.valueOf(Instant.now().toEpochMilli()));
                dispatch.setUsers(new ArrayList<>());
            } else {
                dispatch = existingEvent.get();
            }
        }

        NotificationMessage message = new NotificationMessage(
                null,
                dispatch.getName(),
                dispatch.getItemId(),
                BigInteger.valueOf(0),
                dispatch.getId(),
                false,
                false,
                null,
                dispatch.getModule(),
                dispatch.getType(),
                null,
                BigInteger.valueOf(0)
        );

        // ToDo - corporate blackout dates

        for (Notification notify : notifyList) {

            // ToDo - create new notificationMessage for each event and user
            // if not blackout, send notificationMessage
            // if blackout, mark sent and save for later viewing
            // maybe make missed event message that is scheduled

            for (User u : notify.getUsers()) {
                if (!dispatch.getUsers().contains(u)) {
                    NotificationMessage userMessage = notificationMessageService.createEventMessage(message, u);
                    dispatch.getUsers().add(u);
                }
            }
        }
        appEventService.save(dispatch);
        return dispatch;
    }

}
