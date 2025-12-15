package roguelike_emotions.mainMechanics;

import roguelike_emotions.characters.Player;
import roguelike_emotions.utils.CombatLogger;

/**
 * Handler para procesar negociaciones de fusión entre emociones.
 * Patrón: Service Layer + Strategy
 * 
 * @version 1.0
 */
public class FusionNegotiationHandler {
    
    private final Player player;
    
    public FusionNegotiationHandler(Player player) {
        this.player = player;
    }
    
    /**
     * Procesa negociación entre dos emociones
     */
    public NegotiationResult processNegotiation(EmotionInstance emotionA, EmotionInstance emotionB) {
        boolean sentientA = emotionA instanceof SentientEmotion;
        boolean sentientB = emotionB instanceof SentientEmotion;
        
        // Caso 1: Ambas sentientes
        if (sentientA && sentientB) {
            return processDoubleSentient((SentientEmotion) emotionA, (SentientEmotion) emotionB);
        }
        
        // Caso 2: Solo A es sentiente
        if (sentientA) {
            return processSingleSentient((SentientEmotion) emotionA);
        }
        
        // Caso 3: Solo B es sentiente
        if (sentientB) {
            return processSingleSentient((SentientEmotion) emotionB);
        }
        
        // Caso 4: Ninguna sentiente - fusión directa
        return NegotiationResult.directFusion();
    }
    
    /**
     * Procesa negociación entre dos emociones sentientes
     */
    private NegotiationResult processDoubleSentient(SentientEmotion s1, SentientEmotion s2) {
        FusionNegotiation n1 = s1.negotiateFusionWith(s2);
        FusionNegotiation n2 = s2.negotiateFusionWith(s1);
        
        // Verificar rechazos
        if (!n1.isAccepted()) {
            return NegotiationResult.rejected(s1.getNombre(), n1);
        }
        if (!n2.isAccepted()) {
            return NegotiationResult.rejected(s2.getNombre(), n2);
        }
        
        // Combinar costes
        int totalHp = n1.getHpCost() + n2.getHpCost();
        int totalSacrifice = n1.getSacrificeCount() + n2.getSacrificeCount();
        
        // Verificar debilitamiento
        if (n1.getType() == FusionNegotiation.NegotiationType.WEAKENED ||
            n2.getType() == FusionNegotiation.NegotiationType.WEAKENED) {
            return NegotiationResult.weakened(n1, n2);
        }
        
        // Verificar inestabilidad
        if (n1.getType() == FusionNegotiation.NegotiationType.UNSTABLE ||
            n2.getType() == FusionNegotiation.NegotiationType.UNSTABLE) {
            int maxFailChance = Math.max(n1.getFailureChance(), n2.getFailureChance());
            return NegotiationResult.unstable(maxFailChance, n1, n2);
        }
        
        // Verificar costes
        if (totalHp > 0 || totalSacrifice > 0) {
            return NegotiationResult.requiresCost(totalHp, totalSacrifice, n1, n2);
        }
        
        // Aceptada sin condiciones
        return NegotiationResult.accepted();
    }
    
    /**
     * Procesa negociación con una emoción sentiente
     */
    private NegotiationResult processSingleSentient(SentientEmotion sentient) {
        FusionNegotiation negotiation = sentient.negotiateFusionWith(null);
        
        if (!negotiation.isAccepted()) {
            return NegotiationResult.rejected(sentient.getNombre(), negotiation);
        }
        
        switch (negotiation.getType()) {
            case WEAKENED:
                return NegotiationResult.weakened(negotiation, null);
                
            case UNSTABLE:
                return NegotiationResult.unstable(negotiation.getFailureChance(), negotiation, null);
                
            default:
                if (negotiation.hasAnyCost()) {
                    return NegotiationResult.requiresCost(
                        negotiation.getHpCost(), 
                        negotiation.getSacrificeCount(), 
                        negotiation, 
                        null
                    );
                }
                return NegotiationResult.accepted();
        }
    }
    
    /**
     * Paga los costes de una negociación
     */
    public boolean payCosts(int hpCost, int sacrificeCount) {
        // Validar HP
        if (hpCost > 0) {
            if (player.getHealth() <= hpCost) {
                CombatLogger.get().log("❌ HP insuficiente");
                return false;
            }
            player.takeDamage(hpCost);
            CombatLogger.get().log("💔 Pagaste " + hpCost + " HP");
        }
        
        return true; // Sacrificio se maneja en UI
    }
    
    /**
     * Verifica si se puede pagar el coste
     */
    public boolean canAfford(int hpCost, int sacrificeCount, int availableEmotions) {
        boolean hasHP = player.getHealth() > hpCost;
        boolean hasSacrifice = availableEmotions >= sacrificeCount;
        return hasHP && hasSacrifice;
    }
    
    /**
     * Ejecuta sacrificio de emociones
     */
    public void sacrificeEmotions(EmotionInstance[] toSacrifice, SentientEmotion... gluttons) {
        for (EmotionInstance sacrifice : toSacrifice) {
            player.removeEmocion(sacrifice);
            
            // Alimentar glotonas
            for (SentientEmotion glutton : gluttons) {
                if (glutton != null) {
                    glutton.feedWithEmotion(sacrifice);
                }
            }
            
            CombatLogger.get().log("🔥 Sacrificaste: " + sacrifice.getNombre());
        }
    }
    
    // ==================== INNER CLASS: RESULT ====================
    
    /**
     * Resultado de una negociación de fusión
     */
    public static class NegotiationResult {
        
        public enum ResultType {
            DIRECT_FUSION,
            ACCEPTED,
            REJECTED,
            REQUIRES_COST,
            WEAKENED,
            UNSTABLE
        }
        
        private final ResultType type;
        private final String emotionName;
        private final FusionNegotiation negotiation1;
        private final FusionNegotiation negotiation2;
        private final int hpCost;
        private final int sacrificeCount;
        private final int failureChance;
        
        private NegotiationResult(ResultType type, String name, FusionNegotiation n1, 
                                 FusionNegotiation n2, int hp, int sacrifice, int failChance) {
            this.type = type;
            this.emotionName = name;
            this.negotiation1 = n1;
            this.negotiation2 = n2;
            this.hpCost = hp;
            this.sacrificeCount = sacrifice;
            this.failureChance = failChance;
        }
        
        public static NegotiationResult directFusion() {
            return new NegotiationResult(ResultType.DIRECT_FUSION, null, null, null, 0, 0, 0);
        }
        
        public static NegotiationResult accepted() {
            return new NegotiationResult(ResultType.ACCEPTED, null, null, null, 0, 0, 0);
        }
        
        public static NegotiationResult rejected(String emotionName, FusionNegotiation negotiation) {
            return new NegotiationResult(ResultType.REJECTED, emotionName, negotiation, null, 0, 0, 0);
        }
        
        public static NegotiationResult requiresCost(int hp, int sacrifice, 
                                                     FusionNegotiation n1, FusionNegotiation n2) {
            return new NegotiationResult(ResultType.REQUIRES_COST, null, n1, n2, hp, sacrifice, 0);
        }
        
        public static NegotiationResult weakened(FusionNegotiation n1, FusionNegotiation n2) {
            return new NegotiationResult(ResultType.WEAKENED, null, n1, n2, 0, 0, 0);
        }
        
        public static NegotiationResult unstable(int failChance, FusionNegotiation n1, FusionNegotiation n2) {
            return new NegotiationResult(ResultType.UNSTABLE, null, n1, n2, 0, 0, failChance);
        }
        
        // Getters
        public ResultType getType() { return type; }
        public String getEmotionName() { return emotionName; }
        public FusionNegotiation getNegotiation1() { return negotiation1; }
        public FusionNegotiation getNegotiation2() { return negotiation2; }
        public int getHpCost() { return hpCost; }
        public int getSacrificeCount() { return sacrificeCount; }
        public int getFailureChance() { return failureChance; }
        
        public boolean canProceed() {
            return type == ResultType.DIRECT_FUSION || type == ResultType.ACCEPTED;
        }
    }
}
