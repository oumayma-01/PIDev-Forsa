package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import java.util.List;

public interface IPremiumPayment {
    public List<PremiumPayment> retrieveAllPremiumPayments();
    public PremiumPayment retrievePremiumPayment(Long paymentId);
    public PremiumPayment addPremiumPayment(PremiumPayment payment);
    public void removePremiumPayment(Long paymentId);
    public PremiumPayment modifyPremiumPayment(PremiumPayment payment);
}