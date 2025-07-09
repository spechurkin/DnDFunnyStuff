package dnd;

import java.io.*;
import java.util.*;

public class Main {
  public static void main(String[] args) {
    sortMdList(false);
    sortMdTable(2);
    sortSpellList();
  }

  public static void sortMdList(boolean sortAll) {
    File sortedFile = new File("src/main/resources/ul_li_sorted.md");
    assert !sortedFile.exists() || sortedFile.delete();
    try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/ul_li.md"));
         BufferedWriter bw = new BufferedWriter(new FileWriter(sortedFile, true))) {
      Map<String, String> listOfAll = new HashMap<>();
      String ul = "";
      StringBuilder lisBuilder = new StringBuilder();
      String string;
      while ((string = br.readLine()) != null) {
        if (string.split(" {2}").length == 1) {
          ul = string;
          lisBuilder = new StringBuilder();
          listOfAll.put(ul, "");
        } else {
          String lis = lisBuilder.append(string.split(" {2}")[1]).append(", ").toString();
          listOfAll.put(ul, lis);
        }
      }
      if (sortAll) {
        for (String key : listOfAll.keySet().stream().sorted().toList()) {
          if (!listOfAll.get(key).isEmpty()) {
            bw.write(key);
            bw.newLine();
            for (String elem :
                Arrays.stream(listOfAll.get(key).split(", ")).sorted().toList()) {
              bw.write("\t" + elem);
              bw.newLine();
            }
          } else {
            bw.write(key);
            bw.newLine();
          }
        }
      } else {
        for (String key : listOfAll.keySet().stream().sorted().toList()) {
          if (!listOfAll.get(key).isEmpty()) {
            bw.write(key);
            bw.newLine();
            for (String elem :
                listOfAll.get(key).split(", ")) {
              bw.write("\t" + elem);
              bw.newLine();
            }
          } else {
            bw.write(key);
            bw.newLine();
          }
        }
      }
      bw.flush();
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

  public static void sortMdTable(int column) {
    File sortedFile = new File("src/main/resources/sorted_table.md");
    assert !sortedFile.exists() || sortedFile.delete();
    List<String> lines = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/tables.md"))) {
      String line;
      while ((line = br.readLine()) != null) {
        lines.add(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (lines.size() < 3) {
      return;
    }
    // Извлекаем заголовок и строки данных
    String header = lines.get(0);
    String separator = lines.get(1);
    List<String> dataLines = new ArrayList<>(lines.subList(2, lines.size()));

    // Сортируем строки данных по второму полю
    dataLines.sort((line1, line2) -> {
      String[] fields1 = line1.split("\\|");
      String[] fields2 = line2.split("\\|");

      // Убираем лишние пробелы и сравниваем второе поле
      String field1 = fields1.length > column ? fields1[column].trim() : "";
      String field2 = fields2.length > column ? fields2[column].trim() : "";

      return field1.compareTo(field2);
    });

    // Запись отсортированной таблицы в файл
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(sortedFile))) {
      bw.write(header);
      bw.newLine();
      bw.write(separator);
      bw.newLine();
      for (String dataLine : dataLines) {
        bw.write(dataLine);
        bw.newLine();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void sortSpellList() {
    File sortedFile = new File("src/main/resources/sorted_spells.md");
    assert !sortedFile.exists() || sortedFile.delete();
    StringBuilder finalString = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/spells.md")); BufferedWriter bw = new BufferedWriter(new FileWriter(sortedFile, true))) {
      Arrays.stream(br.readLine().split(", ")).sorted().forEach(str -> finalString.append(str).append(", "));
      finalString.delete(finalString.length() - 2, finalString.length() - 1);
      bw.write(finalString.toString().trim());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}