package net.dahliasolutions.services.mail;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.CustomMessageRepository;
import net.dahliasolutions.models.mail.MailerCustomMessage;
import net.dahliasolutions.models.mail.MailerCustomMessageModel;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.user.UserService;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class MailerCustomMessageService implements MailerCustomMessageServiceInterface{

    private final CustomMessageRepository customMessageRepository;
    private final UserService userService;

    @Override
    public MailerCustomMessage save(MailerCustomMessage message) {
        return customMessageRepository.save(message);
    }

    @Override
    public Optional<MailerCustomMessage> findById(BigInteger id) {
        return customMessageRepository.findById(id);
    }

    @Override
    public List<MailerCustomMessage> findAll() {
        return customMessageRepository.findAll();
    }

    @Override
    public List<MailerCustomMessage> findAllByUser(User user) {
        return customMessageRepository.findByUser(user);
    }

    @Override
    public List<MailerCustomMessage> findByUserAndDraft(User user, boolean draft) {
        return customMessageRepository.findByUserAndDraft(user, draft);
    }

    @Override
    public MailerCustomMessageModel convertEntityToModel(MailerCustomMessage entity) {
        StringBuilder userIds = new StringBuilder();
        StringBuilder userNames = new StringBuilder();
        for (User u : entity.getToUsers()) {
            if (userIds.length() > 0) { userIds.append(", "); }
            userIds.append(u.getId().toString());
            if (userNames.length() > 0) { userNames.append(", "); }
            userNames.append(u.getFullName());
        }
        return new MailerCustomMessageModel(
                entity.getId(),
                entity.getSubject(),
                entity.isDraft(),
                entity.getMessageBody(),
                entity.getUser().getId(),
                userIds.toString(),
                userNames.toString()
        );
    }

    @Override
    public MailerCustomMessage convertModelToEntity(MailerCustomMessageModel model) {
        Optional<User> user = userService.findById(model.getUserId());
        List<User> toUsers = new ArrayList<>();

        List<String> userIds = Arrays.asList(model.getToUsersId().split(",\s"));
        for (String s : userIds) {
            if (!s.equals("")) {
                try {
                    int i = Integer.parseInt(s);
                    Optional<User> u = userService.findById(BigInteger.valueOf(i));
                    if (u.isPresent()) {
                        toUsers.add(u.get());
                    }
                } catch (Error e) {
                    System.out.println(e);
                }
            }
        }

        // force id of 0 to null
        BigInteger currentID = null;
        if (!model.getId().equals(BigInteger.valueOf(0))) { currentID=model.getId(); }

        return new MailerCustomMessage(
                currentID,
                model.getSubject(),
                model.isDraft(),
                model.getMessageBody(),
                user.get(),
                toUsers
        );
    }

    @Override
    public void deleteById(BigInteger id) {
        customMessageRepository.deleteById(id);
    }
}
