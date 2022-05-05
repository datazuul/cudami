package de.digitalcollections.cudami.server.controller.identifiable;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Identifiable controller V5")
public class V5IdentifiableController {

  private final IdentifiableService identifiableService;
  private final UrlAliasService urlAliasService;

  public V5IdentifiableController(
      @Qualifier("identifiableService") IdentifiableService identifiableService,
      UrlAliasService urlAliasService) {
    this.identifiableService = identifiableService;
    this.urlAliasService = urlAliasService;
  }

  @Operation(
      summary =
          "Find limited amount of identifiables containing searchTerm in label or description")
  @GetMapping(
      value = {
        "/v5/identifiables/search",
        "/v2/identifiables/search",
        "/latest/identifiables/search"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    PageResponse pageResponse = identifiableService.find(pageRequest);
    // add "query": "hallo" to request JSON instead of "searchTerm": "hallo"

    return null;
  }
}
