package org.example.forsapidev.Services.implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IResponseService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;
import org.example.forsapidev.Repositories.ResponseRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResponseService implements IResponseService {

    private final ResponseRepository responseRepository;

    public List<Response> retrieveAllResponses() {
        return responseRepository.findAll();
    }

    public Response retrieveResponse(Long responseId) {
        return responseRepository.findById(responseId).orElse(null);
    }

    public Response addResponse(Response r) {
        return responseRepository.save(r);
    }

    public void removeResponse(Long responseId) {
        responseRepository.deleteById(responseId);
    }

    public Response modifyResponse(Response response) {
        return responseRepository.save(response);
    }
}