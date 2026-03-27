package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.ResponseRepository;
import org.example.forsapidev.Services.Interfaces.IResponseService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;
import org.example.forsapidev.openai.ComplaintAiAssistant;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResponseService implements IResponseService {

    private final ResponseRepository responseRepository;
    private final ComplaintAiAssistant complaintAiAssistant;

    @Override
    public List<Response> retrieveAllResponses() {
        return responseRepository.findAll();
    }

    @Override
    public Response retrieveResponse(Long responseId) {
        return responseRepository.findById(responseId).orElse(null);
    }

    @Override
    public Response addResponse(Response r) {
        return responseRepository.save(r);
    }

    @Override
    public void removeResponse(Long responseId) {
        responseRepository.deleteById(responseId);
    }

    @Override
    public Response modifyResponse(Response response) {
        return responseRepository.save(response);
    }

    @Override
    public Map<String, Object> getResponseSummaryReport() {
        long total = responseRepository.count();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("total", total);
        return res;
    }

    @Override
    public Response improveResponseWithAI(Long responseId) {
        Response r = responseRepository.findById(responseId).orElse(null);
        if (r == null) return null;

        if (r.getMessage() != null && !r.getMessage().isBlank()) {
            try {
                String improved = complaintAiAssistant.improveResponse(
                        "SUPPORT",
                        "Response improvement",
                        r.getMessage(),
                        r.getMessage()
                );
                r.setMessage(improved);
            } catch (Exception e) {
                r.setMessage(r.getMessage().trim() + "\n\n(Reviewed & Improved)");
            }
        }
        r.setResponseStatus("PROCESSED");
        return responseRepository.save(r);
    }
}