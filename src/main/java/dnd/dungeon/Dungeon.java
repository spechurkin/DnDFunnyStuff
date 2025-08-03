package dnd.dungeon;

import java.util.ArrayList;
import java.util.List;

public class Dungeon {
  private final List<Room> rooms;
  private Room startRoom;

  public Dungeon(List<Room> rooms) {
    this.rooms = new ArrayList<>(rooms);
    this.startRoom = rooms.isEmpty() ? null : rooms.get(0); // например, первая комната — стартовая
  }

  public List<Room> getRooms() {
    return new ArrayList<>(rooms);
  }

  public Room getStartRoom() {
    return startRoom;
  }

  public void setStartRoom(Room startRoom) {
    this.startRoom = startRoom;
  }
}

