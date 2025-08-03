package dnd.directions;

import java.util.Random;

public enum EDirection {
  RIGHT("Направо"),
  LEFT("Налево"),
  FORWARD("Вперёд"),
  BACKWARD("Назад"),
  FORWARD_RIGHT("Вперёд направо"),
  FORWARD_LEFT("Вперёд налево"),
  BACKWARD_RIGHT("Назад направо"),
  BACKWARD_LEFT("Назад налево"),
  LOOP("Кольцо");

  private final String localization;

  EDirection(String localization) {
    this.localization = localization;
  }

  /**
   * Получить случайное направление
   */
  public static EDirection getRandom() {
    int value = new Random().nextInt(1, 21);

    return switch (value) {
      case 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 -> EDirection.RIGHT;
      case 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24 -> EDirection.LEFT;
      case 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36 -> EDirection.FORWARD;
      case 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48 -> EDirection.BACKWARD;
      case 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60 -> EDirection.FORWARD_RIGHT;
      case 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72 -> EDirection.FORWARD_LEFT;
      case 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84 -> EDirection.BACKWARD_RIGHT;
      case 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96 -> EDirection.BACKWARD_LEFT;
      default -> EDirection.LOOP;
    };
  }

  /**
   * Получить противоположное направление
   */
  public EDirection getOpposite() {
    return switch (this) {
      case RIGHT -> LEFT;
      case LEFT -> RIGHT;
      case FORWARD -> BACKWARD;
      case BACKWARD -> FORWARD;
      case FORWARD_RIGHT -> FORWARD_LEFT;
      case FORWARD_LEFT -> FORWARD_RIGHT;
      case BACKWARD_RIGHT -> BACKWARD_LEFT;
      case BACKWARD_LEFT -> BACKWARD_RIGHT;
      case LOOP -> LOOP;
    };
  }

  public String getLocalization() {
    return this.localization;
  }
}
