package org.example.forsapidev.Services;

import org.example.forsapidev.Repositories.GiftRepository;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.CreditManagement.Gift;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service de gestion des gifts (cadeaux) pour les clients
 * Règle : accumulation de 1.5% du capital de chaque crédit approuvé
 * Attribution automatique quand accumulatedAmount >= 500 DT
 */
@Service
public class GiftService {

    private static final Logger logger = LoggerFactory.getLogger(GiftService.class);

    /**
     * Pourcentage du capital qui est accumulé pour le gift (1.5%)
     */
    private static final BigDecimal GIFT_PERCENTAGE = new BigDecimal("0.015");

    /**
     * Seuil par défaut pour attribution du gift (500 DT)
     */
    private static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal("500.00");

    private final GiftRepository giftRepository;

    @Autowired
    public GiftService(GiftRepository giftRepository) {
        this.giftRepository = giftRepository;
    }

    /**
     * Accumule un montant de gift pour un crédit approuvé
     * Montant accumulé = capital × 1.5%
     */
    @Transactional
    public Gift accumulateForCredit(CreditRequest credit) {
        if (credit == null || credit.getUser() == null) {
            logger.warn("Impossible d'accumuler du gift : crédit ou utilisateur null");
            return null;
        }

        Long clientId = credit.getUser().getId();
        BigDecimal capital = credit.getAmountRequested();

        // Calculer le montant à accumuler (1.5% du capital)
        BigDecimal giftIncrement = capital.multiply(GIFT_PERCENTAGE)
                .setScale(2, RoundingMode.HALF_EVEN);

        logger.info("Accumulation gift pour client {} : capital={}, increment={}",
                clientId, capital, giftIncrement);

        // Récupérer ou créer le gift du client
        Gift gift = giftRepository.findByClientId(clientId)
                .orElseGet(() -> {
                    Gift newGift = new Gift(clientId);
                    newGift.setThreshold(DEFAULT_THRESHOLD);
                    return newGift;
                });

        // Ajouter l'incrément
        gift.addAccumulation(giftIncrement);

        // Sauvegarder
        gift = giftRepository.save(gift);

        logger.info("Gift accumulé pour client {} : montant total={}, seuil={}",
                clientId, gift.getAccumulatedAmount(), gift.getThreshold());

        // Vérifier si le seuil est atteint
        if (gift.isThresholdReached()) {
            logger.info("✅ Seuil atteint pour client {} ! Montant accumulé : {}",
                    clientId, gift.getAccumulatedAmount());
            awardGift(gift);
        }

        return gift;
    }

    /**
     * Attribue le gift à un client (quand seuil atteint)
     * Crée une opération de paiement et marque le gift comme attribué
     */
    @Transactional
    public Gift awardGift(Gift gift) {
        if (gift == null || gift.getAwarded()) {
            logger.warn("Gift déjà attribué ou null");
            return gift;
        }

        if (!gift.isThresholdReached()) {
            logger.warn("Tentative d'attribution alors que le seuil n'est pas atteint : client={}, montant={}",
                    gift.getClientId(), gift.getAccumulatedAmount());
            return gift;
        }

        // Montant à attribuer (peut être le montant accumulé ou un montant fixe 500)
        BigDecimal awardAmount = gift.getAccumulatedAmount();

        // Option 1: donner tout le montant accumulé
        // Option 2: donner exactement 500 DT et garder le surplus
        // Ici on va donner le montant accumulé et réinitialiser

        gift.markAsAwarded(awardAmount);

        // TODO: Créer une opération de paiement/transaction pour créditer le client
        // Exemple : paymentService.creditClientAccount(gift.getClientId(), awardAmount, "GIFT");

        logger.info("🎁 Gift attribué au client {} : montant={} DT",
                gift.getClientId(), awardAmount);

        // Après attribution, réinitialiser pour permettre une nouvelle accumulation
        // OU créer un nouveau Gift record (selon business)
        // Ici on réinitialise :
        gift.setAccumulatedAmount(BigDecimal.ZERO);
        gift.setAwarded(false);  // Permettre une nouvelle accumulation
        gift.setUpdatedAt(LocalDateTime.now());

        return giftRepository.save(gift);
    }

    /**
     * Récupère le gift d'un client
     */
    public Gift getGiftByClientId(Long clientId) {
        return giftRepository.findByClientId(clientId).orElse(null);
    }

    /**
     * Récupère tous les gifts qui ont atteint le seuil mais non attribués
     */
    public List<Gift> getPendingGifts() {
        return giftRepository.findByAwardedFalseAndAccumulatedAmountGreaterThanEqual(DEFAULT_THRESHOLD);
    }

    /**
     * Traite tous les gifts en attente
     */
    @Transactional
    public void processAllPendingGifts() {
        List<Gift> pendingGifts = getPendingGifts();
        logger.info("Traitement de {} gifts en attente", pendingGifts.size());

        for (Gift gift : pendingGifts) {
            awardGift(gift);
        }
    }

    /**
     * Obtient le montant actuel accumulé pour un client
     */
    public BigDecimal getAccumulatedAmount(Long clientId) {
        return giftRepository.findByClientId(clientId)
                .map(Gift::getAccumulatedAmount)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Crée ou met à jour manuellement un gift
     */
    @Transactional
    public Gift saveGift(Gift gift) {
        return giftRepository.save(gift);
    }
}

