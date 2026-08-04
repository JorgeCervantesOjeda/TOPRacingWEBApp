// src/test/java/Model/ModelBeanPriorityPointsTest.java
// Verifies event priority point calculations.
package Model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import Tables.Regatta;
import org.junit.jupiter.api.Test;

class ModelBeanPriorityPointsTest {

  @Test
  void priorityPointsIncreaseWithActiveParticipantsWhenPrizeAndEntryFeeMatch() {
    Regatta regatta = new Regatta();
    regatta.setPrizeFinishing( 80.0 );
    regatta.setPrizeEfficiency( 20.0 );
    regatta.setEntryfee( 10.0 );
    regatta.setTrackrental( 0.0 );
    regatta.setValidregistrations( 4 );

    assertEquals( 40.0,
                  new ModelBean().getRegattaPriorityPoints( regatta ),
                  0.0001 );
  }
}
