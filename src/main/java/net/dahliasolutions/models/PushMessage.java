package net.dahliasolutions.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PushMessage {
    private String topicModule;
    private String topicId;
    private String topicContent;

}
