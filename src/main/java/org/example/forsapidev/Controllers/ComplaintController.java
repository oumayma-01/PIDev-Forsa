package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.Services.Interfaces.IComplaintService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Getter
@Setter
public class ComplaintController {

    private final IComplaintService complaintService;

    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @GetMapping("/retrieve-all-complaints")
    public List<Complaint> getComplaints() {
        return complaintService.retrieveAllComplaints();
    }

    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
    @GetMapping("/retrieve-complaint/{complaint-id}")
    public Complaint retrieveComplaint(@PathVariable("complaint-id") Long cId) {
        return complaintService.retrieveComplaint(cId);
    }
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/add-complaint")
    public Complaint addComplaint(@RequestBody Complaint c) {
        return complaintService.addComplaint(c);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/remove-complaint/{complaint-id}")
    public void removeComplaint(@PathVariable("complaint-id") Long cId) {
        complaintService.removeComplaint(cId);
    }
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @PutMapping("/modify-complaint")
    public Complaint modifyComplaint(@RequestBody Complaint c) {
        return complaintService.modifyComplaint(c);
    }
}