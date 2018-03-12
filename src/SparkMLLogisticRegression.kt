import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

object SparkMLLogisticRegression {
  fun main( args:Array<String> ) {
    val spark = SparkSession
      .builder()
      .appName("Java Spark SQL data sources example")
      .config("spark.master", "local")
      .getOrCreate()

    println("This is a demo of SparkMLLogisticRegression.")
    val training = spark.read().format("libsvm").load("../../../resources/binary.txt")

    val trainer = LogisticRegression()
      .setMaxIter(10)
      .setRegParam(0.3)
      .setElasticNetParam(0.8)
      .setFamily("binomial")

    val model = trainer.fit(training)

    println("coef=${model.coefficientMatrix()} intercept=${model.interceptVector()}")
  }
}
