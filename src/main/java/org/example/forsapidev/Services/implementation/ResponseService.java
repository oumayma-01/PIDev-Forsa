package org.example.forsapidev.Services.implementation;

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

    // ✅ Reporting
    @Override
    public Map<String, Object> getResponseSummaryReport() {
        long total = responseRepository.count();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Object[] row : responseRepository.countByResponseStatus()) {
            byStatus.put(String.valueOf(row[0]), (Long) row[1]);
        }

        return new LinkedHashMap<>(Map.of(
                "totalResponses", total,
                "byResponseStatus", byStatus
        ));
    }




        private final ComplaintAiAssistant complaintAiAssistant;

        @Override
        public Response improveResponseWithAI(Long responseId) {
            Response r = responseRepository.findById(responseId).orElse(null);
            if (r == null) return null;

            if (r.getComplaint() == null) {
                throw new IllegalStateException("Response must be linked to a complaint");
            }

            String improved = complaintAiAssistant.improveResponse(
                    r.getComplaint().getCategory(),
                    r.getComplaint().getSubject(),
                    r.getComplaint().getDescription(),
                    r.getMessage()
            );

            r.setMessage(improved);
            r.setResponseStatus("PROCESSED");
            return responseRepository.save(r);
        }
    }


