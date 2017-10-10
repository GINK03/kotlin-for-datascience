
import java.io.PrintWriter
import java.io.File
import de.bwaldvogel.liblinear.Linear
import de.bwaldvogel.liblinear.Model
import de.bwaldvogel.liblinear.Parameter
import de.bwaldvogel.liblinear.Problem
import de.bwaldvogel.liblinear.SolverType
import de.bwaldvogel.liblinear.FeatureNode

object LogRegWrapper {
  fun exec( ys: List<Double>, Xs: List<List<Pair<Int, Double>>>) : List<Double> {
    val text = ys.zip(Xs).map {
      val ys = it.first.toString()
      val xs = it.second.map {  
        "${it.first+1}:${it.second}"
      }.joinToString(" ")
      "${ys} ${xs}"
    }.joinToString("\n")
    println( text )
    // Array<Object>型に関しては、KotlinとJavaの型変換がうまくいかなくて苦肉の策
    PrintWriter("/tmp/logregwrapper").append(text).close()
    val prob = Problem.readFromFile( File("/tmp/logregwrapper"), -1.0)
    val solver = SolverType.MCSVM_CS // -s 0
	  val C = 100.0 // cost of constraints violation
	  val eps = 0.001 // stopping criteria
    val parameter = Parameter(solver, C, eps)
    val model:Model = Linear.train(prob, parameter)

    val featWeights:List<Double> = model.getFeatureWeights().toList()
    return featWeights
  }
}

