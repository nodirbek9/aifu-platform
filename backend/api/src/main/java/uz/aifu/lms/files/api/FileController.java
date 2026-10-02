package uz.aifu.lms.files.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import uz.aifu.lms.common.error.Exceptions;
import uz.aifu.lms.common.security.AuthenticatedUser;
import uz.aifu.lms.common.security.CurrentUserProvider;
import uz.aifu.lms.files.api.dto.FileDtos.FileDetail;
import uz.aifu.lms.files.app.FileService;
import uz.aifu.lms.files.domain.FileVisibility;
import uz.aifu.lms.files.domain.StoredFile;

/**
 * File upload/download/delete (V013, ADR-008). Download is gated only on {@code isAuthenticated()}
 * — no {@code FILE_READ} permission exists in V003, deliberately: visibility (PUBLIC/AUTHORIZED/
 * PRIVATE, checked by {@code FileService.canRead}) IS the read authorization here, not a coarse
 * role permission, since a file's owning course/content object (which this module cannot see —
 * {@code files} may depend on nothing beyond {@code common}) is what would otherwise decide
 * visibility. No permission was invented to paper over this (CLAUDE.md §14.5).
 */
@RestController
@Tag(name = "Files", description = "Upload, download, delete file metadata")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileService files;
    private final CurrentUserProvider currentUser;

    public FileController(FileService files, CurrentUserProvider currentUser) {
        this.files = files;
        this.currentUser = currentUser;
    }

    @PostMapping(value = "/api/v1/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('FILE_UPLOAD')")
    @Operation(summary = "Upload a file", description = "Visibility defaults to AUTHORIZED.")
    public ResponseEntity<FileDetail> upload(
            @RequestPart("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false) FileVisibility visibility) {
        StoredFile saved = files.upload(file, visibility, currentUser.require().id());
        return ResponseEntity.created(URI.create("/api/v1/files/" + saved.getId()))
                .body(toDetail(saved));
    }

    @GetMapping("/api/v1/files/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get file metadata")
    public FileDetail get(@PathVariable UUID id) {
        StoredFile file = files.require(id);
        requireReadable(file);
        return toDetail(file);
    }

    @GetMapping("/api/v1/files/{id}/content")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Download the file's raw content")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        StoredFile file = files.require(id);
        requireReadable(file);
        Resource content = files.readContent(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getOriginalFilename() + "\"")
                .body(content);
    }

    @DeleteMapping("/api/v1/files/{id}")
    @PreAuthorize("hasAuthority('FILE_MANAGE')")
    @Operation(summary = "Soft-delete a file's metadata (the underlying object is left in place)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        files.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void requireReadable(StoredFile file) {
        AuthenticatedUser caller = currentUser.require();
        if (!files.canRead(file, caller.id(), caller.isAdmin())) {
            throw Exceptions.notFound("File", file.getId());
        }
    }

    private static FileDetail toDetail(StoredFile file) {
        return new FileDetail(
                file.getId(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSizeBytes(),
                file.getChecksumSha256(),
                file.getVisibility(),
                file.getCreatedAt());
    }
}
