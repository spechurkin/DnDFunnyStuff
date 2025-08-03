package dnd.dungeon;

import dnd.directions.EDirection;
import dnd.events.EEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Room {
  private final String id;
  private final String name;
  private final Map<EDirection, Room> exits;
  private final EEvent eventType;
  private int x, y; // координаты комнаты на карте (для визуализации и логики)

  public Room(String id, String name, EEvent eventType, int x, int y) {
    this.id = id;
    this.name = name;
    this.eventType = eventType;
    this.exits = new HashMap<>();
    this.x = x;
    this.y = y;
  }

  // Двунаправленное соединение
  public void connectTo(Room otherRoom, EDirection direction) {
    if (otherRoom == null) throw new IllegalArgumentException("Room can't be null");
    this.exits.put(direction, otherRoom);
    otherRoom.exits.put(direction.getOpposite(), this);
  }

  // Однонаправленное соединение
  public void connectOneWay(Room toRoom, EDirection direction) {
    if (toRoom == null) throw new IllegalArgumentException("Room can't be null");
    this.exits.put(direction, toRoom);
  }

  public boolean hasExit(EDirection direction) {
    return exits.containsKey(direction);
  }

  public Room getExit(EDirection direction) {
    return exits.get(direction);
  }

  public Set<EDirection> getAvailableExits() {
    return exits.keySet();
  }

  public boolean isCircularCorridor(EDirection direction) {
    return exits.get(direction) == this;
  }

  public void createCircularCorridor(EDirection direction) {
    exits.put(direction, this);
  }

  // Геттеры координат
  public int getX() {
    return x;
  }

  // Сеттеры координат (если нужно менять)
  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public EEvent getEventType() {
    return eventType;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(name).append(" (").append(id).append(") at (").append(x).append(",").append(y).append(")\n");
    sb.append(eventType.getType()).append("\n");
    sb.append("Exits:\n");
    for (Map.Entry<EDirection, Room> e : exits.entrySet()) {
      EDirection d = e.getKey();
      Room r = e.getValue();
      if (r == this) {
        sb.append("  ").append(d.name().toLowerCase()).append(" -> кольцевой коридор\n");
      } else {
        sb.append("  ").append(d.name().toLowerCase()).append(" -> ").append(r.getName()).append("\n");
      }
    }
    return sb.toString();
  }
}
