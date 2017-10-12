import java.util.Random
import org.apache.commons.math3.stat.*
import org.apache.commons.math3.stat.inference.ChiSquareTest
object Stat {
  val random = Random()
  
  fun main( args : Array<String> ) {
    val nm = (0..10000).map { Math.random() }

    val mean = StatUtils.mean( nm.toDoubleArray() )
    println( "mean ${mean}" )
    val geometricMean = StatUtils.geometricMean( nm.toDoubleArray() )
    println( "geometricMean ${geometricMean}" )
    val populationVariance = StatUtils.populationVariance( nm.toDoubleArray() )
    println( "populationVariance ${populationVariance}")
    val variance = StatUtils.variance( nm.toDoubleArray() )
    println( "variance ${variance}" )

    val nn = (0..1000).map { 0L }.toMutableList()
    (0..1000000).map { (Math.random()*1000 + 0.5).toInt() }.map {
      nn[it] += 1L
    }
    val mm = (0..1000).map{ 0L }.toMutableList()
    (0..1000000).map { ( (Math.random() + Math.random())/2.0*1000 + 0.5).toInt() }.map {
      mm[it] += 1L
    }
    val oo = (0..1000).map { 0L }.toMutableList()
    (0..1000000).map { (Math.random()*1000 + 0.5).toInt() }.map {
      oo[it] += 1L
    }
    val t = ChiSquareTest()
    val pval = t.chiSquareTestDataSetsComparison(mm.toLongArray(), nn.toLongArray())
    println("p-value mm <-> nn ${pval}")
    val pval2 = t.chiSquareTestDataSetsComparison(oo.toLongArray(), nn.toLongArray())
    println("p-value oo <-> nn ${pval2}")
  }
}
