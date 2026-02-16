package org.example.forsapidev.control;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;
import org.example.forsapidev.service.IResponseService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/responses")
@RequiredArgsConstructor
@Getter
@Setter
public class ResponseController {

    private final IResponseService responseService;

    @GetMapping("/retrieve-all-responses")
    public List<Response> getResponses() {
        return responseService.retrieveAllResponses();
    }

    @GetMapping("/retrieve-response/{response-id}")
    public Response retrieveResponse(@PathVariable("response-id") Long rId) {
        return responseService.retrieveResponse(rId);
    }

    @PostMapping("/add-response")
    public Response addResponse(@RequestBody Response r) {
        return responseService.addResponse(r);
    }

    @DeleteMapping("/remove-response/{response-id}")
    public void removeResponse(@PathVariable("response-id") Long rId) {
        responseService.removeResponse(rId);
    }

    @PutMapping("/modify-response")
    public Response modifyResponse(@RequestBody Response r) {
        return responseService.modifyResponse(r);
    }
}