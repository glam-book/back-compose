package com.tlback.domain.model.contact;

import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;

/**
 * Представляет собой телефонный номер с удобными методами для работы и
 * валидации.
 * Хранит код страны, код региона/оператора и номер.
 * Позволяет извлекать "сырой" номер без специальных символов.
 */
@Data
@Builder
public class PhoneNumber {

    private static final Pattern VALID_NUMBER_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("\\D");

    @Nonnull
    private String rawNumber; // Например, "+7 (999) 123-45-67", "89991234567", "+1-234-567-8900"

    // Опциональные разобранные компоненты
    private Integer countryCode; // Например, 7 для России
    private String regionCode; // Например, "999" для московского номера
    private String subscriberNumber; // Например, "1234567" для московского номера

    /**
     * Создает экземпляр PhoneNumber из "сырого" строкового представления.
     * 
     * @param rawNumber Строка с номером, например, "+7 (999) 123-45-67".
     * @return Новый экземпляр PhoneNumber.
     */
    public static PhoneNumber fromRawNumber(@Nonnull String rawNumber) {
        return PhoneNumber.builder()
                .rawNumber(rawNumber)
                .build();
    }

    /**
     * Создает экземпляр PhoneNumber из составных числовых компонентов.
     * Полезно, если код страны, региона и номер абонента уже известны отдельно.
     * 
     * @param countryCode      Код страны (например, 7).
     * @param regionCode       Код региона или оператора (например, "999").
     * @param subscriberNumber Номер абонента (например, "1234567").
     * @return Новый экземпляр PhoneNumber.
     */
    public static PhoneNumber fromComponents(Integer countryCode, String regionCode, String subscriberNumber) {
        StringBuilder sb = new StringBuilder();
        if (countryCode != null && countryCode > 0) {
            sb.append('+').append(countryCode);
        }
        if (regionCode != null) {
            sb.append(regionCode);
        }
        if (subscriberNumber != null) {
            sb.append(subscriberNumber);
        }
        String rawNumber = sb.toString();

        return PhoneNumber.builder()
                .rawNumber(rawNumber)
                .countryCode(countryCode)
                .regionCode(regionCode)
                .subscriberNumber(subscriberNumber)
                .build();
    }

    /**
     * Извлекает из rawNumber только цифры.
     * 
     * @return Строка, содержащая только цифры номера. Например, "79991234567".
     */
    public String getDigitsOnly() {
        if (rawNumber == null) {
            return null;
        }
        return DIGITS_ONLY_PATTERN.matcher(rawNumber).replaceAll("");
    }

    /**
     * Валидирует номер по общему международному формату E.164.
     * Предполагает, что номер начинается с '+' и содержит от 1 до 15 цифр.
     * 
     * @return true, если номер соответствует базовому формату E.164.
     */
    public boolean isValidFormat() {
        String digits = this.getDigitsOnly();
        if (StringUtils.isBlank(digits)) {
            return false;
        }
        // Убедимся, что номер начинается с + или без него, но начинается с цифры 1-9
        // и содержит от 4 до 15 цифр (после извлечения всех нецифр)
        // Это базовая проверка, можно усложнить для конкретных стран
        return VALID_NUMBER_PATTERN.matcher(digits).matches();
    }

    /**
     * Пытается разобрать номер на составляющие: код страны, регион/оператор, номер
     * абонента.
     * Внимание: Это базовый парсер. Для точной логики разбора по странам и
     * операторам
     * рекомендуется использовать специализированные библиотеки, например,
     * libphonenumber от Google.
     * 
     * @return this (для цепочки вызовов), обновляя поля countryCode, regionCode,
     *         subscriberNumber.
     */
    public PhoneNumber parseComponents() {
        String digits = this.getDigitsOnly();
        if (StringUtils.isBlank(digits)) {
            this.countryCode = null;
            this.regionCode = null;
            this.subscriberNumber = null;
            return this;
        }

        // Пример базовой логики (на примере России и США, можно расширить)
        // Для +79991234567 длина 11, код страны 7 (1 цифра), регион 3 цифры (999),
        // остальное - номер
        // Для +12345678900 длина 11, код страны 1 (1 цифра), регион 3 цифры (234),
        // остальное - номер
        // Общее правило: если длина > 10, первая цифра - код страны
        // Если длина == 10 (например, в США без +1), можно предположить код страны 1,
        // но это хрупко.
        // Для простоты, будем считать, что если длина > 10, первые 1-2 цифры - код
        // страны.
        // Это не всегда верно для всех стран, но показывает подход.

        Integer parsedCountryCode = null;
        String parsedRegionCode = null;
        String parsedSubscriberNumber = null;

        if (digits.length() > 10) { // Общее правило, работает для многих стран
            if (digits.startsWith("1")) { // США, Канада и др.
                parsedCountryCode = 1;
                if (digits.length() >= 11) {
                    parsedRegionCode = digits.substring(1, 4); // 234 из +1-234-567-8900
                    parsedSubscriberNumber = digits.substring(4); // 5678900
                }
            } else if (digits.startsWith("7")) { // Россия, Казахстан
                parsedCountryCode = 7;
                if (digits.length() >= 11) {
                    parsedRegionCode = digits.substring(1, 4); // 999 из +7-999-123-45-67
                    parsedSubscriberNumber = digits.substring(4); // 1234567
                }
            } else if (digits.startsWith("380")) { // Украина
                parsedCountryCode = 380;
                if (digits.length() >= 12) {
                    parsedRegionCode = digits.substring(3, 5); // 50 из +380-50-123-45-67
                    parsedSubscriberNumber = digits.substring(5); // 1234567
                }
            } else {
                // Более общий случай: 1 или 2 цифры код страны
                int ccLen = 1;
                String ccCandidate = digits.substring(0, ccLen);
                if (!ccCandidate.equals("0")) { // Не начинается с 0
                    try {
                        parsedCountryCode = Integer.parseInt(ccCandidate);
                        if (digits.length() >= ccLen + 3) {
                            parsedRegionCode = digits.substring(ccLen, ccLen + 3);
                            parsedSubscriberNumber = digits.substring(ccLen + 3);
                        }
                    } catch (NumberFormatException e) {
                        // Если не удалось распарсить код страны, оставляем как есть
                        // или можно добавить логику для 2-значных кодов
                        // Пример 2-значного: +33 1 23 45 67 89 (Франция)
                        ccLen = 2;
                        if (digits.length() >= ccLen + 3) {
                            ccCandidate = digits.substring(0, ccLen);
                            try {
                                parsedCountryCode = Integer.parseInt(ccCandidate);
                                parsedRegionCode = digits.substring(ccLen, ccLen + 3);
                                parsedSubscriberNumber = digits.substring(ccLen + 3);
                            } catch (NumberFormatException e2) {
                                // Не удалось распознать, оставляем null
                            }
                        }
                    }
                }
            }
        } else {
            // Если номер короче 10, предполагаем, что он введен без кода страны
            // и оставляем countryCode как null или можно попытаться использовать дефолтный.
            // В этом примере оставляем как null.
            parsedSubscriberNumber = digits;
        }

        this.countryCode = parsedCountryCode;
        this.regionCode = parsedRegionCode;
        this.subscriberNumber = parsedSubscriberNumber;

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PhoneNumber that = (PhoneNumber) o;
        // Сравниваем по rawNumber, так как это основное представление
        return Objects.equals(getDigitsOnly(), that.getDigitsOnly());
    }

    @Override
    public int hashCode() {
        // Хэш-код также строим по нормализованному (только цифры) значению
        return Objects.hash(getDigitsOnly());
    }

    public String pretty() {
        // Если компоненты не были разобраны, возвращаем сырой номер как есть
        if (this.countryCode == null && this.regionCode == null && this.subscriberNumber == null) {
            return this.rawNumber;
        }

        StringBuilder formatted = new StringBuilder();

        // Добавляем код страны, если он есть
        if (this.countryCode != null) {
            formatted.append('+').append(this.countryCode);
        }

        // Добавляем код региона/оператора, если он есть
        if (this.regionCode != null) {
            if (formatted.length() > 0) {
                formatted.append(' ');
            }
            formatted.append('(').append(this.regionCode).append(')');
        }

        // Добавляем номер абонента, если он есть
        if (this.subscriberNumber != null) {
            if (formatted.length() > 0) {
                formatted.append(' ');
            }
            // Попробуем красиво разбить subscriberNumber на части
            // Обычно разбиение идет по 2-3-2-2 или 3-3-2-2 для России (например, 123-45-67)
            // или 3-3-3-4 для США (например, 555-123-4567)
            // Это базовая логика, можно усложнить в зависимости от страны и длины номера
            String subNum = this.subscriberNumber;
            int len = subNum.length();

            if (len >= 5) { // Для номеров 5+ символов применяем форматирование
                // Пример: для 7 цифр (1234567) -> 123-45-67
                // Пример: для 10 цифр (5551234567) -> 555-123-4567
                // Попробуем разбить на последние 4 и остальные
                String prefix = subNum.substring(0, len - 4);
                String suffix = subNum.substring(len - 4);

                // Далее разбиваем префикс
                StringBuilder prefixBuilder = new StringBuilder();
                int prefixLen = prefix.length();
                if (prefixLen > 3) {
                    // Или 123456 -> 123 456 -> 123-456
                    int firstPartLen = prefixLen % 3 == 0 ? 3 : prefixLen % 3; // 12 для 12345
                    prefixBuilder.append(prefix, 0, firstPartLen); // "12"
                    for (int i = firstPartLen; i < prefixLen; i += 3) {
                        prefixBuilder.append('-').append(prefix, i, Math.min(i + 3, prefixLen)); // "-345"
                    }
                } else {
                    prefixBuilder.append(prefix); // Если короче 4, просто добавляем как есть
                }

                formatted.append(prefixBuilder).append('-').append(suffix);
            } else {
                // Если короче 5, просто добавляем как есть
                formatted.append(subNum);
            }
        }

        return formatted.toString();
    } // Если префикс длинный, разбиваем его на группы по 3, начиная с конца
      // Например, 12345 -> 12 345 -> 12-345

    @Override
    public String toString() {
        return "PhoneNumber{" +
                "rawNumber='" + rawNumber + '\'' +
                ", countryCode=" + countryCode +
                ", regionCode='" + regionCode + '\'' +
                ", subscriberNumber='" + subscriberNumber + '\'' +
                ", digitsOnly='" + getDigitsOnly() + '\'' +
                '}';
    }
}