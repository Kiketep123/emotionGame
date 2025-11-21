package roguelike_emotions.characters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roguelike_emotions.combat.CombatEntity;
import roguelike_emotions.combat.EmotionalTurnProcessor;
import roguelike_emotions.combat.PassiveAbilityHolder;
import roguelike_emotions.effects.Buff;
import roguelike_emotions.effects.Debuff;
import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.effects.OverTimeHeal;
import roguelike_emotions.mainMechanics.EmotionDominanceMatrix;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionType;
import roguelike_emotions.utils.CombatLogger;

/**
 * Clase Enemy adaptada para implementar CombatEntity.
 * Mantiene toda la funcionalidad original + sistema unificado de combate.
 */
public class Enemy implements CombatEntity, PassiveAbilityHolder {

    // ==================== ATRIBUTOS EXISTENTES ====================

    private String nombre;
    private int baseDamage;
    private int baseDefense;
    private double baseSpeed;
    private EnemyRole role;
    private int health;
    private int maxHealth;

    private EmotionInstance estadoEmocionalActual;
    private List<EmotionInstance> emocionesActivas = new ArrayList<>();
    private List<EffectDetail> efectosActivos = new ArrayList<>();
    private List<OverTimeHeal> healOverTimeEffects = new ArrayList<>();

    private Map<String, Buff> activeBuffs = new HashMap<>();
    private Map<String, Debuff> activeDebuffs = new HashMap<>();

    private static EmotionDominanceMatrix dominanceMatrix;

    // Cooldown para habilidades
    private int cooldownTurns = 0;
    private boolean pasivasAplicadas = false;

    // Constantes
    private static final String DAMAGE_BOOST = "damageBoost";
    private static final String DEFENSE_BOOST = "defenseBoost";
    private static final String SPEED_BOOST = "speedBoost";

    // ==================== CONSTRUCTOR ====================

    public Enemy(String nombre, int maxHealth, int baseDamage, int baseDefense,
                 double baseSpeed, EnemyRole role) {
        this.nombre = nombre;
        this.maxHealth = maxHealth;
        this.health = maxHealth; // Inicializar health con maxHealth
        //TODO preubas
        this.baseDamage = 10;
        this.baseDefense = baseDefense;
        this.baseSpeed = baseSpeed;
        this.role = role;
    }

    // ==================== IMPLEMENTACIÓN DE CombatEntity ====================

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ENEMY;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public int getBaseDamage() {
        return baseDamage;
    }

    @Override
    public void setBaseDamage(int damage) {
        this.baseDamage = Math.max(0, damage);
    }

    @Override
    public int getBaseDefense() {
        return baseDefense;
    }

    @Override
    public void setBaseDefense(int defense) {
        this.baseDefense = Math.max(0, defense);
    }

    @Override
    public int getSpeed() {
        return (int) baseSpeed;
    }

    @Override
    public void setSpeed(int speed) {
        this.baseSpeed = Math.max(1, speed);
    }

    @Override
    public List<EmotionInstance> getEmocionesActivas() {
        return emocionesActivas;
    }

    @Override
    public void addEmocion(EmotionInstance emotion) {
        if (!emocionesActivas.contains(emotion)) {
            emocionesActivas.add(emotion);
        }
    }

    @Override
    public void removeEmocion(EmotionInstance emotion) {
        emocionesActivas.remove(emotion);
    }

    @Override
    public void clearEmociones() {
        emocionesActivas.clear();
    }

    @Override
    public List<EffectDetail> getEfectosActivos() {
        return efectosActivos;
    }

    @Override
    public List<OverTimeHeal> getHealOverTimeEffects() {
        return healOverTimeEffects;
    }

    @Override
    public Map<String, Buff> getActiveBuffs() {
        return activeBuffs;
    }

    @Override
    public Map<String, Debuff> getActiveDebuffs() {
        return activeDebuffs;
    }

    @Override
    public boolean isAlive() {
        return health > 0;
    }

    @Override
    public boolean isStunned() {
        return activeDebuffs.containsKey("stun") ||
               activeDebuffs.containsKey("Stun") ||
               activeDebuffs.containsKey("aturdimiento");
    }

    @Override
    public boolean canAct() {
        return isAlive() && !isStunned();
    }

    @Override
    public int takeDamage(int amount) {
        int actualDamage = Math.min(amount, health);
        health -= actualDamage;
        return actualDamage;
    }

    @Override
    public void heal(int amount) {
        health = Math.min(health + amount, maxHealth);
    }

    @Override
    public boolean canUseActive() {
        return pasivasAplicadas;
    }

    @Override
    public void setCanUseActive(boolean canUse) {
        this.pasivasAplicadas = canUse;
    }

    @Override
    public int getCooldownTurns() {
        return cooldownTurns;
    }

    @Override
    public void setCooldownTurns(int turns) {
        this.cooldownTurns = Math.max(0, turns);
    }

    // ==================== MÉTODO DE TURNO REFACTORIZADO ====================

    /**
     * Procesa el turno emocional del enemigo 
     */
    public void tickTurnoEmocional(Player player) {
        EmotionalTurnProcessor.processTurn(this, player);
    }

    // ==================== IMPLEMENTACIÓN DE PassiveAbilityHolder ====================

    @Override
    public void aplicarPasivas() {
        aplicarPasivasUnaSolaVez();
    }

    /**
     * Aplica pasivas de rol (solo una vez por emoción base).
     */
    private void aplicarPasivasUnaSolaVez() {
        if (pasivasAplicadas || estadoEmocionalActual == null) {
            return;
        }

        EmotionType tipoBase = estadoEmocionalActual.getTipoBase();

        switch (role) {
            case TANK:
                if (tipoBase == EmotionType.CALMA) {
                    baseDefense = (int) (baseDefense * 1.10);
                    CombatLogger.get().log(
                        "[Enemy " + nombre + "] Pasiva TANK: +10% defensa por CALMA"
                    );
                }
                break;

            case DPS:
                if (tipoBase == EmotionType.IRA || tipoBase == EmotionType.RABIA) {
                    baseDamage = (int) (baseDamage * 1.15);
                    CombatLogger.get().log(
                        "[Enemy " + nombre + "] Pasiva DPS: +15% daño por IRA/RABIA"
                    );
                }
                break;

            case SUPPORT:
                if (tipoBase == EmotionType.ALEGRIA) {
                    int heal = (int) (health * 0.10);
                    health = Math.min(health + heal, maxHealth);
                    CombatLogger.get().log(
                        "[Enemy " + nombre + "] Pasiva SUPPORT: cura +10% vida por ALEGRÍA"
                    );
                }
                break;

        }

        pasivasAplicadas = true;
    }

    // ==================== MÉTODOS DE COMBATE ORIGINALES ====================

    /**
     * Método de ataque original (mantiene compatibilidad).
     */
    public void atacar(Player jugador) {
        if (!canAct()) {
            CombatLogger.get().log("[Enemy " + nombre + "] No puede actuar");
            return;
        }

        int dmg = getDamageAgainst(jugador);
        Attack atk = new Attack();
        efectosActivos.forEach(atk::addEffect);
        atk.applyToPlayer(jugador, dmg);

        // Habilidad activa de rol
        usarActiva(jugador);
    }

    /**
     * Calcula daño contra el jugador según dominancia emocional.
     */
    public int getDamageAgainst(Player jugador) {
        double factor = 1.0;

        if (estadoEmocionalActual != null && dominanceMatrix != null) {
            EmotionType tE = estadoEmocionalActual.getTipoBase();
            double suma = 0;

            for (EmotionInstance ej : jugador.getEmocionesActivas()) {
                EmotionType tJ = ej.getTipoBase();
                double pEJ = dominanceMatrix.getPeso(tE, tJ);
                double pJE = dominanceMatrix.getPeso(tJ, tE);
                suma += (pEJ + pJE) / 2;
            }

            if (!jugador.getEmocionesActivas().isEmpty()) {
                factor = suma / jugador.getEmocionesActivas().size();
            }
        }

        return (int) Math.max(0, getDanyo() * factor);
    }

    /**
     * Usa la habilidad activa según el rol.
     */
    public void usarActiva(Player jugador) {
        if (!pasivasAplicadas) {
            return;
        }

        CombatLogger.get().log(
            "[Enemy " + nombre + "] activa habilidad " + role.getActiveSkill()
        );

        switch (role) {
            case TANK:
                // Provocar: obliga al jugador a atacar a este enemigo
                jugador.applyTaunt(1, this);
                break;

            case DPS:
                // Golpe Crítico: daño x2 + stun
                int crit = baseDamage * 2;
                jugador.recibirDanyo(crit);
                jugador.applyDebuff("Stun", 1, 1);
                break;

            case SUPPORT:
                // Vínculo Emocional: hereda la mitad de los buffs
                jugador.inheritBuffsFrom(this, 0.5);
                break;

        }

        pasivasAplicadas = false;
        cooldownTurns = 3; // Cooldown de 3 turnos
    }

    /**
     * Recibe daño (método original con lógica de defensa).
     */
    public void recibirDanyo(int cantidad) {
        int neto;
        int defensaActual = getDefensa();

        if (defensaActual > 0) {
            if (cantidad <= defensaActual) {
                baseDefense -= cantidad;
                neto = 0;
            } else {
                neto = cantidad - defensaActual;
                baseDefense = 0;
            }
        } else {
            neto = cantidad;
        }

        health = Math.max(0, health - neto);

    }

    // ==================== GETTERS/SETTERS ORIGINALES ====================

    /**
     * Devuelve el daño real considerando buffs activos.
     */
    public int getDanyo() {
        double dmg = baseDamage;
        Buff b = activeBuffs.get(DAMAGE_BOOST);
        if (b != null) {
            dmg *= b.getMultiplier();
        }
        return (int) Math.max(1, dmg);
    }

    /**
     * Devuelve la defensa real considerando buffs activos.
     */
    public int getDefensa() {
        double def = baseDefense;
        Buff b = activeBuffs.get(DEFENSE_BOOST);
        if (b != null) {
            def *= b.getMultiplier();
        }
        return (int) Math.max(0, def);
    }

    /**
     * Devuelve la velocidad real considerando buffs activos.
     */
    public double getVelocidad() {
        double vel = baseSpeed;
        Buff b = activeBuffs.get(SPEED_BOOST);
        if (b != null) {
            vel *= b.getMultiplier();
        }
        return Math.max(0.1, vel);
    }

    public EnemyRole getRole() {
        return role;
    }

    public void setRole(EnemyRole role) {
        this.role = role;
    }

    public EmotionInstance getEstadoEmocionalActual() {
        return estadoEmocionalActual;
    }

    public void setEstadoEmocionalActual(EmotionInstance emotion) {
        this.estadoEmocionalActual = emotion;
        // Reset de pasivas cuando cambia la emoción base
        this.pasivasAplicadas = false;
    }

    public void setEstadoEmocional(EmotionInstance e) {
        setEstadoEmocionalActual(e);
    }

    public void addEmotion(EmotionInstance e) {
        addEmocion(e);
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = Math.max(1, maxHealth);
        this.health = Math.min(this.health, this.maxHealth);
    }

    public void setBaseSpeed(double baseSpeed) {
        this.baseSpeed = Math.max(0.1, baseSpeed);
    }

    public void setEmocionesActivas(List<EmotionInstance> emociones) {
        this.emocionesActivas = emociones;
    }

    public void setEfectosActivos(List<EffectDetail> efectos) {
        this.efectosActivos = efectos;
    }

    public void setHealOverTimeEffects(List<OverTimeHeal> effects) {
        this.healOverTimeEffects = effects;
    }

    public void setActiveBuffs(Map<String, Buff> buffs) {
        this.activeBuffs = buffs;
    }

    public void setActiveDebuffs(Map<String, Debuff> debuffs) {
        this.activeDebuffs = debuffs;
    }

    public boolean isPasivasAplicadas() {
        return pasivasAplicadas;
    }

    public void setPasivasAplicadas(boolean aplicadas) {
        this.pasivasAplicadas = aplicadas;
    }

    public void onTurnStart() {
        pasivasAplicadas = true;
    }

    // ==================== MÉTODOS ESTÁTICOS ====================

    public static void setDominanceMatrix(EmotionDominanceMatrix m) {
        dominanceMatrix = m;
    }

    public static EmotionDominanceMatrix getDominanceMatrix() {
        return dominanceMatrix;
    }
}