package dnd.dungeon;

import dnd.directions.EDirection;
import dnd.events.EEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DungeonGenerator {
  private static final int GRID_STEP = 200; // расстояние между комнатами на карте
  private final List<Room> rooms;
  private final Random random;
  private int roomCounter;

  public DungeonGenerator() {
    rooms = new ArrayList<>();
    random = new Random();
    roomCounter = 0;
  }

  public Dungeon generateDungeon(int numberOfRooms) {
    rooms.clear();
    roomCounter = 0;

    // Создаем первую комнату в центре (0,0)
    Room firstRoom = createRoom(0, 0);
    rooms.add(firstRoom);

    // Для остальных комнат будем размещать рядом с уже существующими
    for (int i = 1; i < numberOfRooms; i++) {
      // Выбираем случайную уже созданную комнату для "привязки"
      Room baseRoom = rooms.get(random.nextInt(rooms.size()));

      // Выбираем случайное свободное направление для новой комнаты
      EDirection direction = findFreeDirection(baseRoom);

      if (direction == null) {
        // Если свободных направлений нет, попробуем другую комнату
        i--;
        continue;
      }

      // Вычисляем координаты новой комнаты рядом с baseRoom
      int newX = baseRoom.getX() + dx(direction) * GRID_STEP;
      int newY = baseRoom.getY() + dy(direction) * GRID_STEP;

      // Проверяем, что в этих координатах нет другой комнаты
      if (findRoomByCoordinates(newX, newY) != null) {
        i--;
        continue;
      }

      Room newRoom = createRoom(newX, newY);
      rooms.add(newRoom);

      // Соединяем комнаты (50% двунаправленная, 50% однонаправленная)
      if (random.nextBoolean()) {
        baseRoom.connectTo(newRoom, direction);
      } else {
        baseRoom.connectOneWay(newRoom, direction);
      }
    }

    // Создаем кольцевые коридоры для части комнат
    createCircularCorridors();

    return new Dungeon(rooms);
  }

  private Room createRoom(int x, int y) {
    String id = "room_" + roomCounter++;
    String name = "Комната " + roomCounter;
    EEvent event = EEvent.getRandom();
    return new Room(id, name, event, x, y);
  }

  private EDirection findFreeDirection(Room room) {
    List<EDirection> freeDirs = new ArrayList<>();
    for (EDirection d : EDirection.values()) {
      int nx = room.getX() + dx(d) * GRID_STEP;
      int ny = room.getY() + dy(d) * GRID_STEP;
      if (findRoomByCoordinates(nx, ny) == null) {
        freeDirs.add(d);
      }
    }
    if (freeDirs.isEmpty()) return null;
    return freeDirs.get(random.nextInt(freeDirs.size()));
  }

  private Room findRoomByCoordinates(int x, int y) {
    for (Room r : rooms) {
      if (r.getX() == x && r.getY() == y) return r;
    }
    return null;
  }

  private int dx(EDirection d) {
    return switch (d) {
      case RIGHT, BACKWARD_RIGHT, FORWARD_RIGHT -> 1;
      case LEFT, BACKWARD_LEFT, FORWARD_LEFT -> -1;
      default -> 0;
    };
  }

  private int dy(EDirection d) {
    return switch (d) {
      case FORWARD, FORWARD_LEFT, FORWARD_RIGHT -> -1;
      case BACKWARD, BACKWARD_LEFT, BACKWARD_RIGHT -> 1;
      default -> 0;
    };
  }

  private void createCircularCorridors() {
    int circularCount = Math.max(1, rooms.size() / 5);
    for (int i = 0; i < circularCount; i++) {
      Room room = rooms.get(random.nextInt(rooms.size()));
      EDirection dir = EDirection.getRandom();
      if (!room.hasExit(dir)) {
        room.createCircularCorridor(dir);
      }
    }
  }
}
