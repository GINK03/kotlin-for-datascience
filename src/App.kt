
fun main(args : Array<String>) {
  val kargs = args.toList()
  when { 
    kargs.contains("cinema") -> CinemaAnalyze.main(args)
    kargs.contains("gson") -> GsonDemo.main(args)
    else -> null
  }
}

