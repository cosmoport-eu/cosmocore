package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.controller.dto.TranslationDto;
import com.cosmoport.cosmocore.controller.helper.TranslationHelper;
import com.cosmoport.cosmocore.model.FacilityEntity;
import com.cosmoport.cosmocore.model.TranslationEntity;
import com.cosmoport.cosmocore.repository.FacilityRepository;
import com.cosmoport.cosmocore.repository.LocaleRepository;
import com.cosmoport.cosmocore.repository.TranslationRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/facility")
public class FacilityController {
    private static final String TEMP_CODE = "NEW_FACILITY_CODE";
    private static final String CODE_PREFIX = "facility_";

    private final FacilityRepository facilityRepository;
    private final LocaleRepository localeRepository;
    private final TranslationRepository translationRepository;

    public FacilityController(FacilityRepository facilityRepository,
                              LocaleRepository localeRepository,
                              TranslationRepository translationRepository) {
        this.facilityRepository = facilityRepository;
        this.localeRepository = localeRepository;
        this.translationRepository = translationRepository;
    }

    @PostMapping
    public ResultDto create(@RequestBody String name) {
        final FacilityEntity entity = new FacilityEntity();
        entity.setCode(TEMP_CODE);
        FacilityEntity savedEntity = facilityRepository.save(entity);
        savedEntity.setCode(CODE_PREFIX + savedEntity.getId());
        facilityRepository.save(savedEntity);

        translationRepository.saveAll(
                TranslationHelper.createTranslationForCodeAndDefaultText(localeRepository, savedEntity.getCode(), name)
        );

        return ResultDto.ok();
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResultDto delete(@PathVariable("id") int id) {
        facilityRepository.findById(id).ifPresentOrElse(facilityEntity -> {
            translationRepository.deleteAllByCode(facilityEntity.getCode());
            facilityRepository.deleteById(id);
        }, () -> {
            throw new IllegalArgumentException("Facility not found");
        });
        return ResultDto.ok();
    }

    @GetMapping
    public List<FacilityDto> getAll(@RequestParam("localeId") int localeId) {
        return facilityRepository.findAll().stream()
                .map(facilityEntity -> {
                    final TranslationEntity translation =
                            translationRepository.findByLocaleIdAndCode(localeId, facilityEntity.getCode()).orElseThrow();
                    return new FacilityDto(facilityEntity.getId(), facilityEntity.getCode(), translation.getText());
                }).toList();
    }

    @GetMapping("/all")
    public List<FacilityDtoWithTranslations> getAllWithTranslations() {
        return facilityRepository.findAll().stream()
                .map(entity ->
                        new FacilityDtoWithTranslations(entity.getId(), entity.getCode(),
                                TranslationHelper.getTranslationsByCode(translationRepository, entity.getCode()))).toList();
    }

    @Transactional
    @PutMapping("/{id}")
    @Operation(summary = "Update facility i18n code")
    public ResultDto update(@PathVariable("id") int id, @RequestBody String facilityCode) {
        facilityRepository.findById(id).ifPresentOrElse(facilityEntity -> {
            final List<TranslationEntity> translations = translationRepository.findAllByCode(facilityEntity.getCode());
            translations.forEach(translation -> translation.setCode(facilityCode));
            translationRepository.saveAll(translations);

            facilityEntity.setCode(facilityCode);
            facilityRepository.save(facilityEntity);
        }, () -> {
            throw new IllegalArgumentException("Facility not found");
        });
        return ResultDto.ok();
    }

    public record FacilityDto(int id, String code, String name) {
    }

    public record FacilityDtoWithTranslations(int id, String code, List<TranslationDto> translations) {
    }
}
