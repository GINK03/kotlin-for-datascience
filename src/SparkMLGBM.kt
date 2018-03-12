import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.PipelineStage
import org.apache.spark.ml.classification.*
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.*
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

object SparkMLGBT {
  fun main( args:Array<String> ) {
    val spark = SparkSession
      .builder()
      .appName("Java Spark SQL data sources example")
      .config("spark.master", "local")
      .getOrCreate()

    println("This is a demo of SparkMLLogisticRegression.")
    val training = spark.read().format("libsvm").load("../../../resources/binary.txt")

    val labelIndexer = StringIndexer()
      .setInputCol("label")
      .setOutputCol("indexedLabel")
      .fit(training)
    // Automatically identify categorical features, and index them.
    // Set maxCategories so features with > 4 distinct values are treated as continuous.
    val featureIndexer = VectorIndexer()
      .setInputCol("features")
      .setOutputCol("indexedFeatures")
      .setMaxCategories(4)
      .fit(training)

    val (trainingData, testData) = training.randomSplit(listOf(0.7, 0.3).toDoubleArray())

    val gbt = GBTClassifier()
      .setLabelCol("indexedLabel")
      .setFeaturesCol("indexedFeatures")
      .setMaxIter(10);
    
    val labelConverter = IndexToString()
      .setInputCol("prediction")
      .setOutputCol("predictedLabel")
      .setLabels(labelIndexer.labels())
    
    val pipeline = Pipeline().setStages(listOf<PipelineStage>( labelIndexer, featureIndexer, gbt, labelConverter).toTypedArray() )

    val model = pipeline.fit(trainingData)
    val predictions = model.transform(testData)

    predictions.select("predictedLabel", "label", "features").show(5)

    val evaluator = MulticlassClassificationEvaluator()
      .setLabelCol("indexedLabel")
      .setPredictionCol("prediction")
      .setMetricName("accuracy")

    val accuracy = evaluator.evaluate(predictions)

    println("Test Error Rate = ${1 - accuracy}")
  }
}
