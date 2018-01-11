package de.isas.mztab1_1.server.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.isas.lipidomics.mztab.validator.webapp.service.StorageService;
import de.isas.lipidomics.mztab.validator.webapp.service.ValidationService;
import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-01-11T19:50:29.849+01:00")

@Controller
public class ValidatePlainApiController implements ValidatePlainApi {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    
    private final StorageService storageService;
    
    private final ValidationService validationService;

    @org.springframework.beans.factory.annotation.Autowired
    public ValidatePlainApiController(ObjectMapper objectMapper, HttpServletRequest request, StorageService storageService, ValidationService validationService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.storageService = storageService;
        this.validationService = validationService;
    }

    @Override
    public Optional<ObjectMapper> getObjectMapper() {
        return Optional.ofNullable(objectMapper);
    }

    @Override
    public Optional<HttpServletRequest> getRequest() {
        return Optional.ofNullable(request);
    }
    
    @Override
    public Optional<ValidationService> getValidationService() {
        return Optional.ofNullable(validationService);
    }
    
    @Override
    public Optional<StorageService> getStorageService() {
        return Optional.ofNullable(storageService);
    }

}
