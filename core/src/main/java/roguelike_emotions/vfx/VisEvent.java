package roguelike_emotions.vfx;

public sealed interface VisEvent permits DamageEvent, HealEvent, BuffAppliedEvent, DebuffAppliedEvent, TurnStepEvent,ComboMaxEvent {}
