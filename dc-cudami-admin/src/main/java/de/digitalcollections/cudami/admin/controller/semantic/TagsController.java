package de.digitalcollections.cudami.admin.controller.semantic;

import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.semantic.CudamiTagsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.semantic.Tag;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** Controller for headwords management pages. */
@Controller
public class TagsController extends AbstractPagingAndSortingController<Tag> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TagsController.class);
  private final CudamiLocalesClient localeService;
  private final CudamiTagsClient service;

  public TagsController(CudamiClient client) {
    this.localeService = client.forLocales();
    this.service = client.forTags();
  }

  @GetMapping("/tags/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    return "tags/create";
  }

  @GetMapping("/tags/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Tag tag = service.getByUuid(uuid);
    if (tag == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("tag", tag);
    return "tags/edit";
  }

  @GetMapping("/tags")
  public String list(Model model) throws TechnicalException {
    return "tags/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "tags";
  }

  @GetMapping("/tags/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    Tag tag = service.getByUuid(uuid);
    if (tag == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("tag", tag);
    return "tags/view";
  }
}
