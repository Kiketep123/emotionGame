package roguelike_emotions.characters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import roguelike_emotions.combat.CombatEntity;
import roguelike_emotions.combat.EmotionalTurnProcessor;
import roguelike_emotions.effects.Buff;
import roguelike_emotions.effects.Debuff;
import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.effects.OverTimeHeal;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionInstanceFactory;
import roguelike_emotions.map.EmotionNode;
import roguelike_emotions.utils.CombatLogger;

/**
 * Clase Player adaptada para implementar CombatEntity.
 * Mantiene toda la funcionalidad original + sistema unificado de combate.
 */
public class Player implements CombatEntity, Cloneable {

    // ==================== ATRIBUTOS EXISTENTES ====================

    private int vida = 100;
    private int maxVida = 100;
    private int veneno = 0;
    private int defensaBase = 25;
    private int danyoBase = 50;
    private double velBase = 10;

    // Emociones
    private List<EmotionInstance> emocionesActivas = new ArrayList<>();
    private EmotionNode nodoMentalActual;

    // Efectos
    private Map<String, Buff> activeBuffs = new HashMap<>();
    private Map<String, Debuff> activeDebuffs = new HashMap<>();
    private List<OverTimeHeal> healOverTimeEffects = new ArrayList<>();
    private List<EffectDetail> efectosActivos = new ArrayList<>();

    // Factory y taunt
    private EmotionInstanceFactory emotionFactory = new EmotionInstanceFactory();
    private Enemy tauntSource;
    private int tauntTurnsRemaining = 0;

    // Habilidades (para compatibilidad con CombatEntity)
    private boolean canUseAbility = true;
    private int abilityCooldown = 0;

    // ==================== IMPLEMENTACIÓN DE CombatEntity ====================

    @Override
    public String getNombre() {
        return "Player"; // o añade un campo 'nombre' si lo necesitas
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PLAYER;
    }

    @Override
    public int getHealth() {
        return vida;
    }

    @Override
    public void setHealth(int health) {
        this.vida = Math.max(0, Math.min(health, maxVida));
    }

    @Override
    public int getMaxHealth() {
        return maxVida;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxVida = Math.max(1, maxHealth);
        this.vida = Math.min(this.vida, this.maxVida);
    }

    @Override
    public int getBaseDamage() {
        return danyoBase;
    }

    @Override
    public void setBaseDamage(int damage) {
        this.danyoBase = Math.max(0, damage);
    }

    @Override
    public int getBaseDefense() {
        return defensaBase;
    }

    @Override
    public void setBaseDefense(int defense) {
        this.defensaBase = Math.max(0, defense);
    }

    @Override
    public int getSpeed() {
        return (int) velBase;
    }

    @Override
    public void setSpeed(int speed) {
        this.velBase = Math.max(1, speed);
    }

    @Override
    public List<EmotionInstance> getEmocionesActivas() {
        return emocionesActivas;
    }

    @Override
    public void addEmocion(EmotionInstance emotion) {
        añadirEmocion(emotion);
    }

    @Override
    public void removeEmocion(EmotionInstance emotion) {
        eliminarEmocion(emotion);
    }

    @Override
    public void clearEmociones() {
        emocionesActivas.clear();
        efectosActivos.clear();
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
        return vida > 0;
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
        int actualDamage = Math.min(amount, vida);
        vida -= actualDamage;
        return actualDamage;
    }

    @Override
    public void heal(int amount) {
        vida = Math.min(vida + amount, maxVida);
    }

    @Override
    public boolean canUseActive() {
        return canUseAbility;
    }

    @Override
    public void setCanUseActive(boolean canUse) {
        this.canUseAbility = canUse;
    }

    @Override
    public int getCooldownTurns() {
        return abilityCooldown;
    }

    @Override
    public void setCooldownTurns(int turns) {
        this.abilityCooldown = Math.max(0, turns);
    }

    // ==================== MÉTODO DE TURNO REFACTORIZADO ====================

    /**
     * Procesa el turno emocional usando el sistema unificado.
     * Mantiene compatibilidad con el método original.
     */
    public void tickTurnoEmocional() {
        // Usar el sistema unificado (sin target porque el jugador procesa solo sus efectos)
       EmotionalTurnProcessor.processTurn(this, null);

        // Lógica adicional específica del jugador
        processPlayerSpecificEffects();
    }

    /**
     * Efectos específicos del jugador que no están en el sistema unificado.
     */
    private void processPlayerSpecificEffects() {
        // Veneno
        if (veneno > 0) {
            vida -= veneno;
            CombatLogger.get().log("[Player] Veneno -" + veneno + " vida → " + vida);
        }

        // Taunt
        if (tauntSource != null) {
            tauntTurnsRemaining--;
            if (tauntTurnsRemaining <= 0 || !tauntSource.isAlive()) {
                CombatLogger.get().log("[Player] Ya no estás bajo Provocación");
                tauntSource = null;
                tauntTurnsRemaining = 0;
            }
        }
    }

    // ==================== MÉTODOS ORIGINALES DE EMOCIONES ====================

    /**
     * Añade una emoción al jugador (método original).
     */
    public void añadirEmocion(EmotionInstance e) {
        if (e != null && !emocionesActivas.contains(e)) {
            emocionesActivas.add(e);
            for (EffectDetail ed : e.getEfectos()) {
                efectosActivos.add(EffectDetail.fromConfig(ed.getTipo()));
            }
        }
    }

    public void eliminarEmocion(EmotionInstance e) {
        emocionesActivas.remove(e);
    }

    public void usarEmocion(EmotionInstance emocion) {
        if (!emocionesActivas.contains(emocion)) {
            CombatLogger.get().log(
                "[Player] Emoción no disponible: " + emocion.getNombre()
            );
            return;
        }

        for (EffectDetail ed : emocion.getEfectos()) {
            ed.aplicarA(this);
            efectosActivos.add(new EffectDetail(
                ed.getTipo(),
                ed.getIntensidad(),
                ed.getProbabilidad(),
                ed.getRemainingTurns()
            ));
            CombatLogger.get().log(
                "[Player] Aplica efecto de emoción: " + ed.getNombre()
            );
        }
    }

    // ==================== MÉTODOS DE EFECTOS ====================

    public void curar(int cantidad) {
        vida += cantidad;
        CombatLogger.get().log("[Player] +" + cantidad + " vida → " + vida);
    }

    public void intoxicar(int nivel) {
        veneno += nivel;
        CombatLogger.get().log("[Player] +" + nivel + " veneno → " + veneno);
    }

    public void defender(int nivel) {
        defensaBase += nivel;
        CombatLogger.get().log("[Player] +" + nivel + " defensa → " + defensaBase);
    }

    public void modifySpeed(double multiplier) {
        velBase = Math.max(0.1, velBase * multiplier);
        CombatLogger.get().log(
            "[Player] Velocidad ajustada " + multiplier + " → " + velBase
        );
    }

    public void applyBuff(String type, double multiplier, int duration) {
        activeBuffs.put(type, new Buff(type, multiplier, duration));
        CombatLogger.get().log(
            "[Player] Buff " + type + " con multiplicador " +
            multiplier + " por " + duration + " turnos"
        );
    }

    public void applyDebuff(String type, double multiplier, int duration) {
        activeDebuffs.put(type, new Debuff(type, multiplier, duration));
        CombatLogger.get().log(
            "[Player] Debuff " + type + " por " + duration + " turnos"
        );
    }

    public void applyHealOverTime(int amount, int turns) {
        healOverTimeEffects.add(new OverTimeHeal(amount, turns));
        CombatLogger.get().log(
            "[Player] Curación continua " + amount + " por " + turns + " turnos"
        );
    }

    // ==================== MÉTODOS DE COMBATE ====================

    /**
     * Ataca a un enemigo (método original).
     */
    public void attack(Enemy enemigo) {
        if (!canAct()) {
            CombatLogger.get().log("[Player] No puede actuar");
            return;
        }

        int damage = getDanyo();

        // Forzar taunt
        if (tauntSource != null && enemigo != tauntSource) {
            CombatLogger.get().log(
                "[Player] Taunt activo: debes atacar a " +
                tauntSource.getNombre()
            );
            return;
        }

        // Crear ataque con efectos
        Attack atk = new Attack();
        for (EffectDetail ed : efectosActivos) {
            EffectDetail copia = new EffectDetail(
                ed.getTipo(),
                ed.getIntensidad(),
                ed.getProbabilidad(),
                ed.getRemainingTurns()
            );
            atk.addEffect(copia);
        }
        atk.applyToEnemy(enemigo, damage);
    }

    /**
     * Recibe daño (método original con lógica de defensa).
     */
    public void recibirDanyo(int cantidad) {
        int neto;
        int def = getDefensa();

        if (def > 0) {
            if (cantidad <= def) {
                defensaBase -= cantidad;
                neto = 0;
            } else {
                neto = cantidad - def;
                defensaBase = 0;
            }
        } else {
            neto = cantidad;
        }

        vida = Math.max(0, vida - neto);

    }

    // ==================== GETTERS CON BUFFS ====================

    /**
     * Devuelve el daño real considerando buffs.
     */
    public int getDanyo() {
        double dmg = danyoBase;
        Buff b = activeBuffs.get("damageBoost");
        if (b != null) {
            dmg *= b.getMultiplier();
        }
        return (int) Math.max(1, dmg);
    }

    /**
     * Devuelve la defensa real considerando buffs.
     */
    public int getDefensa() {
        double def = defensaBase;
        Buff b = activeBuffs.get("defenseBoost");
        if (b != null) {
            def *= b.getMultiplier();
        }
        return (int) Math.max(0, def);
    }

    /**
     * Devuelve la velocidad real considerando buffs.
     */
    public double getVelocidad() {
        double vel = velBase;
        Buff b = activeBuffs.get("speedBoost");
        if (b != null) {
            vel *= b.getMultiplier();
        }
        return Math.max(0.1, vel);
    }

    // ==================== MÉTODOS DE TAUNT ====================

    public void applyTaunt(int duration, Enemy source) {
        this.tauntSource = source;
        this.tauntTurnsRemaining = duration;
        CombatLogger.get().log(
            "[Player] Queda taunteado por " + duration +
            " turnos (de " + source.getNombre() + ")"
        );
    }

    public void setTauntSource(Enemy enemigo) {
        this.tauntSource = enemigo;
        CombatLogger.get().log(
            "[Player] Ha sido provocado por " + enemigo.getNombre()
        );
    }

    public Enemy getTauntSource() {
        return tauntSource;
    }

    // ==================== MÉTODOS DE HERENCIA DE BUFFS ====================

    /**
     * Hereda un porcentaje de los buffs de un enemigo.
     */
    public void inheritBuffsFrom(Enemy fuente, double ratio) {
        for (Map.Entry<String, Buff> entry : fuente.getActiveBuffs().entrySet()) {
            String type = entry.getKey();
            Buff buffEnemigo = entry.getValue();
            double mult = buffEnemigo.getMultiplier();
            int turns = buffEnemigo.getRemainingTurns();
            applyBuff(type, mult * ratio, turns);
        }
        CombatLogger.get().log(
            "[Player] Hereda buffs de " + fuente.getNombre() +
            " (ratio " + ratio + ")"
        );
    }

    public void reduceirDebuffs() {
        Iterator<Debuff> it = activeDebuffs.values().iterator();
        while (it.hasNext()) {
            Debuff d = it.next();
            if (d.reducirDuracion()) {
                it.remove();
                CombatLogger.get().log(
                    "[Player] Debuff " + d.getType() + " expirado tras reducción"
                );
            }
        }
    }

    // ==================== GETTERS/SETTERS ADICIONALES ====================

    public int getVeneno() {
        return veneno;
    }

    public void setVeneno(int veneno) {
        this.veneno = Math.max(0, veneno);
    }

    public EmotionNode getNodoMentalActual() {
        return nodoMentalActual;
    }

    public void setNodoMentalActual(EmotionNode nodo) {
        this.nodoMentalActual = nodo;
    }

    // ==================== RESET DE ESTADO ====================

    /**
     * Resetea el estado del jugador (al reiniciar oleada o juego).
     */
    public void resetState() {
        vida = 100;
        maxVida = 100;
        veneno = 0;
        defensaBase = 25;
        danyoBase = 50;
        velBase = 10.0;

        emocionesActivas.clear();
        efectosActivos.clear();
        activeBuffs.clear();
        activeDebuffs.clear();
        healOverTimeEffects.clear();

        tauntSource = null;
        tauntTurnsRemaining = 0;
        canUseAbility = true;
        abilityCooldown = 0;

        // Emoción inicial
        EmotionInstance emocionInicial = emotionFactory.generarProcedural();
        añadirEmocion(emocionInicial);
    }

    // ==================== CLONEABLE ====================

    @Override
    public Player clone() {
        try {
            Player copia = (Player) super.clone();

            // Clonar colecciones mutables
            copia.emocionesActivas = new ArrayList<>(this.emocionesActivas);
            copia.efectosActivos = new ArrayList<>(this.efectosActivos);
            copia.activeBuffs = new HashMap<>(this.activeBuffs);
            copia.activeDebuffs = new HashMap<>(this.activeDebuffs);
            copia.healOverTimeEffects = new ArrayList<>(this.healOverTimeEffects);

            return copia;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError("No debería ocurrir", ex);
        }
    }
}