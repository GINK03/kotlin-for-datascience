# Kotlin For Datascience

# Requirements
- OracleJDK 8(not openjdk or Oracle JDK9)
- redis-server

# ArchLinux Install of JDK
Oracleは別途AURにあるらしいがyaourtより自分でパッケージをビルドしてしまった方がいい
例えば、OracleJDK8はこのように行う
```console
$ git clone https://aur.archlinux.org/jdk8.git
$ cd jdk8
$ makepkg
$ sudo pacman -U  jdk8-8u144-1-x86_64.pkg.tar.xz
$ archlinix-java status
$ sudo archlinux-java set java-8-jdk
```
このようなオペレーションが別途必要

# Run With Argments
GradleでArgmentsを指定する方法がよくわからなかったが、このように実行することで実現できる
```console
$ ./gradlew run -Dexec.args="placeholder arg1 arg2"
```
長くてめんどくさいね

=================

# Kotlin for DataScience

### TL;DR
Kotlinを用いるデータ分析と、簡単な統計分析、一部の機械学習などのデータサイエンスができることを示します。  

### Kotlinをデータサイエンスで使う
かなり変なモチベーションであることを理解しつつ、なぜKotlinを用いるかというと、Apache Sparkで用いられるRDDというデータフレームとの類似性が挙げられます  
Quoraという英語の質問サイトで、Kotlinはデータサイエンスとして、Pythonを脅かすかということかというと、DeepLearningやPandasなどのエコシステムの少なさと、言語として若すぎるということがあって、まだはやることはないだろうということです[[1]](https://www.quora.com/Will-Kotlin-replace-Python-in-data-science-and-machine-learning)  

しかし、Apache Sparkで用いらているScala置き換えになるかという質問がありまして、Absolutely（絶対に）という強い表現があります  

定性的な理由に以下のようなものがあります  

1. 強いJavaとの相互運用性があること
2. C#のプロによって作られて、Swiftとのシンタックスに似ていることから、JVM界隈に人が来やすいこと
3. Javaを多くの点で改良していること

などが挙げられています。クラスタリングなどの機械学習やHadoopやRedisを用いたデータ収集などをやっているということで、説得力があります[[2]](https://www.quora.com/Can-Kotlin-replace-Scala-in-data-science)


### ラムダ式を通して任意の結果を得る

KotlinはApache Sparkで用いられているScalaのシンタックスの流れを汲んでおり、複雑なデータ操作が可能です　　

Apache SparkにはRDDというデータフレームがあって、データフレームを用いてデータオペレーションができるのですが、KotlinやRubyなどのListに対する操作と似た感覚で使うことができます。RDDは関数間のデータをApache Parquetなどでシリアライズすることで、マシン間を横断してスケールアウトできるように設計されていますが、RDDを使わない場合、単一マシンでのメモリ上の操作になります  

具体的には、RDDとKotlinにはこのようなList, Set, Mapなど代表t系なデータ型に対するメソッドが用意されており、SQLで操作できるデータオペレーションと同等かそれ以上のことが可能です　　
(詳細が知りたい方は、[こちらを参照](https://github.com/GINK03/data-analysis-design-patterns)していただけると幸いです)  

### よく使う関数

Kotlinでよく使う関数としてこのようなものがあります  
```console
filter: 条件を満たす要素のみ抽出する
map: 各要素に関数を適用して別の要素に変換する
flatMap: 各要素に関数を適用して別の要素に変換する。関数の返り値はイテレータ
distinct: 重複する要素を取り除く
reduce: 畳み込みを行うような計算をします。系列の和を求めたり、積を求めたりします
groupBy: 特定のキーにてデータを転置・直列化して変換する
```
データとそれを処理するラムダ式は、圏論という数学の一分野で扱うことが可能です  

例えば、[この資料による](http://choreographlife.jp/pdf/intro.pdf)と、型を導入したラムダ式は、カルテシアン閉圏と対応することがわかります  

完全に適応できなくても、始域や終域の制約を外した圏論の状態とも捉えられ、部分的な圏論の知識を用いることで、単射か、全射か、そのラムダ式の並びはモニックなのか、エピックなのかという視点を追加することで、一般的な理論に還元した状態で、データオペレーションを扱うことができます  

これらを使いこなせ、関数の特徴を理解すると便利であり、groupByなど一部処理でメモリがマシンから溢れることがわかるので、本当にどこでマシンをスケールアウトすれば良いのかわかりやすいのと、様々な細やかなオペレーションが可能です。　　

### Kotlinに入っている組み込み関数でRDDのような簡単なデータオペレーションの例
Userという性別(sex)、年齢(age)、名前(name)の三つのフィールドのテーブルがあったとします  

```kotlin
data class User(val sex:String, val age:Int, val name:String)
```

このテーブルはKotlinのリテラルではこのように表現されます  
このデータに対してデータ操作を行っていきます  
```kotlin
val userList = listOf( 
  User("man", 10, "Alison Doe"),
  User("man", 22, "Bob Doe"), 
  User("woman", 12, "Alice Doe"),
  User("woman", 26, "Claris Doe"),
  User("woman", 17, "Dolly Doe")
)
```

20歳以上は何％か計算する 
```kotlin
val over20 = userList.filter { it.age >= 20 }.size.toDouble() / userList.size
println("20歳以上は${over20*100}%です")
(出力->) 20歳以上は40.0%です
```

男性と女性のそれぞれの平均年齢を計算する
```
userList.groupBy {
  it.sex
}.toList().map {
  val sex = it.first 
} val arr = it.second
  val mean = arr.map{it.age}.reduce {y,x -> y+x} / arr.size
  println("性別, ${sex}の平均年齢は${mean}歳です")
}
(出力->) 性別, manの平均年齢は16歳です
(出力->) 性別, womanの平均年齢は18歳です
```

Doeさん一家の名字をSmithに変える
```kotlin
userList.map {
  User(it.sex, it.age, it.name.replace("Doe", "Smith") )
}.map(::println)
(出力->) User(sex=man, age=10, name=Alison Smith)
(出力->) User(sex=man, age=22, name=Bob Smith)
(出力->) User(sex=woman, age=12, name=Alice Smith)
(出力->) User(sex=woman, age=26, name=Claris Smith)
(出力->) User(sex=woman, age=17, name=Dolly Smith)
```
### Kotlinにはなくて、Scalaにある関数型の操作をライブラリで補完する
標準で組み込まれている関数以外にも、より強い関数型の操作ができるようになるライブラリが提供されています  

関数合成、カリー化、バインド、オプションなどは[funKTionale]()というライブラリを用いると、使用可能になります  

オブジェクト志向（というか命令型）と関数型はどちらに偏りがちか、という視点で見ると、KotlinはJavaより関数型的で、ScalaはKotlinより関数型的であるという解釈があるようです[[3]](https://www.quora.com/How-does-Kotlin-compare-to-Scala-as-a-JVM-language-with-OO-and-functional-features)

```kotlin
fun main( args : Array<String> ) {
  // 関数の合成（左から右）
  val add = { a:Int -> a + 5 }
  val multiply = { a:Int -> a*3 }
  val add_multiply = add forwardCompose multiply
  println( add_multiply(2) ) // (2 + 5) * 3

　// 関数の合成（右から左）
  val multiply_add = add compose multiply
  println( multiply_add(2) ) // 2*3 + 5

  // 特的の引数に値を束縛する
  val addp = { a:Int,b:Int,c:Int -> a*b*c }
  val build_addp = addp.partially2(3)
  println( build_addp.partially1(5)(2) )

  //　カリー化
  val sumint = {a:Int, b:Int, c:Int -> a+b+c}
  val curry:(Int)->(Int)->(Int)->Int = sumint.curried()
  println("curried result ${curry(1)(2)(3)}")
}
```

### データの永続化と、DSL SQLによるデータ操作
#### Kotlinx Exposed
計算した結果や、一次加工、二時加工したデータをどこかに保存しておくと、非常に便利なことがあります。　　
データを加工した内容を何らかの方法でディスクやSQLに書き出す必要がありますが、SQLを使うことが多い人がそれなりにいらっしゃるかと思います。　　

DSL（Domain Specific Language）か扱える[Kotlinx Exposed](https://github.com/JetBrains/Exposed)にて、KotlinやScalaはSQLのオペレーションがOR Mapperの操作を容易にしています。　　

例えば、Kotlinx ExposedというKotlinを作成したJetBrain社のライブラリで、SQLで特定のテーブルを作り、データを代入するにはこのようにすればいいので、わかりやすいです
```kotlin
// テーブルの構造をこのように定義して、プログラムの中で利用できる
object CinemaDataFrame : Table() {
  val id = integer("id").autoIncrement().primaryKey() // Column<Int>
  val someInt = integer("someInt") // Column<Int>
  val text = varchar("someText", 100000) // Column<String>
}
```
 最初にデータベースとJDBCとコネクトします  
 コネクトすると、このようにしてデータを書き込むことができます
```kotlin
 fun someFunction() {
  Database.connect("jdbc:mysql://localhost:3306/cinema", driver = "com.mysql.cj.jdbc.Driver", user = "root", password = "root")
  transaction { 
     (0..100).map { index ->
       CinemaDataFrame.insert {
         it[someInt] = index
         it[text] = index.toString()
       }
     }
   }
}
```
全ての要素を取り出すには、このようにすることで取り出すことができます　　
```kotlin
CinemaDataFrame.selectAll().map { 
  // enter some operation...
  it[id] // idのフィールドを取り出せる
  it[someInt] // someIntのフィールドを取り出せる
  it[text] // textのフィールドを取り出せる
}
```
また、プログラム中にロジックを書かなくても、SQL程度であれば、似た記述方法で同等の結果を得ることができます　　
```kotlin
CinemaDataFrame.selectAll().groupBy(CinemaDataFrame.someInt)).forEach {
   val cinemaText = it[CinemaDataFrame.text]
   val cinemaCount = it[CinemaDataFrame.id.count()]
   if (cinemaCount > 0) {
     println("$cinemasCount cinema(s) $cinemaText")
   } else {
     println("Noone in $cinemaText")
   }
}
```

### データ構造のシリアライズとデシリアライズ
#### Kotlinx Serialization
様々なデータオブジェクトのシリアライズした内容を保存しておくことで、あたかも一つのクラス・オブジェクトを平文にしてマシン間の転送や、先ほどのSQLを利用することで、オブジェクト丸ごとの永続化などを行うことができます　　
 
JavaScriptのオブジェクトのシリアライザ・デシリアライザであるJSON.stringfy、JSON.parseというそのままの関数が使える[Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)というものがあります。  
 
Kotlinx Serializationではdata classと呼ばれるセッターゲッターなどが色々まとまったclassの定義の上に@でノーテーションをつけることで、シリアライズ可能になります
```kotlin
import kotlinx.serialization.*
import kotlinx.serialization.json.JSON

@Serializable
data class Data(val a: Int, @Optional val b: String = "42")

fun serialize() {
  println(JSON.stringify(Data(42))) // {"a": 42, "b": "42"}
}
```
 デシリアライズも逆の手順を踏むことで行えます
```kotlin
fun desrialize() {
  val obj = JSON.parse<Data>("""{"a":42}""") // Data(a=42, b="42")
 }
``` 
JSON以外にもKotlinx SerializationはGoogleのシリアライザなどであるProtocol Bufferなどをサポートしています　

#### Google Gson
もう一つの選択肢として、GoogleのシリアライザであるGsonなども便利です。  
GsonはKotlinx Serializationと異なりノーテーションがなくても、シリアライズとデシリアライズが可能であり、Map<String, Map<String, Any>>のような複雑な型にも適応可能であり、Kotlinxで用が足りない時には選択肢に入れると良いです　　
```kotlin
fun serialize() {
  val gson = Gson() 
  val map = mutableMapOf<Int, List<String>>( 0 to listOf("これは", "テスト"))
  val json = gson.toJson(map) 
}
```
デシリアライズには型パラメータのような変数を定義して、型情報を与えて任意の型にデシリアライズします  
```kotlin
fun deserialize(json: String) {
  val type = object : TypeToken<Map<Int, List<String>>>() {}.type 
  val recover:Map<String,List<String>> = gson.fromJson<Map<String, List<String>>>(json, type) 
  println( recover ) 
}
```

### PureJVMによる形態素解析
#### Kuromoji Neologd
幾人かの人が[形態素解析のライブラリ](https://github.com/atilika/kuromoji)を提供してくださっているおかげで、neologdなどの最新の辞書を追加した状態で、JVMのみで形態素解析が可能になりました  
githubから該当のプロジェクトをクローンしてmvn packageで一つにまとめたパッケージを作ることで簡単に再利用可能なjarファイルを得ることができます　　
これをKotlinをコンパイルする際のbuild.gradleなどに依存を追加することで、Kotlinでも利用可能です　　
```console
$ git clone https://github.com/atilika/kuromoji
$ cd kuromoji
$ mvn package
//  作成したjarファイルを利用することで、neologdの辞書を反映したモデルを構築可能
```
Kotlinで形態素解析する例です
```kotlin
object Neologd { 
  val tokenizer = Tokenizer() 
  fun main( args : Array<String> ) { 
    tokenizer.tokenize("一夜一夜に人見頃").map { 
      Pair(it.getSurface(), it.getAllFeatures()) 
    }.map {
      println(it)
    }
  }                                                                                                                                                                                                    
}  
```
実行すると、形態素解析できていることがわかります
```console
$ ./gradlew run -Dexec.args="neologd"
(一夜一夜に人見頃, 名詞,固有名詞,一般,*,*,*,一夜一夜に人見頃,ヒトヨヒトヨニヒトミゴロ,ヒトヨヒトヨニヒトミゴロ)

BUILD SUCCESSFUL
```

### mean, variance, mode,  normalize, chi square testなどを求める

Javaの統計ライブラリである、StatUtilsが用いられるのですがこのように用いることができます  

Chi Square Test（カイ二乗検定）などは、[事象の発生回数をカウント](http://www.statisticshowto.com/probability-and-statistics/chi-square/)していけば、p-valueを返却してくれるので、この値が十分小さければ(例えば0.05以下)、有意であると言えそうです　　

試しに、[randomを二回重ねて、少しだけコクのある乱数](https://togetter.com/li/1044668)にすると、p-valueは0になり、ただのrandomとは異なり、別の事象であるということができそうです。
```kotlin
mport java.util.Random
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
```

### 機械学習ツールを使う
#### Liblinear-Javaを用いる
LiblinearもJavaに移植されていて、使うことができます。  
interfaceは当然、Javaなのですが、KotlinがJavaと互換性があるおかげで、Liblinear-Javaを実行することが可能です  

例えば、2クラス分類ではこのようにすることで行えます  
この例ではliblinear フォーマットに従った出力を/tmp/logregwrapperに出力して、それで学習しています
```kotlin
PrintWriter("/tmp/logregwrapper").append(text).close()
val prob = Problem.readFromFile( File("/tmp/logregwrapper"), -1.0)
val solver = SolverType.MCSVM_CS // -s 0
val C = 100.0 // cost of constraints violation
val eps = 0.001 // stopping criteria
val parameter = Parameter(solver, C, eps)
val model:Model = Linear.train(prob, parameter)

val featWeights:List<Double> = model.getFeatureWeights().toList()
```

### まとめ
RDDのような専用のフレームワークを使わなくても、一台のマシンならばユーザ定義型で、シリアライズなしに関数間のデータ構造の移動ができるので便利です。（ビッグデータになると、RDDのようなフレームワークに載せる必要があります）  

この構造は、畳み込み関数を任意にユーザが動作を定義できたり、複雑にgroupByして、groupByして、何かしらの値を求めて、、、といった操作に対して柔軟な操作ができるので優れいていると考えています  

基本的には、メモリ上に溜め込む必要がある操作（groupBy, reduceでリストを作る, setを作りユニークにする）など以外は大きなデータでも処理できるので、処理ロジックに応じてどこがオーバーヘッドなのか理解しやすいなどのメリットがあります  
