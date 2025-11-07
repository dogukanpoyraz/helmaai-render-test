package com.backend.helmaaibackend.controller.admin;

import com.backend.helmaaibackend.dto.admin.AdminUserView;
import com.backend.helmaaibackend.dto.admin.UpdateUserRolesRequest;
import com.backend.helmaaibackend.dto.admin.UpdateUserStatusRequest;
import com.backend.helmaaibackend.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Admin - Users", description = "User management (ADMIN only)")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(summary = "List users (paginated + search)")
    @GetMapping
    public ResponseEntity<Page<AdminUserView>> list(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", required = false) String[] sort // e.g.: sort=-createdAt&sort=email
    ) {
        List<String> sorts = (sort == null) ? List.of() : Arrays.asList(sort);
        return ResponseEntity.ok(adminUserService.listUsers(search, page, size, sorts));
    }

    @Operation(summary = "Get single user details")
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserView> getOne(@PathVariable String id) {
        return ResponseEntity.ok(adminUserService.getUser(id));
    }

    @Operation(summary = "Update user active status")
    @PutMapping("/{id}/status")
    public ResponseEntity<AdminUserView> updateStatus(@PathVariable String id,
                                                      @Valid @RequestBody UpdateUserStatusRequest req) {
        return ResponseEntity.ok(adminUserService.updateStatus(id, req.getActive()));
    }

    @Operation(summary = "Update user roles")
    @PutMapping("/{id}/roles")
    public ResponseEntity<AdminUserView> updateRoles(@PathVariable String id,
                                                     @Valid @RequestBody UpdateUserRolesRequest req) {
        return ResponseEntity.ok(adminUserService.updateRoles(id, req.getRoles()));
    }

    @Operation(summary = "Soft delete user (marks deletedAt, sets active=false)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable String id) {
        adminUserService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Soft-deleted kullanıcıyı geri getir (restore)")
    @PutMapping("/{id}/restore")
    public ResponseEntity<AdminUserView> restore(@PathVariable String id) {
        return ResponseEntity.ok(adminUserService.restore(id));
    }
}
