/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.sql;

import java.awt.Component;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class App {
  // static final String[] hardwareTypes = {
  // "mainboard", "cpu", "gpu", "ram", "psu",
  // "storage", "ccase", "fan", "cpu_cooler", "rad" };
  static final String[] hardwareTypes = { "cpu" };
  static final JFrame frame = new JFrame("Simple GUI");
  static final JPanel panel = new JPanel();
  static final Map<String, JComboBox<String>> comboboxes = new HashMap<>(hardwareTypes.length);
  PreparedStatement ps;
  Connection con;
  final String url = "jdbc:mariadb://localhost:3306/HardWare";
  String user = "guest";
  String password = "password";

  public App() {
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(980, 720);

    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    try {
      connect();
      for (String hardwareType : hardwareTypes) {

        String query = "SELECT " + hardwareType + ".name, " + hardwareType +
            ".buyPrice FROM " + hardwareType + ";";
        ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        String[] choices = new String[rs.getFetchSize()];
        for (String choice : choices) {
          choice = rs.getString("name") + "\n" + rs.getLong("buyPrice");
          System.out.println(choice);
          rs.next();
        }

        JComboBox<String> cb = new JComboBox<>(choices);

        cb.setToolTipText("Add a " + hardwareType);

        cb.setMaximumSize(cb.getPreferredSize());
        cb.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(cb);

        comboboxes.put(hardwareType, cb);
      }
    } catch (SQLException e) {
      System.err.println(e.getMessage());
    }
    frame.add(panel);
    frame.setVisible(true);
  }

  // public String getPassword() {
  // try {
  // return new BufferedReader(new FileReader("passwd.txt")).readLine();
  // } catch (IOException e) {
  // return null;
  // }
  // }

  public Boolean connect() {
    try {
      con = DriverManager.getConnection(url, user, password);
      System.out.println("Connected Successfully!");
      return true;
    } catch (SQLException e) {
      return false;
    }
  }

  public static String compatible() {
    return "test";
  }

  public static void main(String[] args) {
    App A = new App();
  }
}
