package org.sql;

import java.sql.*;
import java.util.*;
import javax.swing.*;

public class FilterManager {
    private static List<String> hardwareTypes;
    private static ArrayList<JComboBox<Hardware>> comboBoxes;
    private static Map<Integer, Hardware> hardwareList;
    static ResultSet rs;

    public FilterManager(List<String> hardwareTypes, ArrayList<JComboBox<Hardware>> comboboxes,
            Map<Integer, Hardware> hardwareList) {
        FilterManager.hardwareTypes = hardwareTypes;
        FilterManager.comboBoxes = comboboxes;
        FilterManager.hardwareList = hardwareList;
    }

    public void createFilters() {
        GUI.checkCompatibilityButton.addActionListener(e -> FilterManager.checkWattage());
        JSlider[] priceFilterSlider = new JSlider[App.hardwareTypes.size()];
        try {
            for (int i = 0; i < priceFilterSlider.length; i++) {
                rs = SQLManager.getConnection()
                        .prepareStatement("SELECT MAX(" + App.hardwareTypes.get(i) + ".price) as maxPrice FROM "
                                + hardwareTypes.get(i))
                        .executeQuery();
                rs.next();
                int maxPrice = rs.getInt("maxPrice");
                priceFilterSlider[i] = new JSlider(JSlider.VERTICAL, 0, maxPrice, 0);
                priceFilterSlider[i]
                        .addChangeListener(new SliderFilter(priceFilterSlider[i].getMaximum(), "cpu", "price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        GUI.pricefilterSlider = priceFilterSlider;
        // Add sliders to legend or any other component if needed
        try {
            rs.close();
        } catch (Exception e) {
        }
    }

    public static Boolean filterByValue(String type, String characteristic, Object value, Boolean out) {
        String query = "SELECT " + type + ".ID FROM " + type + " WHERE " + type + "." + characteristic + " = " + value;
        return addFilter(new String[] { query }, type, out, comboBoxes, hardwareTypes, hardwareList);
    }

    public static Boolean filterByItem(String type, int ID, Boolean out) {
        ArrayList<String> queries = new ArrayList<>();
        switch (type) {
            case "cpu":
                queries.add("SELECT m.ID FROM mainboard m WHERE m.cpuForm <> (SELECT cpu.form FROM cpu WHERE cpu.ID = "
                        + ID + ")");
                break;
            case "ram":
                queries.add(
                        "SELECT m.ID FROM mainboard m WHERE m.ddrType <> (SELECT ram.ddrType FROM ram WHERE ram.ID = "
                                + ID + ")");
                queries.add("SELECT cpu.ID FROM cpu WHERE cpu.ddrType < (SELECT ram.ddrType FROM ram WHERE ram.ID = "
                        + ID + ")");
                break;
            case "mainboard":
                queries.add("SELECT cpu.ID FROM cpu WHERE cpu.form <> (SELECT m.form FROM mainboard m WHERE m.ID = "
                        + ID + ")");
                queries.add(
                        "SELECT ram.ID FROM ram WHERE ram.ddrType <> (SELECT m.ddrType FROM mainboard m WHERE m.ID = "
                                + ID + ")");
                queries.add("SELECT c.ID FROM ccase c WHERE c.size < (SELECT m.size FROM mainboard m WHERE m.ID = " + ID
                        + ")");
                break;
            case "ssd":
                queries.add(
                        "SELECT m.ID FROM mainboard m WHERE m.IO NOT LIKE CONCAT('%', (SELECT ssd.type FROM ssd WHERE ssd.ID = "
                                + ID + "), '%')");
                break;
            case "ccase":
                queries.add("SELECT m.ID FROM mainboard m WHERE m.size < (SELECT c.size FROM ccase c WHERE c.ID = " + ID
                        + ")");
                break;
            default:
                break;
        }
        return addFilter(queries.toArray(new String[0]), type, out, comboBoxes, hardwareTypes, hardwareList);
    }

    public static Boolean addFilter(String[] queries, String type, Boolean out,
            ArrayList<JComboBox<Hardware>> comboboxes,
            List<String> hardwareTypes, Map<Integer, Hardware> hardwareList) {
        JComboBox<Hardware> temp = comboboxes.get(hardwareTypes.indexOf(type));
        try {
            if (out) {
                for (String str : queries) {
                    rs = SQLManager.getConnection().prepareStatement(str).executeQuery();
                    while (rs.next()) {
                        temp.removeItem(hardwareList.get(rs.getInt("ID")));
                    }
                }
                comboboxes.set(hardwareTypes.indexOf(type), temp);
            } else {
                HashSet<Hardware> tempHash = new HashSet<>();
                for (int i = 0; i < temp.getItemCount(); i++) {
                    tempHash.add(temp.getItemAt(i));
                }
                for (String str : queries) {
                    rs = SQLManager.getConnection().prepareStatement(str).executeQuery();
                    while (rs.next()) {
                        tempHash.add(hardwareList.get(rs.getInt("ID")));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    static public void checkWattage() {
        int psuWattage = 0;
        try {
            rs = SQLManager.getConnection().prepareStatement("SELECT psu.wattage FROM psu").executeQuery();
            psuWattage = rs.getInt("wattage");
        } catch (SQLException e) {
        }
        if (App.config.get("wattage") > psuWattage) {
            JOptionPane.showMessageDialog(null, "PSU Overload",
                    "Your Config's power consumption is higher than your PSU can handle.\nconsider downgrading or using a better PSU",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
