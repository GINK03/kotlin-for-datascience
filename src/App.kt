
val s = {a:String -> "a+" + a }
fun main(args : Array<String>) {
  val kargs = args.toList()
  when { 
    kargs.contains("cinema") -> CinemaAnalyze.main(args)
    kargs.contains("gson") -> GsonDemo.main(args)
    kargs.contains("neologd") -> Neologd.main(args)
    kargs.contains("stat") -> Stat.main(args)
    kargs.contains("funktionale") -> Funktionale.main(args)
    kargs.contains("yssadwords") -> YssAdwordsParser.main(args)
    else -> null
  }
}

