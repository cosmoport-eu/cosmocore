package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.controller.dto.TranslationDto;
import com.cosmoport.cosmocore.controller.helper.TranslationHelper;
import com.cosmoport.cosmocore.events.ReloadMessage;
import com.cosmoport.cosmocore.model.TranslationEntity;
import com.cosmoport.cosmocore.repository.GateRepository;
import com.cosmoport.cosmocore.repository.TranslationRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gates")
public class GateEndpoint {

    private final GateRepository gateRepository;
    private final TranslationRepository translationRepository;
    private final ApplicationEventPublisher eventBus;

    public GateEndpoint(GateRepository gateRepository,
                        TranslationRepository translationRepository,
                        ApplicationEventPublisher eventBus) {
        this.gateRepository = gateRepository;
        this.translationRepository = translationRepository;
        this.eventBus = eventBus;
    }

    @GetMapping
    @Operation(summary = "Получить все ворота со всеми переводами")
    public List<GateDto> getAll() {
        return gateRepository.findAll().stream()
                .map(gateEntity -> new GateDto(
                                gateEntity.getId(),
                                gateEntity.getCode(),
                                TranslationHelper.getTranslationsByCode(translationRepository, gateEntity.getCode())
                        )
                )
                .toList();
    }

    @GetMapping("/locale/{localeId}")
    @Operation(summary = "Получить все ворота с текстом для указанной локали")
    public List<GateDtoWithText> getAllWithText(@PathVariable int localeId) {
        return gateRepository.findAll().stream()
                .map(gateEntity -> {
                    final TranslationEntity translation = translationRepository.findByLocaleIdAndCode(localeId, gateEntity.getCode())
                            .orElseThrow(() -> new IllegalStateException("No translation for gate code " + gateEntity.getCode() + " and locale " + localeId));
                    return new GateDtoWithText(
                            gateEntity.getId(),
                            translation.getId(),
                            gateEntity.getCode(),
                            translation.getText()
                    );
                })
                .toList();
    }

    @PutMapping("/{translationId}")
    @Operation(summary = "Обновить название ворот по id перевода")
    public ResultDto updateText(@PathVariable int translationId, @RequestBody String text) {
        translationRepository.findById(translationId)
                .ifPresentOrElse(
                        translationEntity -> {
                            translationEntity.setText(text);
                            translationRepository.save(translationEntity);
                            eventBus.publishEvent(new ReloadMessage(this));
                        },
                        () -> {
                            throw new IllegalStateException("No translation with id " + translationId);
                        }
                );
        return ResultDto.ok();
    }

    @Transactional
    @PutMapping("/code/{gateId}")
    @Operation(summary = "Обновить кодовое обозначение ворот")
    public ResultDto updateCode(@PathVariable int gateId, @RequestBody String text) {
        gateRepository.findById(gateId)
                .ifPresentOrElse(
                        gateEntity -> {
                            final List<TranslationEntity> translations =
                                    translationRepository.findAllByCode(gateEntity.getCode());
                            translations.forEach(translationEntity -> translationEntity.setCode(text));
                            gateEntity.setCode(text);

                            gateRepository.save(gateEntity);
                            translationRepository.saveAll(translations);
                        },
                        () -> {
                            throw new IllegalStateException("No gate with id " + gateId);
                        }
                );

        eventBus.publishEvent(new ReloadMessage(this));
        return ResultDto.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить ворота по id")
    public ResultDto delete(@PathVariable int id) {
        eventBus.publishEvent(new ReloadMessage(this));
        gateRepository.deleteById(id);
        return ResultDto.ok();
    }

    public record GateDto(long id, String code, List<TranslationDto> translations) {
    }

    public record GateDtoWithText(long id, int translationId, String code, String text) {
    }
}
