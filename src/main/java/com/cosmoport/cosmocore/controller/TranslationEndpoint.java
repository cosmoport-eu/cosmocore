package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.events.ReloadMessage;
import com.cosmoport.cosmocore.events.TimeoutUpdateMessage;
import com.cosmoport.cosmocore.model.I18NEntity;
import com.cosmoport.cosmocore.model.LocaleEntity;
import com.cosmoport.cosmocore.model.TranslationEntity;
import com.cosmoport.cosmocore.repository.I18NRepository;
import com.cosmoport.cosmocore.repository.LocaleRepository;
import com.cosmoport.cosmocore.repository.TranslationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/translations")
public class TranslationEndpoint {
    private final TranslationRepository translationRepository;
    private final I18NRepository i18NRepository;
    private final LocaleRepository localeRepository;
    private final ApplicationEventPublisher eventBus;

    public TranslationEndpoint(TranslationRepository translationRepository,
                               I18NRepository i18NRepository,
                               LocaleRepository localeRepository,
                               ApplicationEventPublisher eventBus) {
        this.translationRepository = translationRepository;
        this.i18NRepository = i18NRepository;
        this.localeRepository = localeRepository;
        this.eventBus = eventBus;
    }

    @PostMapping("/locale")
    public LocaleDto createLocale(@RequestBody LocaleDto locale) {
        final LocaleEntity localeEntity = new LocaleEntity();
        localeEntity.setDefault(false);
        localeEntity.setCode(locale.code());
        localeEntity.setShow(true);
        localeEntity.setLocaleDescription(locale.localeDescription());
        final LocaleEntity newLocale = localeRepository.save(localeEntity);

        final List<TranslationEntity> neDefaultTranslations = translationRepository.findAllByLocaleId(1).stream()
                .map(translationEntity -> {
                    final TranslationEntity newTranslation = new TranslationEntity();
                    newTranslation.setLocaleId(newLocale.getId());
                    newTranslation.setI18NId(translationEntity.getI18NId());
                    newTranslation.setTrText(translationEntity.getTrText());
                    return newTranslation;
                })
                .toList();

        translationRepository.saveAll(neDefaultTranslations);

        return new LocaleDto(newLocale.getId(), newLocale.getCode(), newLocale.isDefault(),
                newLocale.getLocaleDescription(), newLocale.isShow(), newLocale.getShowTime());
    }

    @PostMapping("/locale/show")
    public LocaleDto setLocaleShowData(@RequestBody LocaleDto locale) {
        return localeRepository.findById(locale.id())
                .map(localeEntity -> {
                    localeEntity.setShow(locale.show());
                    localeEntity.setShowTime(locale.showTime());
                    LocaleEntity newLocale = localeRepository.save(localeEntity);
                    eventBus.publishEvent(new TimeoutUpdateMessage(this));
                    return newLocale;
                })
                .map(localeEntity -> new LocaleDto(localeEntity.getId(), localeEntity.getCode(), localeEntity.isDefault(),
                        localeEntity.getLocaleDescription(), localeEntity.isShow(), localeEntity.getShowTime()))
                .orElseThrow();
    }


    @GetMapping("/locales/visible")
    public List<LocaleDto> getVisibleLocales() {
        return localeRepository.findAll().stream()
                .filter(LocaleEntity::isShow)
                .map(localeEntity -> new LocaleDto(localeEntity.getId(), localeEntity.getCode(), localeEntity.isDefault(),
                        localeEntity.getLocaleDescription(), true, localeEntity.getShowTime())).toList();
    }

    @GetMapping("/locales")
    public List<LocaleDto> getLocales() {
        return localeRepository.findAll().stream().map(localeEntity -> new LocaleDto(localeEntity.getId(), localeEntity.getCode(), localeEntity.isDefault(),
                localeEntity.getLocaleDescription(), localeEntity.isShow(), localeEntity.getShowTime())).toList();
    }

    @PostMapping("/update/{translationId}")
    public ResultDto updateTranslation(@PathVariable("translationId") int translationId,
                                       @RequestBody TextValueUpdateRequestDto requestDto) {
        return new ResultDto(translationRepository.findById(translationId).map(translationEntity -> {
            translationEntity.setTrText(requestDto.text());
            translationRepository.save(translationEntity);
            eventBus.publishEvent(new ReloadMessage(this));
            return true;
        }).orElse(false));
    }


    @GetMapping("/localeId={localeId}")
    public List<TranslationDto> getTranslations(@PathVariable("localeId") long localeId) {
        final Map<Integer, I18NEntity> i18NMap = i18NRepository.findAll().stream()
                .collect(Collectors.toMap(I18NEntity::getId, Function.identity()));
        return translationRepository.findAllByLocaleId(localeId).stream().map(translationEntity -> {
            final I18NEntity entity = i18NMap.get(translationEntity.getI18NId());
            return new TranslationDto(translationEntity.getId(), translationEntity.getI18NId(), translationEntity.getLocaleId(), translationEntity.getTrText(),
                    new I18nDto(entity.getId(), entity.getTag(), entity.isExternal(), entity.getDescription(), entity.getParams()));
        }).toList();
    }

    @GetMapping
    public Map<String, Map<String, TranslationLightDto>> get() {
        final Map<Integer, I18NEntity> i18nsByIdMap = i18NRepository.findAll().stream()
                .collect(Collectors.toMap(I18NEntity::getId, Function.identity()));

        final Map<Integer, LocaleEntity> localesByIdMap = localeRepository.findAll().stream()
                .collect(Collectors.toMap(LocaleEntity::getId, Function.identity()));

        final Map<String, Map<String, TranslationLightDto>> map = new LinkedHashMap<>();
        for (final TranslationEntity translation : translationRepository.findAll()) {
            final I18NEntity i18NEntity = i18nsByIdMap.get(translation.getI18NId());
            final TranslationLightDto lightDto = new TranslationLightDto(translation.getId(), getValues(translation, i18NEntity));
            final String locale = localesByIdMap.get(translation.getLocaleId()).getCode();
            final String translationKey = i18NEntity.isExternal() ? i18NEntity.getTag() : String.valueOf(i18NEntity.getId());
            map.computeIfAbsent(locale, k -> new HashMap<>()).put(translationKey, lightDto);
        }
        return map;
    }


    @SneakyThrows
    private List<String> getValues(final TranslationEntity translation, I18NEntity i18NEntity) {
        if ("json_array".equals(i18NEntity.getParams())) {
            return Arrays.asList(new ObjectMapper().readValue(translation.getTrText(), String[].class));
        } else {
            return Collections.singletonList(translation.getTrText());
        }
    }

    public record TranslationLightDto(long id, List<String> values) {
    }

    public record TranslationDto(long id, long i18nId, long localeId, String text, I18nDto i18n) {
    }

    public record I18nDto(long id, String tag, boolean external, String description, String params) {
    }

    public record TextValueUpdateRequestDto(String text) {
    }


    public record LocaleDto(int id, String code, boolean isDefault, String localeDescription, boolean show,
                            int showTime) {
    }
}
