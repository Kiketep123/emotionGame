package roguelike_emotions.mainMechanics;

/**
 * DTO para resultado de negociación de fusión entre emociones sentientes.
 * Patrón: Value Object + Factory Method
 * 
 * @author Kike
 * @version 2.0
 */
public final class FusionNegotiation {
    
    // ==================== ENUMS ====================
    
    public enum RejectionReason {
        INCOMPATIBLE,
        LOW_LOYALTY,
        REQUIRES_SACRIFICE,
        UNSTABLE
    }
    
    public enum NegotiationType {
        ACCEPTED,
        REJECTED,
        REQUIRES_HP,
        REQUIRES_SACRIFICE,
        WEAKENED,
        UNSTABLE
    }
    
    // ==================== FIELDS (IMMUTABLE) ====================
    
    private final NegotiationType type;
    private final boolean accepted;
    private final String message;
    private final RejectionReason reason;
    private final int hpCost;
    private final int sacrificeCount;
    private final double fusionMultiplier;
    private final int failureChance;
    private final double failurePenalty;
    
    // ==================== CONSTRUCTOR (PRIVATE) ====================
    
    private FusionNegotiation(NegotiationType type, boolean accepted, String msg, 
                              RejectionReason reason, int hp, int sacrifice, 
                              double mult, int failChance, double failPenalty) {
        this.type = type;
        this.accepted = accepted;
        this.message = msg;
        this.reason = reason;
        this.hpCost = hp;
        this.sacrificeCount = sacrifice;
        this.fusionMultiplier = mult;
        this.failureChance = failChance;
        this.failurePenalty = failPenalty;
    }
    
    // ==================== FACTORY METHODS ====================
    
    public static FusionNegotiation accepted(double multiplier) {
        return new FusionNegotiation(
            NegotiationType.ACCEPTED, true, 
            "Fusión aceptada", null, 0, 0, multiplier, 0, 1.0
        );
    }
    
    public static FusionNegotiation rejected(String msg, RejectionReason reason) {
        return new FusionNegotiation(
            NegotiationType.REJECTED, false, 
            msg, reason, 0, 0, 1.0, 0, 1.0
        );
    }
    
    public static FusionNegotiation requiresHP(String msg, int hp) {
        return new FusionNegotiation(
            NegotiationType.REQUIRES_HP, false, 
            msg, RejectionReason.LOW_LOYALTY, hp, 0, 1.0, 0, 1.0
        );
    }
    
    public static FusionNegotiation requiresSacrifice(String msg, int count) {
        return new FusionNegotiation(
            NegotiationType.REQUIRES_SACRIFICE, false, 
            msg, RejectionReason.REQUIRES_SACRIFICE, 0, count, 1.0, 0, 1.0
        );
    }
    
    public static FusionNegotiation weakened(String msg, double penaltyMultiplier) {
        return new FusionNegotiation(
            NegotiationType.WEAKENED, true, 
            msg, null, 0, 0, penaltyMultiplier, 0, 1.0
        );
    }
    
    public static FusionNegotiation unstable(String msg, int failChance, double failPenalty) {
        return new FusionNegotiation(
            NegotiationType.UNSTABLE, true, 
            msg, RejectionReason.UNSTABLE, 0, 0, 1.0, failChance, failPenalty
        );
    }
    
    // ==================== GETTERS ====================
    
    public NegotiationType getType() { return type; }
    public boolean isAccepted() { return accepted; }
    public String getMessage() { return message; }
    public RejectionReason getReason() { return reason; }
    public int getHpCost() { return hpCost; }
    public int getSacrificeCount() { return sacrificeCount; }
    public double getFusionMultiplier() { return fusionMultiplier; }
    public int getFailureChance() { return failureChance; }
    public double getFailurePenalty() { return failurePenalty; }
    
    // ==================== UTILITY METHODS ====================
    
    public boolean hasAnyCost() {
        return hpCost > 0 || sacrificeCount > 0;
    }
    
    public boolean isRisky() {
        return type == NegotiationType.UNSTABLE || type == NegotiationType.WEAKENED;
    }
    
    @Override
    public String toString() {
        return String.format("FusionNegotiation[type=%s, accepted=%b, msg='%s']", 
                             type, accepted, message);
    }
}
