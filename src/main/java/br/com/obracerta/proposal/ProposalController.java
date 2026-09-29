package br.com.obracerta.proposal;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ProposalController {

    private final ProposalService service;

    ProposalController(ProposalService service) {
        this.service = service;
    }

    /** Abre no navegador; com download=true, baixa o arquivo. */
    @GetMapping("/api/estimates/{id}/proposal")
    ResponseEntity<byte[]> proposal(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean download) {
        GeneratedProposal proposal = service.generate(id);
        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(proposal.fileName())
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(proposal.content());
    }
}
