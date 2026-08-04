// src/main/java/Model/PointscountDelta.java
// Builds differential persistence plans for pointscount recalculation results.
package Model;

import Tables.Pointscount;
import Tables.PointscountId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class PointscountDelta {

  private static final double DIFFERENCE_TOLERANCE = 1.0E-9;

  private PointscountDelta() {
  }

  static SavePlan planFor( List<Pointscount> existing,
                           List<Pointscount> desired ) {
    Map<PointscountId, Pointscount> existingById = pointscountById( existing );
    Map<PointscountId, Pointscount> desiredById = pointscountById( desired );
    List<Pointscount> inserts = new ArrayList<>();
    List<Pointscount> updates = new ArrayList<>();
    List<Pointscount> deletes = new ArrayList<>();

    for( Pointscount item
         : desired ) {
      Pointscount current = existingById.get( item.getId() );
      if( current == null ) {
        inserts.add( item );
      } else if( hasDifferentValues( current,
                                     item ) ) {
        updates.add( item );
      }
    }

    for( Pointscount item
         : existing ) {
      if( !desiredById.containsKey( item.getId() ) ) {
        deletes.add( item );
      }
    }

    return new SavePlan( inserts,
                         updates,
                         deletes );
  }

  private static Map<PointscountId, Pointscount> pointscountById(
    List<Pointscount> items ) {
    Map<PointscountId, Pointscount> output = new HashMap<>();
    for( Pointscount item
         : items ) {
      output.put( item.getId(),
                  item );
    }
    return output;
  }

  private static boolean hasDifferentValues( Pointscount current,
                                             Pointscount desired ) {
    return isDifferent( current.getPointsSD(),
                        desired.getPointsSD() )
           || isDifferent( current.getPointsSO(),
                           desired.getPointsSO() )
           || isDifferent( current.getPointsRD(),
                           desired.getPointsRD() )
           || isDifferent( current.getPointsRO(),
                           desired.getPointsRO() )
           || isDifferent( current.getPointsED(),
                           desired.getPointsED() )
           || isDifferent( current.getPointsEO(),
                           desired.getPointsEO() );
  }

  private static boolean isDifferent( double current,
                                      double desired ) {
    return Math.abs( current - desired ) > DIFFERENCE_TOLERANCE;
  }

  static final class SavePlan {

    private final List<Pointscount> inserts;
    private final List<Pointscount> updates;
    private final List<Pointscount> deletes;

    private SavePlan( List<Pointscount> inserts,
                      List<Pointscount> updates,
                      List<Pointscount> deletes ) {
      this.inserts = inserts;
      this.updates = updates;
      this.deletes = deletes;
    }

    List<Pointscount> getInserts() {
      return inserts;
    }

    List<Pointscount> getUpdates() {
      return updates;
    }

    List<Pointscount> getDeletes() {
      return deletes;
    }
  }
}
