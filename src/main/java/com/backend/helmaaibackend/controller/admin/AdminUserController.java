package com.backend.helmaaibackend.controller.admin;

import com.backend.helmaaibackend.domain.AuditType;
import com.backend.helmaaibackend.domain.UserAccount;
import com.backend.helmaaibackend.dto.admin.AdminUserView;
import com.backend.helmaaibackend.dto.admin.StatsResponse;
import com.backend.helmaaibackend.dto.admin.UpdateUserRolesRequest;
import com.backend.helmaaibackend.dto.admin.UpdateUserStatusRequest;
import com.backend.helmaaibackend.service.AdminUserService;
import com.backend.helmaaibackend.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Admin - Users", description = "User management (ADMIN only)")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AuditLogService audit;

    public AdminUserController(AdminUserService adminUserService, AuditLogService audit) {
        this.adminUserService = adminUserService;
        this.audit = audit;
    }

    private UserAccount actor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (UserAccount) auth.getPrincipal();
    }

    @Operation(summary = "List users (paginated + search)")
    @GetMapping
    public ResponseEntity<Page<AdminUserView>> list(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", required = false) String[] sort
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
        var actor = actor();
        var res = adminUserService.updateStatus(id, req.getActive());
        audit.log(AuditType.ADMIN_UPDATE_STATUS, id, res.getEmail(), actor.getId(), actor.getEmail(),
                "active=" + req.getActive());
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "Update user roles")
    @PutMapping("/{id}/roles")
    public ResponseEntity<AdminUserView> updateRoles(@PathVariable String id,
                                                     @Valid @RequestBody UpdateUserRolesRequest req) {
        var actor = actor();
        var res = adminUserService.updateRoles(id, req.getRoles());
        audit.log(AuditType.ADMIN_UPDATE_ROLES, id, res.getEmail(), actor.getId(), actor.getEmail(),
                "roles=" + req.getRoles());
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "Soft delete user")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable String id) {
        var actor = actor();
        adminUserService.softDelete(id);
        audit.log(AuditType.ADMIN_SOFT_DELETE, id, null, actor.getId(), actor.getEmail(), "soft-delete");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Hard delete user (permanently delete)")
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDelete(@PathVariable String id) {
        var actor = actor();
        adminUserService.hardDelete(id);
        audit.log(AuditType.ADMIN_HARD_DELETE, id, null, actor.getId(), actor.getEmail(), "hard-delete");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deactivate user (active=false)")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<AdminUserView> deactivate(@PathVariable String id) {
        var actor = actor();
        var res = adminUserService.deactivate(id);
        audit.log(AuditType.ADMIN_DEACTIVATE, id, res.getEmail(), actor.getId(), actor.getEmail(), "deactivate");
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "Restore soft-deleted user")
    @PutMapping("/{id}/restore")
    public ResponseEntity<AdminUserView> restore(@PathVariable String id) {
        var actor = actor();
        var res = adminUserService.restore(id);
        audit.log(AuditType.ADMIN_RESTORE, id, res.getEmail(), actor.getId(), actor.getEmail(), "restore");
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "System statistics")
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> stats() {
        return ResponseEntity.ok(adminUserService.stats());
    }
}
