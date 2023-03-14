package de.digitalcollections.cudami.admin.controller.identifiable.entity.work;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.entity.AbstractEntitiesController;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.work.CudamiManifestationsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** Controller for manifestation management pages. */
@Controller
public class ManifestationsController
    extends AbstractEntitiesController<Manifestation, CudamiManifestationsClient> {

  public ManifestationsController(LanguageService languageService, CudamiClient client) {
    super(client.forManifestations(), languageService, client.forLocales());
  }

  @GetMapping("/manifestations")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, localeService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "manifestations/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "manifestations";
  }

  @GetMapping("/manifestations/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Manifestation manifestation = service.getByUuid(uuid);
    if (manifestation == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("manifestation", manifestation);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(manifestation);
    String dataLanguage = getDataLanguage(targetDataLanguage, localeService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    Locale displayLocale = LocaleContextHolder.getLocale();
    List<Locale> existingItemsLanguages =
        languageService.sortLanguages(displayLocale, service.getLanguagesOfItems(uuid));
    model
        .addAttribute("existingItemsLanguages", existingItemsLanguages)
        .addAttribute("dataLanguageItems", getDataLanguage(null, localeService));

    return "manifestations/view";
  }
}
