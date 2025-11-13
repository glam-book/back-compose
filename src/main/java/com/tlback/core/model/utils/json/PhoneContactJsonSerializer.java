package com.tlback.core.model.utils.json;

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.tlback.core.model.contact.PhoneContact;

// Используем @JsonComponent - это удобный способ для Spring Boot
// зарегистрировать кастомные сериализаторы/десериализаторы
@JsonComponent
public class PhoneContactJsonSerializer {

    public static class ContactSerializer extends JsonSerializer<PhoneContact> {

        @Override
        public void serialize(PhoneContact value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            // Вызываем метод pretty() у phoneNumber и записываем результат
            gen.writeStringField("phoneNumber", value.getPhoneNumber().pretty());
            gen.writeEndObject();
        }
    }
}
