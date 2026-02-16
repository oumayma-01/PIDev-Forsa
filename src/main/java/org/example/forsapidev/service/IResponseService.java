package org.example.forsapidev.service;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;
import java.util.List;

public interface IResponseService {

    List<Response> retrieveAllResponses();
    Response retrieveResponse(Long responseId);
    Response addResponse(Response r);
    void removeResponse(Long responseId);
    Response modifyResponse(Response response);
}