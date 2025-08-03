package dnd.events;

import java.util.Random;

public enum EEvent {
  COMBAT("Сражение"),
  NEGATIVE("Негативное"),
  POSITIVE("Позитивное"),
  NOTHING("Ничего");


  private final String type;

  EEvent(String type) {
    this.type = type;
  }

  public static EEvent getRandom() {
    int value = new Random().nextInt(1, 21);

    return switch (value) {
      case 1, 2, 3, 4 -> EEvent.COMBAT;
      case 8, 9, 10 -> EEvent.NEGATIVE;
      case 13, 14, 15 -> EEvent.POSITIVE;
      default -> EEvent.NOTHING;
    };
  }

  public String getType() {
    return type;
  }
}