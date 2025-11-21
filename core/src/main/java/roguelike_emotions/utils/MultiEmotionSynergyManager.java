package roguelike_emotions.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import roguelike_emotions.mainMechanics.EmotionType;

/**
 * Gestor de sinergias entre emociones.
 * Define efectos especiales cuando múltiples emociones están activas simultáneamente.
 */
public class MultiEmotionSynergyManager {

    // ==================== REGISTRO DE SINERGIAS ====================

    private static final Map<EmotionPair, SynergyEffect> SYNERGY_REGISTRY = buildSynergyRegistry();

    /**
     * Par ordenado de emociones (inmutable, hasheable)
     */
    private static class EmotionPair {
        private final EmotionType first;
        private final EmotionType second;
        private final int hash;

        public EmotionPair(EmotionType a, EmotionType b) {
            // Orden lexicográfico automático
            if (a.name().compareTo(b.name()) < 0) {
                this.first = a;
                this.second = b;
            } else {
                this.first = b;
                this.second = a;
            }
            this.hash = Objects.hash(first, second);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EmotionPair)) return false;
            EmotionPair pair = (EmotionPair) o;
            return first == pair.first && second == pair.second;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return first.name() + "-" + second.name();
        }
    }

    // ==================== CONSTRUCCIÓN DEL REGISTRO ====================

    private static Map<EmotionPair, SynergyEffect> buildSynergyRegistry() {
        Map<EmotionPair, SynergyEffect> registry = new HashMap<>();

        // ===== SINERGIAS POSITIVAS (CONSTRUCTIVAS) =====

        // Alegría + Esperanza: "Brillo Inspirador" - Curación y optimismo
        register(registry, EmotionType.ALEGRIA, EmotionType.ESPERANZA,
            SynergyEffect.builder()
                .name("Brillo Inspirador")
                .speed(0.95)
                .healOverTime(3, 3)
                .build()
        );

        // Alegría + Calma: "Éxtasis Sereno" - Equilibrio perfecto
        register(registry, EmotionType.ALEGRIA, EmotionType.CALMA,
            SynergyEffect.builder()
                .name("Éxtasis Sereno")
                .build()
        );

        // Esperanza + Calma: "Paz Radiante" - Defensa y silencio enemigo
        register(registry, EmotionType.ESPERANZA, EmotionType.CALMA,
            SynergyEffect.builder()
                .name("Paz Radiante")
                .buff("defenseBoost", 1.20, 2)
                .debuff("silence", 1)
                .build()
        );

        // Culpa + Calma: "Remordimiento Tranquilo" - Redención con curación
        register(registry, EmotionType.CULPA, EmotionType.CALMA,
            SynergyEffect.builder()
                .name("Remordimiento Tranquilo")
                .healOverTime(2, 2)
                .build()
        );

        // ===== SINERGIAS AGRESIVAS (DESTRUCTIVAS) =====

        // Ira + Rabia: "Rabia Incontenible" - Máximo daño y aturdimiento
        register(registry, EmotionType.IRA, EmotionType.RABIA,
            SynergyEffect.builder()
                .name("Rabia Incontenible")
                .damage(1.20)
                .stun(1)
                .build()
        );

        // Alegría + Ira: "Furia Regocijada" - Daño con veneno
        register(registry, EmotionType.ALEGRIA, EmotionType.IRA,
            SynergyEffect.builder()
                .name("Furia Regocijada")
                .damage(1.10)
                .poisonOverTime(2, 2)
                .build()
        );

        // Alegría + Rabia: "Cólera Exultante" - Velocidad extrema con stun
        register(registry, EmotionType.ALEGRIA, EmotionType.RABIA,
            SynergyEffect.builder()
                .name("Cólera Exultante")
                .speed(1.15)
                .stun(1)
                .build()
        );

        // Ira + Miedo: "Terror Indignado" - Daño con veneno
        register(registry, EmotionType.IRA, EmotionType.MIEDO,
            SynergyEffect.builder()
                .name("Terror Indignado")
                .damage(1.10)
                .poisonOverTime(2, 2)
                .build()
        );

        // Rabia + Tristeza: "Odio Lúgubre" - Alto daño
        register(registry, EmotionType.RABIA, EmotionType.TRISTEZA,
            SynergyEffect.builder()
                .name("Odio Lúgubre")
                .damage(1.15)
                .build()
        );

        // Culpa + Rabia: "Furia Rubor" - Daño moderado
        register(registry, EmotionType.CULPA, EmotionType.RABIA,
            SynergyEffect.builder()
                .name("Furia Rubor")
                .damage(1.08)
                .build()
        );

        // ===== SINERGIAS DEFENSIVAS =====

        // Alegría + Miedo: "Alivio Nervioso" - Boost defensivo
        register(registry, EmotionType.ALEGRIA, EmotionType.MIEDO,
            SynergyEffect.builder()
                .name("Alivio Nervioso")
                .defense(1.10)
                .buff("defenseBoost", 1.10, 2)
                .build()
        );

        // Esperanza + Tristeza: "Consuelo Etéreo" - Alta defensa
        register(registry, EmotionType.ESPERANZA, EmotionType.TRISTEZA,
            SynergyEffect.builder()
                .name("Consuelo Etéreo")
                .defense(1.10)
                .build()
        );

        // Culpa + Miedo: "Pavor Apesadumbrado" - Defensa aumentada
        register(registry, EmotionType.CULPA, EmotionType.MIEDO,
            SynergyEffect.builder()
                .name("Pavor Apesadumbrado")
                .defense(1.10)
                .build()
        );

        // Culpa + Tristeza: "Pesadumbre Equilibrada" - Defensa moderada
        register(registry, EmotionType.CULPA, EmotionType.TRISTEZA,
            SynergyEffect.builder()
                .name("Pesadumbre Equilibrada")
                .defense(1.05)
                .build()
        );

        // Ira + Calma: "Cólera Serenada" - Defensa a cambio de velocidad
        register(registry, EmotionType.IRA, EmotionType.CALMA,
            SynergyEffect.builder()
                .name("Cólera Serenada")
                .defense(1.10)
                .speed(0.95)
                .build()
        );

        // ===== SINERGIAS MIXTAS (BALANCEADAS) =====

        // Alegría + Culpa: "Calma Turbulenta" - Ligera alteración
        register(registry, EmotionType.ALEGRIA, EmotionType.CULPA,
            SynergyEffect.builder()
                .name("Calma Turbulenta")
                .damage(1.05)
                .defense(0.95)
                .speed(0.90)
                .build()
        );

        // Alegría + Tristeza: "Melancolía Alegre" - Balance defensa/velocidad
        register(registry, EmotionType.ALEGRIA, EmotionType.TRISTEZA,
            SynergyEffect.builder()
                .name("Melancolía Alegre")
                .defense(1.05)
                .speed(0.95)
                .build()
        );

        // Esperanza + Ira: "Enojo Iluminado" - Velocidad aumentada
        register(registry, EmotionType.ESPERANZA, EmotionType.IRA,
            SynergyEffect.builder()
                .name("Enojo Iluminado")
                .speed(1.05)
                .build()
        );

        // Esperanza + Miedo: "Valor Tembloroso" - Daño con stun
        register(registry, EmotionType.ESPERANZA, EmotionType.MIEDO,
            SynergyEffect.builder()
                .name("Valor Tembloroso")
                .damage(1.05)
                .stun(1)
                .build()
        );

        // Esperanza + Rabia: "Furia Esperanzada" - Curación pero lento
        register(registry, EmotionType.ESPERANZA, EmotionType.RABIA,
            SynergyEffect.builder()
                .name("Furia Esperanzada")
                .speed(0.90)
                .healOverTime(4, 3)
                .build()
        );

        // Culpa + Esperanza: "Redención Frágil" - Silencio aplicado
        register(registry, EmotionType.CULPA, EmotionType.ESPERANZA,
            SynergyEffect.builder()
                .name("Redención Frágil")
                .debuff("silence", 1)
                .build()
        );

        // Culpa + Ira: "Cólera Avergonzada" - Velocidad reducida
        register(registry, EmotionType.CULPA, EmotionType.IRA,
            SynergyEffect.builder()
                .name("Cólera Avergonzada")
                .speed(0.90)
                .build()
        );

        // Ira + Tristeza: "Cólera Melancólica" - Defensa baja, velocidad alta
        register(registry, EmotionType.IRA, EmotionType.TRISTEZA,
            SynergyEffect.builder()
                .name("Cólera Melancólica")
                .defense(0.90)
                .speed(1.05)
                .build()
        );

        // Miedo + Rabia: "Pavor Rabioso" - Silencio aplicado
        register(registry, EmotionType.MIEDO, EmotionType.RABIA,
            SynergyEffect.builder()
                .name("Pavor Rabioso")
                .debuff("silence", 1)
                .build()
        );

        // Miedo + Tristeza: "Terror Doloroso" - Velocidad reducida
        register(registry, EmotionType.MIEDO, EmotionType.TRISTEZA,
            SynergyEffect.builder()
                .name("Terror Doloroso")
                .speed(0.90)
                .build()
        );

        // Miedo + Calma: "Calma Fóbica" - Curación con silencio
        register(registry, EmotionType.MIEDO, EmotionType.CALMA,
            SynergyEffect.builder()
                .name("Calma Fóbica")
                .healOverTime(3, 2)
                .debuff("silence", 1)
                .build()
        );

        // Rabia + Calma: "Ira Apacible" - Velocidad reducida
        register(registry, EmotionType.RABIA, EmotionType.CALMA,
            SynergyEffect.builder()
                .name("Ira Apacible")
                .speed(0.90)
                .build()
        );

        // Tristeza + Calma: "Melancolía Serena" - Defensa con lentitud extrema
        register(registry, EmotionType.TRISTEZA, EmotionType.CALMA,
            SynergyEffect.builder()
                .name("Melancolía Serena")
                .defense(1.05)
                .speed(0.80)
                .debuff("slow", 2)
                .build()
        );

        return Collections.unmodifiableMap(registry);
    }

    private static void register(Map<EmotionPair, SynergyEffect> registry,
                                 EmotionType a, EmotionType b,
                                 SynergyEffect effect) {
        registry.put(new EmotionPair(a, b), effect);
    }

    // ==================== API PÚBLICA ====================

    /**
     * Obtiene todas las sinergias activas para una lista de emociones.
     * Procesa cada par único de emociones.
     *
     * @param activeTypes Lista de tipos de emoción activos
     * @return Lista de efectos de sinergia aplicables
     */
    public static List<SynergyEffect> getSynergies(List<EmotionType> activeTypes) {
        if (activeTypes == null || activeTypes.size() < 2) {
            return Collections.emptyList();
        }

        List<SynergyEffect> results = new ArrayList<>();

        // Procesar todos los pares únicos
        for (int i = 0; i < activeTypes.size(); i++) {
            for (int j = i + 1; j < activeTypes.size(); j++) {
                EmotionPair pair = new EmotionPair(activeTypes.get(i), activeTypes.get(j));
                SynergyEffect effect = SYNERGY_REGISTRY.get(pair);

                if (effect != null) {
                    results.add(effect);
                }
            }
        }

        return results;
    }

    /**
     * Obtiene la sinergia específica entre dos emociones (si existe).
     *
     * @return El efecto de sinergia, o null si no hay sinergia definida
     */
    public static SynergyEffect getSynergy(EmotionType a, EmotionType b) {
        if (a == null || b == null || a == b) {
            return null;
        }
        return SYNERGY_REGISTRY.get(new EmotionPair(a, b));
    }

    /**
     * Verifica si existe una sinergia entre dos emociones.
     */
    public static boolean hasSynergy(EmotionType a, EmotionType b) {
        return getSynergy(a, b) != null;
    }

    /**
     * Obtiene todas las emociones que tienen sinergia con el tipo dado.
     */
    public static Set<EmotionType> getCompatibleEmotions(EmotionType type) {
        if (type == null) {
            return Collections.emptySet();
        }

        return SYNERGY_REGISTRY.keySet().stream()
            .filter(pair -> pair.first == type || pair.second == type)
            .map(pair -> pair.first == type ? pair.second : pair.first)
            .collect(Collectors.toSet());
    }

    /**
     * Obtiene estadísticas del registro de sinergias.
     */
    public static SynergyStats getStats() {
        return new SynergyStats(
            SYNERGY_REGISTRY.size(),
            countSynergiesByCategory()
        );
    }

    private static Map<String, Integer> countSynergiesByCategory() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("Positivas", 0);
        counts.put("Agresivas", 0);
        counts.put("Defensivas", 0);
        counts.put("Mixtas", 0);

        for (SynergyEffect effect : SYNERGY_REGISTRY.values()) {
            String category = categorizeEffect(effect);
            counts.put(category, counts.get(category) + 1);
        }

        return counts;
    }

    private static String categorizeEffect(SynergyEffect effect) {
        if (effect.getHotAmount() > 0) return "Positivas";
        if (effect.getDamageMultiplier() > 1.05) return "Agresivas";
        if (effect.getDefenseMultiplier() > 1.05) return "Defensivas";
        return "Mixtas";
    }

    // ==================== CLASE DE ESTADÍSTICAS ====================

    public static class SynergyStats {
        private final int totalSynergies;
        private final Map<String, Integer> byCategory;

        SynergyStats(int total, Map<String, Integer> byCategory) {
            this.totalSynergies = total;
            this.byCategory = Collections.unmodifiableMap(byCategory);
        }

        public int getTotalSynergies() { return totalSynergies; }
        public Map<String, Integer> getByCategory() { return byCategory; }

        @Override
        public String toString() {
            return String.format("Total: %d | Positivas: %d | Agresivas: %d | Defensivas: %d | Mixtas: %d",
                totalSynergies,
                byCategory.get("Positivas"),
                byCategory.get("Agresivas"),
                byCategory.get("Defensivas"),
                byCategory.get("Mixtas")
            );
        }
    }
}