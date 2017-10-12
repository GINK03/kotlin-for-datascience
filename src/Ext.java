import org.apache.commons.lang3.ArrayUtils;
import java.util.*;
import de.bwaldvogel.liblinear.*;
public class Ext {
  static double[] ConvDoubleArray(ArrayList<Double> alist) { 
    //return ArratUtils.toObject(alist);
    double[] doubleArray = new double[alist.size()];
    for (int i = 0 ; i < alist.size() ; i++) {
      Double d = alist.get(i); 
      doubleArray[i] = d;
    }
    return doubleArray;
  }
}
