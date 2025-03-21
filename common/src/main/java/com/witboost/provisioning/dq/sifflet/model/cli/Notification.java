package com.witboost.provisioning.dq.sifflet.model.cli;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Getter
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "kind",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Notification.Email.class, name = "Email"),
})
public class Notification {

    private final String kind;

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @ToString
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Email extends Notification {
        private final String name;

        @JsonCreator
        public Email(@JsonProperty("name") String name) {
            super("Email");
            this.name = name;
        }
    }
}
