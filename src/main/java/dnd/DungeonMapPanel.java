package dnd;

import dnd.directions.EDirection;
import dnd.dungeon.Dungeon;
import dnd.dungeon.DungeonGenerator;
import dnd.dungeon.Room;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;

public class DungeonMapPanel extends JPanel {
  private static final int ROOM_SIZE = 120; // размер комнаты
  private static final int MARGIN = 200;    // отступы для панелей, чтобы не обрезать
  private static final DungeonGenerator generator = new DungeonGenerator();
  private static final int rooms = 20;
  private static Dungeon dungeon;
  private static Room startRoom;

  public DungeonMapPanel() {
    dungeon = generator.generateDungeon(rooms);
    startRoom = dungeon.getStartRoom();
    // Вычислим предпочтительный размер панели с запасом по координатам комнат
    int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
    int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
    for (Room r : dungeon.getRooms()) {
      minX = Math.min(minX, r.getX());
      maxX = Math.max(maxX, r.getX());
      minY = Math.min(minY, r.getY());
      maxY = Math.max(maxY, r.getY());
    }
    int width = maxX - minX + ROOM_SIZE + 2 * MARGIN;
    int height = maxY - minY + ROOM_SIZE + 2 * MARGIN;
    setPreferredSize(new Dimension(width, height));
    setBackground(Color.WHITE);
  }

  // Тестовый запуск с JFrame и JScrollPane
  public static void main(String[] args) {
    JFrame frame = new JFrame("Карта подземелья");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());

    DungeonMapPanel mapPanel = new DungeonMapPanel();
    JScrollPane scrollPane = new JScrollPane(mapPanel);

    JButton regenerate = new JButton("Новое");
    regenerate.setPreferredSize(new Dimension(500, 100));
    regenerate.addActionListener(e -> {
      dungeon = generator.generateDungeon(rooms);
      mapPanel.setDungeon(dungeon);

      mapPanel.revalidate();
      mapPanel.repaint();
    });

    JButton imageButton = new JButton("Сохранить изображение");
    imageButton.setPreferredSize(new Dimension(500, 100));
    imageButton.addActionListener(e -> {
      try {
        mapPanel.exportToPNG(new File("dungeon_map.png"));
      } catch (IOException ignored) {
      }
    });

    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    topPanel.add(regenerate);
    topPanel.add(imageButton);

    frame.setLayout(new BorderLayout());
    frame.add(topPanel, BorderLayout.SOUTH);
    frame.add(scrollPane, BorderLayout.CENTER);
    frame.setSize(1080, 1080);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  public void setDungeon(Dungeon dungeon) {
    DungeonMapPanel.dungeon = dungeon;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (dungeon == null || dungeon.getRooms().isEmpty()) return;

    // Размер панели для рисования
    int panelWidth = getWidth();
    int panelHeight = getHeight();

    // Отступы от краёв
    int margin = 50;

    // Находим минимальные и максимальные координаты комнат
    int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
    int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

    for (Room room : dungeon.getRooms()) {
      int x = room.getX();
      int y = room.getY();
      if (x < minX) minX = x;
      if (x > maxX) maxX = x;
      if (y < minY) minY = y;
      if (y > maxY) maxY = y;
    }

    // Размер области, занимаемой комнатами
    int roomsWidth = maxX - minX;
    int roomsHeight = maxY - minY;

    // Размер одной комнаты в "логических" единицах (например, 1 комната = 1 единица)
    // Можно использовать фиксированный ROOM_SIZE как базу
    int baseRoomSize = ROOM_SIZE;

    // Вычисляем масштаб, чтобы все комнаты поместились с учётом отступов
    double scaleX = (panelWidth - 2.0 * margin) / (roomsWidth + baseRoomSize);
    double scaleY = (panelHeight - 2.0 * margin) / (roomsHeight + baseRoomSize);
    double scale = Math.min(scaleX, scaleY);

    // Функция преобразования логических координат комнаты в координаты на панели
    // с учётом масштаба и смещения
    // x и y — координаты комнаты
    // Возвращает Point — координаты центра комнаты на панели
    int finalMinX = minX;
    int finalMinY = minY;
    BiFunction<Integer, Integer, Point> toPanelCoords = (x, y) -> {
      int px = (int) ((x - finalMinX) * scale) + margin + (int) (baseRoomSize * scale / 2);
      int py = (int) ((y - finalMinY) * scale) + margin + (int) (baseRoomSize * scale / 2);
      return new Point(px, py);
    };

    // Рисуем коридоры
    for (Room room : dungeon.getRooms()) {
      Point p1 = toPanelCoords.apply(room.getX(), room.getY());

      for (EDirection dir : room.getAvailableExits()) {
        Room toRoom = room.getExit(dir);
        if (toRoom == null) continue;

        Point p2 = toPanelCoords.apply(toRoom.getX(), toRoom.getY());

        if (toRoom == room) {
          drawCircularCorridor(g2, p1.x, p1.y, scale, baseRoomSize);
        } else {
          boolean oneWay = !toRoom.getAvailableExits().contains(dir.getOpposite())
              || toRoom.getExit(dir.getOpposite()) != room;
          drawCorridor(g2, p1.x, p1.y, p2.x, p2.y, oneWay);
        }
      }
    }

    // Рисуем комнаты
    for (Room room : dungeon.getRooms()) {
      Point p = toPanelCoords.apply(room.getX(), room.getY());
      drawRoom(g2, room, p.x, p.y, scale, baseRoomSize);
    }

    // Рисуем легенду поверх всего (если есть)
    drawLegend(g2);
  }

// Модифицируем методы рисования, чтобы учитывать масштаб и размер комнаты

  private void drawRoom(Graphics2D g2, Room room, int cx, int cy, double scale, int baseRoomSize) {
    int size = (int) (baseRoomSize * scale);
    int halfSize = size / 2;

    // Цвет для стартовой комнаты (пример)
    if (room == dungeon.getStartRoom()) {
      g2.setColor(new Color(253, 217, 160));
    } else {
      switch (room.getEventType()) {
        case POSITIVE -> g2.setColor(new Color(144, 238, 144));
        case NEGATIVE -> g2.setColor(new Color(88, 76, 229));
        case COMBAT -> g2.setColor(new Color(165, 32, 32));
        case NOTHING -> g2.setColor(Color.LIGHT_GRAY);
      }
    }
    g2.fillRect(cx - halfSize, cy - halfSize, size, size);

    g2.setColor(Color.BLACK);
    g2.drawRect(cx - halfSize, cy - halfSize, size, size);

    String name = room.getName();
    FontMetrics fm = g2.getFontMetrics();
    int textWidth = fm.stringWidth(name);
    g2.drawString(name, cx - textWidth / 2, cy + fm.getAscent() / 2);
  }

  private void drawCorridor(Graphics2D g2, int x1, int y1, int x2, int y2, boolean oneWay) {
    g2.setColor(Color.BLUE);
    g2.setStroke(new BasicStroke(2));
    g2.drawLine(x1, y1, x2, y2);

    if (oneWay) {
      drawArrowHead(g2, x1, y1, x2, y2);
    }
  }

  private void drawCircularCorridor(Graphics2D g2, int cx, int cy, double scale, int baseRoomSize) {
    g2.setColor(Color.MAGENTA);
    g2.setStroke(new BasicStroke(2));
    int size = (int) (baseRoomSize * scale / 2);
    int arcX = cx + size / 2;
    int arcY = cy + size / 2;
    g2.drawArc(arcX, arcY, size, size, 0, 360);

    g2.setColor(Color.MAGENTA.darker());
  }


  private void drawLegend(Graphics2D g2) {
    int squareSize = 20;
    int padding = 10;

    g2.setColor(Color.BLACK);
    g2.setFont(new Font("SansSerif", Font.PLAIN, 14));

    final Color[] legendColors = {
        new Color(144, 238, 144), // Позитивное событие
        new Color(88, 76, 229),   // Негативное событие
        new Color(165, 32, 32),   // Сражение
        new Color(192, 192, 192)  // Ничего
    };

    final String[] legendLabels = {
        "Позитивное событие",
        "Негативное событие",
        "Сражение",
        "Ничего"
    };

    for (int i = 0; i < legendColors.length; i++) {
      // Рисуем цветной квадрат
      g2.setColor(legendColors[i]);
      g2.fillRect(padding, padding + i * (squareSize + padding), squareSize, squareSize);

      // Рисуем черную рамку вокруг квадрата
      g2.setColor(Color.BLACK);
      g2.drawRect(padding, padding + i * (squareSize + padding), squareSize, squareSize);

      // Рисуем подпись справа от квадрата
      g2.drawString(legendLabels[i], padding + squareSize + padding, padding + i * (squareSize + padding) + squareSize - 5);
    }
  }

  private void drawArrowHead(Graphics2D g2, int x1, int y1, int x2, int y2) {
    double phi = Math.toRadians(30);
    int barb = 15;
    double dy = y2 - y1;
    double dx = x2 - x1;
    double theta = Math.atan2(dy, dx);
    double x, y, rho = theta + phi;

    for (int j = 0; j < 2; j++) {
      x = x2 - barb * Math.cos(rho);
      y = y2 - barb * Math.sin(rho);
      g2.drawLine(x2, y2, (int) x, (int) y);
      rho = theta - phi;
    }
  }

  public void exportToPNG(File file) throws IOException {
    // Размер изображения — можно задать фиксированный или использовать текущий размер панели
    int panelWidth = getWidth() > 0 ? getWidth() : 1280;  // например, 1280 если размер не задан
    int panelHeight = getHeight() > 0 ? getHeight() : 720; // например, 720

    BufferedImage image = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(Color.WHITE);
    g2.fillRect(0, 0, panelWidth, panelHeight);

    if (dungeon == null || dungeon.getRooms().isEmpty()) {
      g2.dispose();
      ImageIO.write(image, "png", file);
      return;
    }

    int margin = 50; // отступы от краёв

    // Находим минимальные и максимальные координаты комнат
    int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
    int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

    for (Room room : dungeon.getRooms()) {
      int x = room.getX();
      int y = room.getY();
      if (x < minX) minX = x;
      if (x > maxX) maxX = x;
      if (y < minY) minY = y;
      if (y > maxY) maxY = y;
    }

    int roomsWidth = maxX - minX;
    int roomsHeight = maxY - minY;

    int baseRoomSize = ROOM_SIZE;

    // Рассчитываем масштаб по ширине и высоте с учётом размера комнат
    double scaleX = (panelWidth - 2.0 * margin) / (roomsWidth + baseRoomSize);
    double scaleY = (panelHeight - 2.0 * margin) / (roomsHeight + baseRoomSize);

    // Если масштаб слишком большой (например, карта очень маленькая), ограничиваем максимальный масштаб
    double scale = Math.min(scaleX, scaleY);

    int finalMinX = minX;
    int finalMinY = minY;
    double finalScale = scale;
    BiFunction<Integer, Integer, Point> toImageCoords = (x, y) -> {
      int px = (int) ((x - finalMinX) * finalScale) + margin + (int) (baseRoomSize * finalScale / 2);
      int py = (int) ((y - finalMinY) * finalScale) + margin + (int) (baseRoomSize * finalScale / 2);
      return new Point(px, py);
    };

    // Рисуем коридоры
    for (Room room : dungeon.getRooms()) {
      Point p1 = toImageCoords.apply(room.getX(), room.getY());

      for (EDirection dir : room.getAvailableExits()) {
        Room toRoom = room.getExit(dir);
        if (toRoom == null) continue;

        Point p2 = toImageCoords.apply(toRoom.getX(), toRoom.getY());

        if (toRoom == room) {
          drawCircularCorridor(g2, p1.x, p1.y, scale, baseRoomSize);
        } else {
          boolean oneWay = !toRoom.getAvailableExits().contains(dir.getOpposite())
              || toRoom.getExit(dir.getOpposite()) != room;
          drawCorridor(g2, p1.x, p1.y, p2.x, p2.y, oneWay);
        }
      }
    }

    // Рисуем комнаты
    for (Room room : dungeon.getRooms()) {
      Point p = toImageCoords.apply(room.getX(), room.getY());
      drawRoom(g2, room, p.x, p.y, scale, baseRoomSize);
    }

    // Рисуем легенду поверх всего
    drawLegend(g2);

    g2.dispose();

    ImageIO.write(image, "png", file);
  }
}