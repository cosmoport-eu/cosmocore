package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.model.MaterialEntity;
import com.cosmoport.cosmocore.model.TranslationEntity;
import com.cosmoport.cosmocore.repository.LocaleRepository;
import com.cosmoport.cosmocore.repository.MaterialRepository;
import com.cosmoport.cosmocore.repository.TranslationRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/material")
public class MaterialController {
    private static final String TEMP_CODE = "NEW_MATERIAL_CODE";
    private static final String CODE_PREFIX = "material_";

    private final MaterialRepository materialRepository;
    private final LocaleRepository localeRepository;
    private final TranslationRepository translationRepository;

    public MaterialController(MaterialRepository materialRepository,
                              LocaleRepository localeRepository,
                              TranslationRepository translationRepository) {
        this.materialRepository = materialRepository;
        this.localeRepository = localeRepository;
        this.translationRepository = translationRepository;
    }

    @PostMapping
    public ResultDto create(@RequestBody String facilityName) {
        final MaterialEntity entity = new MaterialEntity();
        entity.setCode(TEMP_CODE);
        MaterialEntity savedEntity = materialRepository.save(entity);
        savedEntity.setCode(CODE_PREFIX + savedEntity.getId());
        materialRepository.save(savedEntity);

        final List<TranslationEntity> newTranslations = localeRepository.findAll().stream()
                .map(localeEntity -> {
                    final TranslationEntity translation = new TranslationEntity();
                    translation.setLocaleId(localeEntity.getId());
                    translation.setCode(savedEntity.getCode());
                    translation.setText(facilityName);
                    return translation;
                }).toList();

        translationRepository.saveAll(newTranslations);
        return ResultDto.ok();
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResultDto delete(@PathVariable("id") int id) {
        materialRepository.findById(id).ifPresentOrElse(materialEntity -> {
            translationRepository.deleteAllByCode(materialEntity.getCode());
            materialRepository.deleteById(id);
        }, () -> {
            throw new IllegalArgumentException("Material not found");
        });
        return ResultDto.ok();
    }

    @GetMapping
    public List<MaterialDto> getAll(@RequestParam("localeId") int localeId) {
        return materialRepository.findAll().stream()
                .map(facilityEntity -> {
                    final TranslationEntity translation =
                            translationRepository.findByLocaleIdAndCode(localeId, facilityEntity.getCode()).orElseThrow();
                    return new MaterialDto(facilityEntity.getId(), facilityEntity.getCode(), translation.getText());
                }).toList();
    }

    @Transactional
    @PutMapping("/{id}")
    @Operation(summary = "Update i18n code")
    public ResultDto update(@PathVariable("id") int id, @RequestBody String materialCode) {
        materialRepository.findById(id).ifPresentOrElse(materialEntity -> {
            final List<TranslationEntity> translations = translationRepository.findAllByCode(materialEntity.getCode());
            translations.forEach(translation -> translation.setCode(materialCode));
            translationRepository.saveAll(translations);

            materialEntity.setCode(materialCode);
            materialRepository.save(materialEntity);
        }, () -> {
            throw new IllegalArgumentException("Facility not found");
        });
        return ResultDto.ok();
    }

    public record MaterialDto(int id, String code, String name) {
    }
}
