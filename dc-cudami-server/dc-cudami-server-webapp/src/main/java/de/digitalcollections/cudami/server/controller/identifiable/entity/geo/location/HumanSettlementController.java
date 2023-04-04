package de.digitalcollections.cudami.server.controller.identifiable.entity.geo.location;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo.location.HumanSettlementService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Human settlement controller")
public class HumanSettlementController extends AbstractIdentifiableController<HumanSettlement> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HumanSettlementController.class);

  private final HumanSettlementService service;

  public HumanSettlementController(HumanSettlementService humanSettlementService) {
    this.service = humanSettlementService;
  }

  @Operation(summary = "Delete a human settlement")
  @DeleteMapping(
      value = {"/v6/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the human settlement") @PathVariable("uuid")
          UUID uuid)
      throws ConflictException {
    boolean successful;
    try {
      successful = service.delete(HumanSettlement.builder().uuid(uuid).build());
    } catch (ServiceException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get all human settlements as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/humansettlements"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<HumanSettlement> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria)
      throws ServiceException {
    PageRequest pageRequest =
        createPageRequest(HumanSettlement.class, pageNumber, pageSize, sortBy, filterCriteria);
    return service.find(pageRequest);
  }

  @Override
  @Operation(
      summary = "Get a geolocation by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {
        "/v6/humansettlements/identifier/**",
        "/v5/humansettlements/identifier/**",
        "/v2/humansettlements/identifier/**",
        "/latest/humansettlements/identifier/**"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HumanSettlement> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get a human settlement by namespace and id")
  @GetMapping(
      value = {
        "/v6/humansettlements/identifier",
        "/v5/humansettlements/identifier",
        "/v2/humansettlements/identifier",
        "/latest/humansettlements/identifier"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> getByIdentifier(
      @RequestParam(name = "namespace", required = true) String namespace,
      @RequestParam(name = "id", required = true) String id,
      HttpServletRequest request)
      throws ServiceException {
    URI newLocation =
        URI.create(request.getRequestURI().concat(String.format("/%s:%s", namespace, id)));
    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(newLocation).build();
  }

  @Operation(summary = "Get a human settlement by uuid")
  @GetMapping(
      value = {
        "/v6/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HumanSettlement> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the human settlement, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws ServiceException {

    HumanSettlement result;
    if (pLocale == null) {
      result = service.getByExample(HumanSettlement.builder().uuid(uuid).build());
    } else {
      result = service.getByExampleAndLocale(HumanSettlement.builder().uuid(uuid).build(), pLocale);
    }
    return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Override
  protected IdentifiableService<HumanSettlement> getService() {
    return service;
  }

  @Operation(summary = "save a newly created human settlement")
  @PostMapping(
      value = {
        "/v6/humansettlements",
        "/v5/humansettlements",
        "/v2/humansettlements",
        "/latest/humansettlements"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public HumanSettlement save(@RequestBody HumanSettlement humanSettlement, BindingResult errors)
      throws ServiceException, ValidationException {
    service.save(humanSettlement);
    return humanSettlement;
  }

  @Operation(summary = "update a human settlement")
  @PutMapping(
      value = {
        "/v6/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public HumanSettlement update(
      @PathVariable("uuid") UUID uuid,
      @RequestBody HumanSettlement humanSettlement,
      BindingResult errors)
      throws ServiceException, ValidationException {
    assert Objects.equals(uuid, humanSettlement.getUuid());
    service.update(humanSettlement);
    return humanSettlement;
  }
}
