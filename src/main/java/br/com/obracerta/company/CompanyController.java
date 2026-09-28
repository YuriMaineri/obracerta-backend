package br.com.obracerta.company;

import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/company")
class CompanyController {

    private final CompanyService service;

    CompanyController(CompanyService service) {
        this.service = service;
    }

    @GetMapping
    CompanyResponse get() {
        return CompanyResponse.from(service.getCompany());
    }

    @PutMapping
    CompanyResponse update(@RequestBody @Valid CompanyRequest request) {
        return CompanyResponse.from(service.updateCompany(request));
    }

    @GetMapping("/logo")
    ResponseEntity<byte[]> logo() {
        Company company = service.getCompany();
        if (!company.hasLogo()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(company.getLogoContentType()))
                .cacheControl(CacheControl.noCache())
                .body(company.getLogo());
    }

    @PutMapping(path = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Void> uploadLogo(@RequestParam("file") MultipartFile file) throws IOException {
        service.replaceLogo(file.getBytes());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/logo")
    ResponseEntity<Void> removeLogo() {
        service.removeLogo();
        return ResponseEntity.noContent().build();
    }

    record CompanyResponse(Long id, String legalName, String tradeName, String taxId, String phone,
                           String email, String address, String district, String city,
                           String postalCode, String signatoryName, boolean hasLogo) {

        static CompanyResponse from(Company c) {
            return new CompanyResponse(c.getId(), c.getLegalName(), c.getTradeName(), c.getTaxId(),
                    c.getPhone(), c.getEmail(), c.getAddress(), c.getDistrict(), c.getCity(),
                    c.getPostalCode(), c.getSignatoryName(), c.hasLogo());
        }
    }
}
