/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.sql;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AppTest {
  App classUnderTest = new App();

  @Test
  void appConnection() {
    assertTrue(classUnderTest.sqlManager.connect());
    System.out.println("Connection successful");
  }

  // @Test
  // void appComboboxes() {
  // }

}
