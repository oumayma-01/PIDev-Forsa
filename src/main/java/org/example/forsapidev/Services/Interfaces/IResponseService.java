package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;
import java.util.List;
import java.util.Map;

public interface IResponseService {

    List<Response> retrieveAllResponses();
    Response retrieveResponse(Long responseId);
    Response addResponse(Response r);
    void removeResponse(Long responseId);
    Response modifyResponse(Response response);
    Map<String, Object> getResponseSummaryReport();
    Response improveResponseWithAI(Long responseId);


}