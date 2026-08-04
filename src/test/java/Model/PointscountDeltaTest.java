// src/test/java/Model/PointscountDeltaTest.java
// Verifies differential pointscount persistence planning.
package Model;

import Tables.Pointscount;
import Tables.PointscountId;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PointscountDeltaTest {

  @Test
  void planForSeparatesInsertsUpdatesDeletesAndUnchangedRows() {
    Pointscount unchangedExisting = pointscount( 1,
                                                 10.0 );
    Pointscount unchangedDesired = pointscount( 1,
                                                10.0 );
    Pointscount changedExisting = pointscount( 2,
                                               20.0 );
    Pointscount changedDesired = pointscount( 2,
                                              25.0 );
    Pointscount removedExisting = pointscount( 3,
                                               30.0 );
    Pointscount insertedDesired = pointscount( 4,
                                               40.0 );

    PointscountDelta.SavePlan plan = PointscountDelta.planFor(
      List.of( unchangedExisting,
               changedExisting,
               removedExisting ),
      List.of( unchangedDesired,
               changedDesired,
               insertedDesired ) );

    assertEquals( List.of( insertedDesired ),
                  plan.getInserts() );
    assertEquals( List.of( changedDesired ),
                  plan.getUpdates() );
    assertEquals( List.of( removedExisting ),
                  plan.getDeletes() );
  }

  private Pointscount pointscount( long participantId,
                                   double pointsSD ) {
    return new Pointscount(
      new PointscountId( participantId,
                         2,
                         2026,
                         7,
                         282 ),
      null,
      pointsSD,
      2.0,
      3.0,
      4.0,
      5.0,
      6.0 );
  }
}
