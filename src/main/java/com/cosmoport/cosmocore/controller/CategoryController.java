package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.controller.dto.TranslationDto;
import com.cosmoport.cosmocore.controller.helper.TranslationHelper;
import com.cosmoport.cosmocore.model.EventTypeCategoryEntity;
import com.cosmoport.cosmocore.model.TranslationEntity;
import com.cosmoport.cosmocore.repository.EventTypeCategoryRepository;
import com.cosmoport.cosmocore.repository.LocaleRepository;
import com.cosmoport.cosmocore.repository.TranslationRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    private static final String TEMP_CODE = "NEW_CATEGORY_CODE";
    private static final String CODE_PREFIX = "event_type_category_";
    private final EventTypeCategoryRepository categoryRepository;
    private final TranslationRepository translationRepository;
    private final LocaleRepository localeRepository;

    public CategoryController(EventTypeCategoryRepository categoryRepository,
                              TranslationRepository translationRepository,
                              LocaleRepository localeRepository) {
        this.categoryRepository = categoryRepository;
        this.translationRepository = translationRepository;
        this.localeRepository = localeRepository;
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Создает новую категорию с названием по умолчанию для всех языков")
    public ResultDto create(@RequestBody CreateCategoryDto dto) {
        final EventTypeCategoryEntity entity = new EventTypeCategoryEntity();
        entity.setCode(TEMP_CODE);
        entity.setParent(dto.parentId());
        entity.setColor(dto.color());
        final EventTypeCategoryEntity newCategory = categoryRepository.save(entity);
        newCategory.setCode(CODE_PREFIX + newCategory.getId());
        if (categoryRepository.existsByCode(newCategory.getCode())) {
            throw new IllegalStateException("Category with code is already present: " + newCategory.getCode());
        }
        categoryRepository.save(newCategory);

        translationRepository.saveAll(
                TranslationHelper.createTranslationForCodeAndDefaultText(localeRepository, newCategory.getCode(), dto.name())
        );

        return ResultDto.ok();
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResultDto delete(@PathVariable("id") int id) {
        categoryRepository.findById(id).ifPresentOrElse(entity -> {
            translationRepository.deleteAllByCode(entity.getCode());
            categoryRepository.deleteById(id);
        }, () -> {
            throw new IllegalArgumentException("Not found");
        });
        return ResultDto.ok();
    }

    @GetMapping
    public List<CategoryDto> getAll(@RequestParam("localeId") int localeId) {
        return categoryRepository.findAll().stream()
                .map(entity -> {
                    final TranslationEntity translation =
                            translationRepository.findByLocaleIdAndCode(localeId, entity.getCode()).orElseThrow();
                    return new CategoryDto(
                            entity.getId(),
                            entity.getCode(),
                            translation.getText(),
                            entity.getColor(),
                            entity.getParent()
                    );
                }).toList();
    }

    @GetMapping("/all")
    public List<CategoryTranslationsDto> getAllWithTranslations() {
        return categoryRepository.findAll().stream()
                .map(entity ->
                        new CategoryTranslationsDto(entity.getId(), entity.getCode(), entity.getColor(), entity.getParent(),
                                TranslationHelper.getTranslationsByCode(translationRepository, entity.getCode()))).toList();
    }

    @Transactional
    @PutMapping("/{id}")
    @Operation(summary = "Update i18n code")
    public ResultDto update(@PathVariable("id") int id, @RequestBody String code) {
        categoryRepository.findById(id).ifPresentOrElse(materialEntity -> {
            final List<TranslationEntity> translations = translationRepository.findAllByCode(materialEntity.getCode());
            translations.forEach(translation -> translation.setCode(code));
            translationRepository.saveAll(translations);

            materialEntity.setCode(code);
            categoryRepository.save(materialEntity);
        }, () -> {
            throw new IllegalArgumentException("Facility not found");
        });
        return ResultDto.ok();
    }

    public record CreateCategoryDto(String name, String color, Integer parentId) {
    }

    public record CategoryDto(int id, String code, String name, String color, Integer parent) {
    }

    public record CategoryTranslationsDto(int id, String code, String color, Integer parent,
                                          List<TranslationDto> translations) {
    }
}