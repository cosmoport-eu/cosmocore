package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.model.MaterialEntity;
import com.cosmoport.cosmocore.model.TranslationEntity;
import com.cosmoport.cosmocore.repository.LocaleRepository;
import com.cosmoport.cosmocore.repository.MaterialRepository;
import com.cosmoport.cosmocore.repository.TranslationRepository;
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
    public void create(@RequestBody String facilityName) {
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
    }

    @Transactional
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") int id) {
        materialRepository.findById(id).ifPresentOrElse(materialEntity -> {
            translationRepository.deleteAllByCode(materialEntity.getCode());
            materialRepository.deleteById(id);
        }, () -> {
            throw new IllegalArgumentException("Material not found");
        });
    }
}
